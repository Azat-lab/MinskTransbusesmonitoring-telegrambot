package com.Azat9011.MTranstelegrambot.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Подписка пользователя на конкретный автобус
 *
 * @author Azat9011
 */
@Getter
@Setter
@Document(collection = "usersTicketsSubscription")
@ToString
public class UserTicketsSubscription {
    @Id
    private String id;

    private long chatId;

    private String busNumber;

    private String busName;

    private String busStationDepart;

    private String busStationArrival;

    private String dateDepart;

    private String dateArrival;

    private String timeDepart;

    private String timeArrival;

    private List<Car> subscribedCars;

    public UserTicketsSubscription(long chatId, String busNumber, String busName, String busStationDepart,
                                   String busStationArrival, String dateDepart, String dateArrival, String timeDepart,
                                   String timeArrival, List<Car> subscribedCars) {
        this.chatId = chatId;
        this.busNumber = busNumber;
        this.busName = this.busName;
        this.busStationDepart = this.busStationDepart;
        this.busStationArrival = this.busStationArrival;
        this.dateDepart = dateDepart;
        this.dateArrival = dateArrival;
        this.timeDepart = timeDepart;
        this.timeArrival = timeArrival;
        this.subscribedCars = subscribedCars;
    }
}
