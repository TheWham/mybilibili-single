package com.easylive.utils;


import com.easylive.config.AdminConfig;
import com.easylive.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FFmpegUtils {
    @Resource
    private AdminConfig adminConfig;

    public void createImageThumbnail(String filePath)
    {
        String CMD = "ffmepg -i \"%s\" -vf scale=200:-1 \"%s\"";
        CMD = String.format(CMD, filePath, filePath + Constants.IMAGE_THUMBNAIL_SUFFIX);
        ProcessUtils.executeCommand(CMD, adminConfig.getShowFFmpegLog());
    }

    public Integer getVideoInfoDuration(String completeVideo)
    {
        final String CMD_GET_CODE = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        String cmd = String.format(CMD_GET_CODE, completeVideo);
        String result = ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
        if (StringTools.isEmpty(result))
            return 0;
        result = result.replace("\n", "");
        return new BigDecimal(result).intValue();
    }
    public String getVideoCodec(String videoFilePath)
    {
        final String CMD_GET_CODE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        String cmd = String.format(CMD_GET_CODE, videoFilePath);
        String result = ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
        result = result.replace("\n", "");
        result = result.substring(0,result.indexOf("=") + 1);
        String codec = result.substring(0, result.indexOf("["));
        return codec;
    }

    public void convertHevc2Mp4(String newFileName, String videoFilePath)
    {
        String CMD_HEVC_264 = "ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y";
        String cmd = String.format(CMD_HEVC_264, newFileName, videoFilePath);
        ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
    }
}
