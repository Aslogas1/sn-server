package ru.skillbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skillbox.enums.NameNotification;
import ru.skillbox.model.Settings;
import ru.skillbox.repository.SettingRepository;
import ru.skillbox.request.settings.SettingRq;
import ru.skillbox.response.settings.NotificationSettingsDto;
import ru.skillbox.response.settings.SettingsDto;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final PersonService personService;
    private final SettingRepository settingRepository;


    public NotificationSettingsDto getSetting() {
        Settings settings = getSettingNotif();
        NotificationSettingsDto settingsDto = new NotificationSettingsDto();
        settingsDto.setTime(new Date().toString());
        settingsDto.getListSettings().add(SettingRq.getSettingRq(
                NameNotification.FRIEND_REQUEST, settings.isFriendRequest()));
        settingsDto.getListSettings().add(SettingRq.getSettingRq(
                NameNotification.FRIEND_BIRTHDAY, settings.isFriendBirthday()));
        settingsDto.getListSettings().add(SettingRq.getSettingRq(
                NameNotification.POST_COMMENT, settings.isPostComment()));
        settingsDto.getListSettings().add(SettingRq.getSettingRq(
                NameNotification.COMMENT_COMMENT, settings.isCommentComment()));
        settingsDto.getListSettings().add(SettingRq.getSettingRq(
                NameNotification.POST, settings.isPost()));
        settingsDto.getListSettings().add(SettingRq.getSettingRq(
                NameNotification.MESSAGE, settings.isMessage()));
        settingsDto.getListSettings().add(SettingRq.getSettingRq(
                NameNotification.SEND_EMAIL_MESSAGE, settings.isSendEmailMessage()));
        settingsDto.setUserId(settings.getId());
        return settingsDto;
    }


    public void saveSettings(NameNotification notification, boolean bool) {
        Settings setting = getSettingNotif();
        switch (notification.getName()) {
            case "friendRequest":
                setting.setFriendRequest(bool);
                break;
            case "postComment":
                setting.setPostComment(bool);
                break;
            case "friendBirthday":
                setting.setFriendBirthday(bool);
                break;
            case "post":
                setting.setPost(bool);
                break;
            case "commentComment":
                setting.setCommentComment(bool);
                break;
            case "message":
                setting.setMessage(bool);
                break;
            case "sendPhoneMessage":
                setting.setSendPhoneMessage(bool);
                break;
            case "sendEmailMessage":
                setting.setSendEmailMessage(bool);
                break;
        }
        settingRepository.save(setting);
    }


    public SettingsDto compareSettings(SettingsDto settingsDto) {
        Settings settingNotification = getSettingNotif();
        if (settingNotification.getId().equals(settingsDto.getId())) {
            settingNotification.setPost(settingsDto.isPost());
            settingNotification.setMessage(settingsDto.isMessage());
            settingNotification.setCommentComment(settingsDto.isCommentComment());
            settingNotification.setPostComment(settingsDto.isPostComment());
            settingNotification.setFriendBirthday(settingsDto.isFriendBirthday());
            settingNotification.setFriendRequest(settingsDto.isFriendRequest());
            settingNotification.setSendEmailMessage(settingsDto.isSendEmailMessage());
            settingNotification.setSendPhoneMessage(settingsDto.isSendPhoneMessage());
            settingRepository.save(settingNotification);
        }
        return settingsDto;
    }

    public Settings getSettingNotif() {
        return settingRepository.findById(personService
                .getCurrentPerson()
                .getSettings().getId()).get();
    }
}