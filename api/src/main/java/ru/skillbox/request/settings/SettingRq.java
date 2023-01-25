package ru.skillbox.request.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.skillbox.enums.NameNotification;

@Getter
@Setter
public class SettingRq {

    @JsonProperty(value = "notification_type")
    private NameNotification notificationType;
    private boolean enable;

    public static SettingRq getSettingRq(NameNotification name, boolean enable) {
        SettingRq settingRq = new SettingRq();
        settingRq.setNotificationType(name);
        settingRq.setEnable(enable);
        return settingRq;
    }
}