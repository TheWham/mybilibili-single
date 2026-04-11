package com.easylive.component;

import com.easylive.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.VideoPlayDTO;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.service.VideoInfoFilePostService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component("VideoPlayerComponent")
public class VideoPlayerComponent {


    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;
    @Resource
    private RedisComponent redisComponent;

    public ResponseEntity<UrlResource> videoResource(String fileId, String projectFolder) throws MalformedURLException {
        VideoInfoFilePost filePostByFileId = videoInfoFilePostService.getVideoInfoFilePostByFileId(fileId);
        String filePath = filePostByFileId.getFilePath();
        String completeFilePath = projectFolder + Constants.FILE_PATH_FOLDER + filePath;
        Path m3u8FilePath = Paths.get(completeFilePath).resolve(Constants.M3U8_NAME).normalize();
        if (!Files.exists(m3u8FilePath) || !Files.isRegularFile(m3u8FilePath)) {
            return ResponseEntity.notFound().build();
        }
        UrlResource urlResource = new UrlResource(m3u8FilePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .body(urlResource);
    }


    public ResponseEntity<UrlResource> videoResource(String fileId, String name, String projectFolder) throws MalformedURLException {
        VideoInfoFilePost filePostByFileId = videoInfoFilePostService.getVideoInfoFilePostByFileId(fileId);
        String filePath = filePostByFileId.getFilePath();
        String completeFilePath = projectFolder + Constants.FILE_PATH_FOLDER + filePath;
        Path tsFilePath = Paths.get(completeFilePath).resolve(name).normalize();
        if (!Files.exists(tsFilePath) || !Files.isRegularFile(tsFilePath)) {
            return ResponseEntity.notFound().build();
        }


        String tokenId = getTokenIdFromCookie();
        TokenUserInfoDTO tokenInfo = redisComponent.getTokenInfo(tokenId);
        if (tokenInfo != null) {
            VideoPlayDTO videoPlayDTO = new VideoPlayDTO();
            videoPlayDTO.setVideoId(filePostByFileId.getVideoId());
            videoPlayDTO.setFileIndex(filePostByFileId.getFileIndex());
            videoPlayDTO.setUserId(tokenInfo.getUserId());
            videoPlayDTO.setVideoUserId(filePostByFileId.getUserId());
            //用uv做真实记录统计
            redisComponent.saveVideoPlayCount2HLL(videoPlayDTO.getVideoId(), videoPlayDTO.getUserId());
            //用户id保留30min, 30min之后再次观看可记录到播放统计
            boolean isEffectivePlay = redisComponent.saveVideoEffectivePlay(videoPlayDTO.getVideoId(), videoPlayDTO.getUserId());
            if (isEffectivePlay) {
                //设置用户数量缓存
                redisComponent.incrementUserStats(videoPlayDTO.getVideoUserId(), UserStatsRedisEnum.VIDEO_PLAY.getField(), Constants.ONE);
                //记录增量20s统计完之后清空
                redisComponent.addVideoPlayCountDelta(videoPlayDTO.getVideoId());
            }
            redisComponent.saveVideoHistory(videoPlayDTO.getVideoId(), videoPlayDTO.getUserId(), videoPlayDTO.getFileIndex());
        }

        UrlResource urlResource = new UrlResource(tsFilePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(urlResource);
    }


    private String getTokenIdFromCookie()
    {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies)
        {
            if (cookie.getName().equals(Constants.WEB_TOKEN_KEY))
                return cookie.getValue();
        }
        return null;
    }
}
