package com.easylive.entity.event;

import com.easylive.enums.UserStatsRedisEnum;
import org.springframework.context.ApplicationEvent;

public class UserStatsChangeEvent extends ApplicationEvent {
    private String fromUserId;
    private String toUserId;
    private UserStatsRedisEnum typeEnum;
    private Integer count;

    public UserStatsChangeEvent(Object source, String fromUserId, String toUserId, int count, UserStatsRedisEnum typeEnum) {
        super(source);
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.count = count;
        this.typeEnum = typeEnum;
    }

    public UserStatsRedisEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(UserStatsRedisEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}

