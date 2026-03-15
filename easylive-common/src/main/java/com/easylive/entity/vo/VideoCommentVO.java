package com.easylive.entity.vo;

import com.easylive.entity.po.VideoComment;

import java.util.List;

public class VideoCommentVO {
    private PaginationResultVO<VideoComment>  commentData;
    private List<UserActionVO> userActionList;
    private Boolean showReply;

    public Boolean getShowReply() {
        return showReply;
    }

    public void setShowReply(Boolean showReply) {
        this.showReply = showReply;
    }

    public PaginationResultVO<VideoComment> getCommentData() {
        return commentData;
    }

    public void setCommentData(PaginationResultVO<VideoComment> commentData) {
        this.commentData = commentData;
    }

    public List<UserActionVO> getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List<UserActionVO> userActionList) {
        this.userActionList = userActionList;
    }
}
