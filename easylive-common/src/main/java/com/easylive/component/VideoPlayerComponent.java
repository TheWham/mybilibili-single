package com.easylive.component;

import com.easylive.constants.Constants;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.service.VideoInfoFilePostService;
import jakarta.annotation.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component("VideoPlayerComponent")
public class VideoPlayerComponent {


    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;


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
        UrlResource urlResource = new UrlResource(tsFilePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(urlResource);
    }
}
