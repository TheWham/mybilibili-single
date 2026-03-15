package com.easylive.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoCommentDTO {

    @Size(max = 500)
    @NotEmpty
    private String content;
    @Size(max = 65)
    private String imgPath;
    @NotEmpty
    private String videoId;
    private Integer replyCommentId;
    private String nickName;

}
