package com.easylive.service;

import com.easylive.entity.po.UserAction;

/**
 * 是评论和视频action的综合 写在同一个接口, 但是分开存储
 */
public interface UserActionService {

    void doAction(UserAction userAction);
}
