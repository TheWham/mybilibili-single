package com.easylive.utils;


import com.easylive.config.AdminConfig;
import com.easylive.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

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

}
