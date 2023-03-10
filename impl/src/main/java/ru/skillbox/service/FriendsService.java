package ru.skillbox.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.enums.StatusCode;
import ru.skillbox.exception.BadRequestException;
import ru.skillbox.exception.UserNotFoundException;
import ru.skillbox.model.Friendship;
import ru.skillbox.model.Person;
import ru.skillbox.model.User;
import ru.skillbox.repository.FriendsRepository;
import ru.skillbox.response.data.PersonDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class FriendsService {
    private final FriendsRepository friendsRepository;
    private final PersonService personService;


    public List<PersonDto> getRelationsForByCode(Long personId, StatusCode statusCode) {
        Optional<List<Friendship>> allByStatusCodeAndSrcPersonId =
                friendsRepository.findAllByStatusCodeAndSrcPersonId(statusCode, personId);
        ArrayList<PersonDto> result = new ArrayList<>();
        if (allByStatusCodeAndSrcPersonId.isPresent()) {
            PersonDto.PersonDtoBuilder builder = PersonDto.builder();
            List<Friendship> friendships = allByStatusCodeAndSrcPersonId.get();
            Person dstPerson;
            for (Friendship fr : friendships) {
                dstPerson = fr.getDstPerson();
                result.add(
                        builder
                                .id(dstPerson.getId())
                                .photo(dstPerson.getPhoto())
                                .statusCode(fr.getStatusCode().toString())
                                .firstName(dstPerson.getFirstName())
                                .lastName(dstPerson.getLastName())
                                .birthDate(dstPerson.getBirthDate())
                                .isOnline(dstPerson.getIsOnline())
                                .city("dstPerson.getCity().toString()")
                                .country("dstPerson.getCountry().toString()")
                                .build());
            }
        }
        log.debug("getRelationsForByCode with id = {}, code = {}, result size = {}",
                personId, statusCode, result.size());
        return result;
    }

    public String sendFriendRequest(Long currentUser, Long otherPersonId) throws UserNotFoundException {
        Optional<Friendship> friendship =
                friendsRepository.findBySrcPersonIdAndDstPersonId(currentUser, otherPersonId);
        if (friendship.isPresent() && friendship.get().getStatusCode() == StatusCode.FRIEND) {

            log.debug("friendship already exists {}", friendship.get());
        } else {
            Optional<Friendship> requestedFriendship = friendsRepository
                    .findByDstPersonIdAndSrcPersonIdAndStatusCodeIs(currentUser, otherPersonId, StatusCode.REQUEST_TO);
            Person otherPerson = personService.getPersonById(otherPersonId);
            Person currentPerson = personService.getPersonById(currentUser);
            if (requestedFriendship
                    .isEmpty()) {
                Friendship f = new Friendship();
                f.setDstPerson(otherPerson);
                f.setSrcPerson(currentPerson);
                f.setStatusCode(StatusCode.REQUEST_TO);
                friendsRepository.save(f);

                Friendship fr = new Friendship();
                fr.setSrcPerson(otherPerson);
                fr.setDstPerson(currentPerson);
                fr.setStatusCode(StatusCode.REQUEST_FROM);
                friendsRepository.save(fr);

                log.debug("friendship saved {}", fr);
            }
        }
        return "Ok";
    }

    public int getFriendsRequestsCountFor(Long personId) {
        Optional<List<Friendship>> optionalList = friendsRepository
                .findAllByStatusCodeLikeAndDstPersonId(StatusCode.REQUEST_TO, personId);
        return optionalList.map(List::size).orElse(0);


    }

    public String approveFriends(Long currentUser, Long otherPersonId) throws UserNotFoundException {
        Optional<Friendship> requestedFriendship = friendsRepository
                .findByDstPersonIdAndSrcPersonIdAndStatusCodeIs(currentUser, otherPersonId, StatusCode.REQUEST_TO);
        Optional<Friendship> alternativeFriendship = friendsRepository
                .findByDstPersonIdAndSrcPersonIdAndStatusCodeIs(otherPersonId, currentUser, StatusCode.REQUEST_FROM);
        if (requestedFriendship.isPresent() && alternativeFriendship.isPresent()) {
            Friendship fr = requestedFriendship.get();
            fr.setStatusCode(StatusCode.FRIEND);
            friendsRepository.save(fr);

            Friendship returnedFriendship = alternativeFriendship.get();
            returnedFriendship.setSrcPerson(fr.getDstPerson());
            returnedFriendship.setDstPerson(fr.getSrcPerson());
            returnedFriendship.setStatusCode(StatusCode.FRIEND);
            friendsRepository.save(returnedFriendship);

        }
        return "Ok";
    }

    public String deleteFriend(Long currentUser, Long otherPersonId) {
        Optional<Friendship> fr = friendsRepository.findBySrcPersonIdAndDstPersonId(currentUser, otherPersonId);
        Optional<Friendship> fr2 = friendsRepository.findBySrcPersonIdAndDstPersonId(otherPersonId, currentUser);
        if (fr.isPresent() && fr2.isPresent()) {
            Friendship friendship = fr.get();
            friendship.setPreviousStatus(friendship.getStatusCode());
            friendship.setStatusCode(StatusCode.NONE);
            friendsRepository.save(friendship);

            Friendship friendship2 = fr2.get();
            friendship2.setPreviousStatus(friendship2.getStatusCode());
            friendship2.setStatusCode(StatusCode.NONE);
            friendsRepository.save(friendship2);
            log.debug("friendship save = {}, {}",
                    currentUser, otherPersonId);
        } else {
            log.debug("friendship not present = {}, {}",
                    currentUser, otherPersonId);
        }
        return "Ok";
    }

    public String subscribe(Long currentUserId, Long otherPersonId) throws UserNotFoundException {
        Person currentPerson = personService.getPersonById(currentUserId);
        Person otherPerson = personService.getPersonById(otherPersonId);
        Friendship f = new Friendship();
        f.setDstPerson(otherPerson);
        f.setSrcPerson(currentPerson);
        f.setStatusCode(StatusCode.SUBSCRIBED);
        friendsRepository.save(f);
        return "Ok";
    }

    public String blockFriend(Long currentUserId, Long otherPersonId) throws UserNotFoundException {
        log.info("start blockFriend: currentUser={}, to block={}", currentUserId, otherPersonId);

        if (currentUserId.equals(otherPersonId)) {
            throw new BadRequestException("Friendship cannot be formed");
        }
        Optional<Friendship> blockFriends1 = friendsRepository
                .findBySrcPersonIdAndDstPersonId(currentUserId, otherPersonId);
        Optional<Friendship> blockFriends2 = friendsRepository
                .findBySrcPersonIdAndDstPersonId(otherPersonId, currentUserId);

        if (blockFriends1.isPresent() && blockFriends2.isPresent()) {
            Friendship friendshipDirect = blockFriends1.get();
            if (friendshipDirect.getStatusCode() != StatusCode.BLOCKED) {
                friendshipDirect.setPreviousStatus(friendshipDirect.getStatusCode());
                friendshipDirect.setStatusCode(StatusCode.BLOCKED);

            } else {
                friendshipDirect.setPreviousStatus(friendshipDirect.getStatusCode());
                friendshipDirect.setStatusCode(StatusCode.NONE);
            }
            friendsRepository.save(friendshipDirect);
            Friendship friendshipReverse = blockFriends2.get();
            log.info("User {} blocks user {}", currentUserId, otherPersonId);

            friendshipReverse.setPreviousStatus(friendshipReverse.getStatusCode());
            friendshipReverse.setStatusCode(StatusCode.NONE);
            friendsRepository.save(friendshipReverse);

            log.info("finish blockFriend: currentUser={}, to block={}", currentUserId, otherPersonId);

        }
        return "Ok";
    }

    public Object searchRecommendations(User currentUser) {
        log.info("start searchRecommendations");


        log.info("finish searchRecommendations");

        return null;

    }
}
