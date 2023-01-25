package ru.skillbox.enums;

import lombok.Getter;

@Getter
public enum NameNotification {

    FRIEND_REQUEST("friendRequest"),
    FRIEND_BIRTHDAY("friendBirthday"),
    POST_COMMENT("postComment"),
    COMMENT_COMMENT("commentComment"),
    POST("post"),
    MESSAGE("massage"),
    SEND_PHONE_MESSAGE("sendPhoneMessage"),
    SEND_EMAIL_MESSAGE("sendEmailMessage");

    private final String name;

    NameNotification(String name) {
        this.name = name;
    }
}
