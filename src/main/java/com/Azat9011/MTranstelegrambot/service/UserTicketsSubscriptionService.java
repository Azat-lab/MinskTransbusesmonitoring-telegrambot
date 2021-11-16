package com.Azat9011.MTranstelegrambot.service;

import com.Azat9011.MTranstelegrambot.model.UserTicketsSubscription;
import com.Azat9011.MTranstelegrambot.repository.UserTicketsSubscriptionMongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сохраняет, удаляет, ищет подписки пользователя.
 *
 * @author Azat9011
 */
@Service
public class UserTicketsSubscriptionService {

    private final UserTicketsSubscriptionMongoRepository subscriptionsRepository;

    public UserTicketsSubscriptionService(UserTicketsSubscriptionMongoRepository repository) {
        this.subscriptionsRepository = repository;
    }

    public List<UserTicketsSubscription> getAllSubscriptions() {
        return subscriptionsRepository.findAll();
    }

    public void saveUserSubscription(UserTicketsSubscription usersSubscription) {
        subscriptionsRepository.save(usersSubscription);
    }

    public void deleteUserSubscription(String subscriptionID) {
        subscriptionsRepository.deleteById(subscriptionID);
    }


    public boolean hasTicketsSubscription(UserTicketsSubscription userSubscription) {
        return subscriptionsRepository.findByChatIdAndBusNumberAndDateDepart(userSubscription.getChatId(),
                userSubscription.getBusNumber(), userSubscription.getDateDepart()).size() > 0;
    }

    public Optional<UserTicketsSubscription> getUsersSubscriptionById(String subscriptionID) {
        return subscriptionsRepository.findById(subscriptionID);
    }

    public List<UserTicketsSubscription> getUsersSubscriptions(long chatId) {
        return subscriptionsRepository.findByChatId(chatId);
    }


}
