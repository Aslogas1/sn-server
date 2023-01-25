package ru.skillbox.controller;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.request.settings.NotificationInputDto;
import ru.skillbox.request.settings.SettingRq;
import ru.skillbox.response.settings.*;
import ru.skillbox.service.NotificationsService;
import ru.skillbox.service.SettingService;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final SettingService settingService;

    private final NotificationsService notificationsService;

    private static final Logger logger = LogManager
            .getLogger(NotificationSettingController.class);


    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsDto> getSettings() {
        return ResponseEntity.ok(settingService.getSetting());
    }

    @PutMapping("/settings")
    public ResponseEntity<DefaultRs> putSettings(@RequestBody SettingRq settingRq) {
        settingService.saveSettings(settingRq.getNotificationType(), settingRq.isEnable());
        DefaultRs defaultRs = new DefaultRs();
        defaultRs.setTime((new Date().toString()));
        defaultRs.setStatus(true);
        logger.info("putSetting" + settingRq
                .getNotificationType()
                .getName() +
                " : " + settingRq.isEnable());
        return ResponseEntity.ok().body(defaultRs);
    }

    @PostMapping("/settings")
    public SettingsDto createSettings(SettingsDto settingsDto) {
        logger.info("postSetting" + settingService
                .compareSettings(settingsDto)
                .toString());
        return settingService.compareSettings(settingsDto);
    }

    @GetMapping("")
    public ResponseEntity<NotificationSentDto> getNotification() {
        logger.info("getNotification " + notificationsService
                .getNotifications()
                .getTimeStamp());
        return ResponseEntity.ok(notificationsService.getNotifications());
    }

    @PostMapping("")
    public ResponseEntity<NotificationDto> createNotification(@RequestBody NotificationInputDto notif) {
        return ResponseEntity.ok(notificationsService.getNotificationDto(notif));
    }

    @GetMapping("/count")
    public ResponseEntity<NotificationCountRs> getNotificationCount() {
        return ResponseEntity.ok(notificationsService.getNotificationCount());
    }
}