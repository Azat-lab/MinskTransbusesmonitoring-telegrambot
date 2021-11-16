package com.Azat9011.MTranstelegrambot.service;

import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.model.Bus;
import com.Azat9011.MTranstelegrambot.model.Car;
import com.Azat9011.MTranstelegrambot.model.UserTicketsSubscription;
import com.Azat9011.MTranstelegrambot.utils.Emojis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Сервис уведомлений,
 * рассылка информации об изменении цен на билеты.
 * <p>
 * Работает с подписками пользователей.
 *
 * @author Azat9011
 */
@Slf4j
@Service
public class UserSubscriptionProcessService {
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final UserTicketsSubscriptionService subscriptionService;
    private final BusTicketsGetInfoService busTicketsGetInfoService;
    private final BusStationCodeService busStationCodeService;
    private final CarsProcessingService carsProcessingService;
    private final ReplyMessagesService messagesService;
    private final MinskTransTelegramBot telegramBot;

    public UserSubscriptionProcessService(UserTicketsSubscriptionService subscriptionService,
                                          BusTicketsGetInfoService busTicketsGetInfoService,
                                          BusStationCodeService busStationCodeService,
                                          CarsProcessingService carsProcessingService,
                                          ReplyMessagesService messagesService,
                                          @Lazy MinskTransTelegramBot telegramBot) {
        this.subscriptionService = subscriptionService;
        this.busTicketsGetInfoService = busTicketsGetInfoService;
        this.busStationCodeService = busStationCodeService;
        this.carsProcessingService = carsProcessingService;
        this.messagesService = messagesService;
        this.telegramBot = telegramBot;
    }


    /**
     * Периодически смотрит за обновлением цен
     * по всей базе подписок.
     */
    @Scheduled(fixedRateString = "${subscriptions.processPeriod}")
    public void processAllUsersSubscriptions() {
        log.info("Выполняю обработку подписок пользователей.");
        subscriptionService.getAllSubscriptions().forEach(this::processSubscription);
        log.info("Завершил обработку подписок пользователей.");
    }

    /**
     * Получает актуальные данные по билетам для текущей подписки,
     * если цена изменилась сохраняет последнюю и уведомляет клиента.
     */
    private void processSubscription(UserTicketsSubscription subscription) {
        List<Bus> actualBuses = getActualBuses(subscription);

        if (isBusHasDeparted(actualBuses, subscription)) {
            subscriptionService.deleteUserSubscription(subscription.getId());
            telegramBot.sendMessage(messagesService.getReplyMessage(subscription.getChatId(), "subscription.busHasDeparted",
                    Emojis.NOTIFICATION_BELL, subscription.getBusNumber(), subscription.getBusName(),
                    subscription.getDateDepart(), subscription.getTimeDepart()));
            return;
        }

        actualBuses.forEach(actualBus -> {

            if (actualBus.getNumber().equals(subscription.getBusNumber()) &&
                    actualBus.getDateDepart().equals(subscription.getDateDepart())) {

                List<Car> actualCarsWithMinimumPrice = carsProcessingService.filterCarsWithMinimumPrice(actualBus.getAvailableCars());

                Map<String, List<Car>> updatedCarsNotification = processCarsLists(subscription.getSubscribedCars(),
                        actualCarsWithMinimumPrice);

                if (!updatedCarsNotification.isEmpty()) {
                    String priceChangesMessage = updatedCarsNotification.keySet().iterator().next();
                    List<Car> updatedCars = updatedCarsNotification.get(priceChangesMessage);

                    subscription.setSubscribedCars(updatedCars);
                    subscriptionService.saveUserSubscription(subscription);
                    sendUserNotification(subscription, priceChangesMessage, updatedCars);
                }
            }
        });


    }

    private List<Bus> getActualBuses(UserTicketsSubscription subscription) {
        int busStationDepartCode = busStationCodeService.getStationCode(subscription.getBusStationDepart());
        int busStationArrivalCode = busStationCodeService.getStationCode(subscription.getBusStationArrival());
        Date dateDeparture = parseDateDeparture(subscription.getDateDepart());

        return busTicketsGetInfoService.getBusTicketsList(subscription.getChatId(),
                busStationDepartCode, busStationArrivalCode, dateDeparture);
    }

    private boolean isBusHasDeparted(List<Bus> actualBuses, UserTicketsSubscription subscription) {
        return actualBuses.stream().map(Bus::getNumber).noneMatch(Predicate.isEqual(subscription.getBusNumber()));
    }

    /**
     * Возвращает Мапу: Строку-уведомление и список обновленных цен в вагонах подписки.
     * Если цены не менялись, вернет пустую мапу.
     */
    private Map<String, List<Car>> processCarsLists(List<Car> subscribedCars, List<Car> actualCars) {
        StringBuilder notificationMessage = new StringBuilder();

        for (Car subscribedCar : subscribedCars) {

            for (Car actualCar : actualCars) {
                if (actualCar.getCarType().equals(subscribedCar.getCarType())) {

                    if (actualCar.getMinimalPrice() > subscribedCar.getMinimalPrice()) {
                        notificationMessage.append(messagesService.getReplyText("subscription.PriceUp", Emojis.NOTIFICATION_PRICE_UP,
                                actualCar.getCarType(), subscribedCar.getMinimalPrice(), actualCar.getMinimalPrice()));
                        subscribedCar.setMinimalPrice(actualCar.getMinimalPrice());
                    } else if (actualCar.getMinimalPrice() < subscribedCar.getMinimalPrice()) {
                        notificationMessage.append(messagesService.getReplyText("subscription.PriceDown", Emojis.NOTIFICATION_PRICE_DOWN,
                                actualCar.getCarType(), subscribedCar.getMinimalPrice(), actualCar.getMinimalPrice()));
                        subscribedCar.setMinimalPrice(actualCar.getMinimalPrice());
                    }
                    subscribedCar.setFreeSeats(actualCar.getFreeSeats());
                }
            }
        }

        return notificationMessage.length() == 0 ? Collections.emptyMap() : Collections.singletonMap(notificationMessage.toString(), subscribedCars);
    }

    private void sendUserNotification(UserTicketsSubscription subscription, String priceChangeMessage, List<Car> updatedCars) {
        StringBuilder notificationMessage = new StringBuilder(messagesService.getReplyText("subscription.trainTicketsPriceChanges",
                Emojis.NOTIFICATION_BELL, subscription.getBusNumber(), subscription.getBusName(),
                subscription.getDateDepart(), subscription.getTimeDepart(), subscription.getBusStationArrival())).append(priceChangeMessage);

        notificationMessage.append(messagesService.getReplyText("subscription.lastTicketPrices"));

        for (Car car : updatedCars) {
            notificationMessage.append(messagesService.getReplyText("subscription.carsTicketsInfo",
                    car.getCarType(), car.getFreeSeats(), car.getMinimalPrice()));
        }

        telegramBot.sendMessage(subscription.getChatId(), notificationMessage.toString());
    }


    private Date parseDateDeparture(String dateDeparture) {
        Date dateDepart = null;
        try {
            dateDepart = DATE_FORMAT.parse(dateDeparture);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateDepart;
    }


}
