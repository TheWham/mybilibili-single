package com.easylive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AdminConfig {

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.account:}")
    private String account;
    @Value("${admin.password:}")
    private String password;

    @Value("${showFFmpegLog:true}")
    private Boolean showFFmpegLog;

    @Value("${es.index.video.name:easylive_video}")
    private String esIndexVideoName;

    @Value("${es.index.video.subtitle.vector.name:easylive_video_subtitle_vector}")
    private String esIndexVideoSubtitleVectorName;

    @Value("${es.host.port:127.0.0.1:9201}")
    private String esHostPort;

    @Value("${ai.rag.min-score:0.55}")
    private Double aiRagMinScore;

    @Value("${ai.rag.default-top-k:5}")
    private Integer aiRagDefaultTopK;

    @Value("${ai.rag.max-top-k:10}")
    private Integer aiRagMaxTopK;

    public String getEsIndexVideoName() {
        return esIndexVideoName;
    }

    public String getEsIndexVideoSubtitleVectorName() {
        return esIndexVideoSubtitleVectorName;
    }

    public String getEsHostPort() {
        return esHostPort;
    }

    public Double getAiRagMinScore() {
        return aiRagMinScore;
    }

    public Integer getAiRagDefaultTopK() {
        return aiRagDefaultTopK;
    }

    public Integer getAiRagMaxTopK() {
        return aiRagMaxTopK;
    }

    public Boolean getShowFFmpegLog() {
        return showFFmpegLog;
    }

    public String getAccount() {
        return account;
    }


    public String getPassword() {
        return password;
    }

    public String getProjectFolder() {
        return projectFolder;
    }
}
