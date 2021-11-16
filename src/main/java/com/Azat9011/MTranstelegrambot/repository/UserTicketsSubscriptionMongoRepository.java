package com.Azat9011.MTranstelegrambot.repository;

import com.Azat9011.MTranstelegrambot.model.UserTicketsSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Sergei Viacheslaev
 */
@Repository
public interface UserTicketsSubscriptionMongoRepository extends MongoRepository<UserTicketsSubscription, String> {
    List<UserTicketsSubscription> findByChatId(long chatId);

    List<UserTicketsSubscription> findByChatIdAndBusNumberAndDateDepart(long chatId, String busNumber, String dateDepart);
}
