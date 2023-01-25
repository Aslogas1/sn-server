package ru.skillbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skillbox.common.AccountDto;
import ru.skillbox.enums.NameNotification;
import ru.skillbox.enums.Status;
import ru.skillbox.enums.StatusCode;
import ru.skillbox.exception.UserNotFoundException;
import ru.skillbox.mapper.AccountMapper;
import ru.skillbox.model.*;
import ru.skillbox.repository.MessageRepository;
import ru.skillbox.repository.NotificationRepository;
import ru.skillbox.request.settings.NotificationInputDto;
import ru.skillbox.response.data.PersonDto;
import ru.skillbox.response.settings.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationsService {

    private final NotificationRepository notificationRepository;
    private final PersonService personService;
    private final MessageRepository messageRepository;
    private final EmailService emailService;
    private final FriendsService friendsService;

    public NotificationSentDto getNotifications() {
        try {
            getAllNotification();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
        NotificationSentDto notificationSentDto = new NotificationSentDto();
        List<NotificationDataRs> dataRs = new ArrayList<>();
        try {
            dataRs = saveAllNotificationInNotificationDataRs();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
        notificationSentDto.setData(dataRs);
        notificationSentDto.setTimeStamp(new Date().toString());
        return notificationSentDto;
    }


    public NotificationCountRs getNotificationCount() {
        NotificationCountRs notificationCountRs = new NotificationCountRs();
        CountRs countRs = new CountRs();
        int count = getListNotification().size();
        countRs.setCount(count);
        notificationCountRs.setCountRs(countRs);
        notificationCountRs.setTimestamp(new Date().getTime());

        return notificationCountRs;
    }


    public void getAllNotification() throws UserNotFoundException {
        Person person = personService.getCurrentPerson();
        Settings settings = person.getSettings();
        List<PersonDto> friendList = friendsService
                .getRelationsForByCode(
                        person.getId(),
                        StatusCode.FRIEND);
        for (PersonDto personDto : friendList) {
            Person friend = personService.getPersonById(personDto.getId());
            if (settings.isPost()) {
                for (Post post : friend.getPostList()) {
                    saveNewNotification(
                            person.getId(),
                            post.getTitle(),
                            NameNotification.POST,
                            AccountMapper
                                    .INSTANCE
                                    .personToAccountDto(post.getPerson()));
                }
            } else if (settings.isFriendBirthday()) {
                LocalDateTime date = LocalDateTime.parse(new Date().toString());
                LocalDateTime date1 = LocalDateTime.parse(friend.getBirthDate().toString());
                if (date.getDayOfMonth() == date1.getDayOfMonth()) {
                    saveNewNotification(
                            person.getId(),
                            "The birthday at " + friend.getFirstName(),
                            NameNotification.FRIEND_BIRTHDAY,
                            AccountMapper
                                    .INSTANCE
                                    .personToAccountDto(friend));
                }
            }
        }
        if (settings.isPostComment()) {
            for (PostComment postComment : person.getPostCommentList()) {
                saveNewNotification(
                        person.getId(),
                        postComment.getCommentText(),
                        NameNotification.POST_COMMENT,
                        AccountMapper
                                .INSTANCE
                                .personToAccountDto(postComment.getPerson()));
            }
        }
        if (settings.isMessage()) {
            for (Message message :
                    messageRepository.findAllByRecipientIdAndStatus(
                            person,
                            Status.SENT)) {
                saveNewNotification(
                        person.getId(),
                        message.getMessageText(),
                        NameNotification.MESSAGE,
                        AccountMapper
                                .INSTANCE
                                .personToAccountDto(message.getAuthorId()));
            }
        }
    }


    public NotificationDto getNotificationDto(NotificationInputDto notificationInputDto) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setId(notificationInputDto.getUserId());
        notificationDto.setNotificationType(notificationInputDto.getNameNotification());
        notificationDto.setAuthorId(notificationInputDto.getAuthorId());
        notificationDto.setContent(notificationInputDto.getContent());
        notificationDto.setTime(new Date().toString());
        notificationDto.setUserId(notificationInputDto.getUserId());
        notificationDto.setStatusSent(getStatusReadById(notificationInputDto.getUserId()));
        trueRead(notificationDto.getId());
        return notificationDto;
    }


    public void saveNewNotification(
            long personId,
            String title,
            NameNotification name,
            AccountDto acc) {
        Notification notification = new Notification();
        notification.setPersonId(personId);
        notification.setNameNotification(name);
        notification.setContent(title);
        notification.setAuthorId(acc.getId());
        notification.setRead(false);
        if (ifNewNotification(notification)) {
            notificationRepository.save(notification);
        }
    }

    public boolean ifNewNotification(Notification n) {
        boolean ok = false;
        for (Notification notification : getListNotification()) {
            ok = !notification.getContent().equals(n.getContent()) &&
                    !notification.getAuthorId().equals(n.getAuthorId()) &&
                    !notification.getNameNotification().equals(n.getNameNotification());
        }
        return ok;
    }


    public List<NotificationDataRs> saveAllNotificationInNotificationDataRs() throws UserNotFoundException {
        Person person = personService.getCurrentPerson();
        List<NotificationDataRs> dataRs = new ArrayList<>();
        List<Notification> notificationList = notificationRepository
                .findByPersonId(person.getId());
        for (Notification notification1 : notificationList) {
            if (!notification1.isRead()) {
                Person id = personService.getPersonById(notification1.getPersonId());
                NotificationDataRs notificationDataRs = new NotificationDataRs();
                notificationDataRs.setId(notification1.getId());
                notificationDataRs.setNotificationType(notification1.getNameNotification());
                notificationDataRs.setSentTime(new Date().toString());
                notificationDataRs.setContent(notification1.getContent());
                notificationDataRs.setAuthor(AccountMapper
                        .INSTANCE
                        .personToAccountDto(id));
                dataRs.add(notificationDataRs);
                if (person.getSettings().isSendEmailMessage()) {
                    emailService.sendSimpleMessage(
                            person.getEmail(),
                            notification1.getNameNotification().getName(),
                            notification1.getContent());
                }
            }
        }
        return dataRs;
    }


    public void trueRead(Long id) {
        for (Notification notification : notificationRepository.findByPersonId(id)) {
            if (notification.getId().equals(id)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }
    }


    public boolean getStatusReadById(Long id) {
        Notification notification = new Notification();
        if (notificationRepository.findById(id).isPresent()) {
            notification = notificationRepository.findById(id).get();
        }
        return notification.isRead();
    }

    public List<Notification> getListNotification() {
        Person person = personService.getCurrentPerson();
        return notificationRepository
                .findByPersonId(person.getId());
    }
}
