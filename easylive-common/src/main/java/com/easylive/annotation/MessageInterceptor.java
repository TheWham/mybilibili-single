package com.easylive.annotation;

import com.easylive.enums.MessageTypeEnum;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author amani
 * @since 2026.4.12
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageInterceptor {
    boolean sendMessage() default true;
    MessageTypeEnum messageType() default MessageTypeEnum.SYSTEM;

    /**
     * 点赞投币收藏用了一个doAction方法需要用userActionType来区分
     */
    boolean resolveByActionType() default false;
}
