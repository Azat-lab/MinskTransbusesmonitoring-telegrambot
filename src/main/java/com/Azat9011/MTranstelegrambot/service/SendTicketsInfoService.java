package com.Azat9011.MTranstelegrambot.service;

import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.botapi.handlers.callbackquery.CallbackQueryType;
import com.Azat9011.MTranstelegrambot.cache.UserDataCache;
import com.Azat9011.MTranstelegrambot.model.Bus;
import com.Azat9011.MTranstelegrambot.model.Car;
import com.Azat9011.MTranstelegrambot.utils.Emojis;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Отправляет в чат данные по поездам,
 * после выполнения команды "Поиск поездов" пользователем.
 *
 * @author Azat9011
 */
@Service
public class SendTicketsInfoService {
    private MinskTransTelegramBot telegramBot;
    private CarsProcessingService carsProcessingService;
    private UserDataCache userDataCache;
    private ReplyMessagesService messagesService;

    public SendTicketsInfoService(CarsProcessingService carsProcessingService,
                                  UserDataCache userDataCache,
                                  ReplyMessagesService messagesService,
                                  @Lazy MinskTransTelegramBot telegramBot) {
        this.carsProcessingService = carsProcessingService;
        this.userDataCache = userDataCache;
        this.messagesService = messagesService;
        this.telegramBot = telegramBot;
    }


    public void sendTicketsInfo(long chatId, List<Bus> busList) {
        for (Bus bus : busList) {
            StringBuilder carsInfo = new StringBuilder();
            List<Car> carsWithMinimalPrice = carsProcessingService.filterCarsWithMinimumPrice(bus.getAvailableCars());
            bus.setAvailableCars(carsWithMinimalPrice);

            for (Car car : carsWithMinimalPrice) {
                carsInfo.append(messagesService.getReplyText("subscription.carsTicketsInfo",
                        car.getCarType(), car.getFreeSeats(), car.getMinimalPrice()));
            }

            String busTicketsInfoMessage = messagesService.getReplyText("reply.busSearch.busInfo",
                    Emojis.BUS, bus.getNumber(), bus.getBrand(), bus.getBusStationDepart(), bus.getDateDepart(), bus.getTimeDepart(),
                    bus.getBusStationArrival(), bus.getDateArrival(), bus.getTimeArrival(),
                    Emojis.TIME_IN_WAY, bus.getTimeInWay(), carsInfo);

            String busInfoData = String.format("%s|%s|%s", CallbackQueryType.SUBSCRIBE,
                    bus.getNumber(), bus.getDateDepart());

            telegramBot.sendInlineKeyBoardMessage(chatId, busTicketsInfoMessage, "Подписаться", busInfoData);
        }
        userDataCache.saveSearchFoundedBuses(chatId, busList);
    }


}
