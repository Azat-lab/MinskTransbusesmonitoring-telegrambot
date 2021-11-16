package com.Azat9011.MTranstelegrambot.botapi.handlers.menu;

import com.Azat9011.MTranstelegrambot.botapi.BotState;
import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.botapi.handlers.InputMessageHandler;
import com.Azat9011.MTranstelegrambot.model.UserTicketsSubscription;
import com.Azat9011.MTranstelegrambot.utils.Emojis;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.Azat9011.MTranstelegrambot.botapi.handlers.callbackquery.CallbackQueryType;
import com.Azat9011.MTranstelegrambot.cache.UserDataCache;
import com.Azat9011.MTranstelegrambot.model.Car;
import com.Azat9011.MTranstelegrambot.service.ReplyMessagesService;
import com.Azat9011.MTranstelegrambot.service.UserTicketsSubscriptionService;

import java.util.List;

@Component
public class SubscriptionsMenuHandler implements InputMessageHandler {
    private final UserTicketsSubscriptionService subscribeService;
    private final MinskTransTelegramBot telegramBot;
    private final UserDataCache userDataCache;
    private final ReplyMessagesService messagesService;

    public SubscriptionsMenuHandler(UserTicketsSubscriptionService subscribeService,
                                    UserDataCache userDataCache,
                                    ReplyMessagesService messagesService,
                                    @Lazy MinskTransTelegramBot telegramBot) {
        this.subscribeService = subscribeService;
        this.messagesService = messagesService;
        this.userDataCache = userDataCache;
        this.telegramBot = telegramBot;
    }

    @Override
    public SendMessage handle(Message message) {
        List<UserTicketsSubscription> usersSubscriptions = subscribeService.getUsersSubscriptions(message.getChatId());

        if (usersSubscriptions.isEmpty()) {
            userDataCache.setUsersCurrentBotState(message.getFrom().getId(), BotState.SHOW_MAIN_MENU);
            return messagesService.getReplyMessage(message.getChatId(), "reply.subscriptions.userHasNoSubscriptions");
        }

        for (UserTicketsSubscription subscription : usersSubscriptions) {
            StringBuilder carsInfo = new StringBuilder();
            List<Car> cars = subscription.getSubscribedCars();

            for (Car car : cars) {
                carsInfo.append(messagesService.getReplyText("subscription.carsTicketsInfo",
                        car.getCarType(), car.getFreeSeats(), car.getMinimalPrice()));
            }

            String subscriptionInfo = messagesService.getReplyText("subscriptionMenu.trainTicketsInfo",
                    Emojis.BUS, subscription.getBusNumber(), subscription.getBusName(),
                    subscription.getBusStationDepart(), subscription.getTimeDepart(), subscription.getBusStationArrival(),
                    subscription.getTimeArrival(), Emojis.TIME_DEPART, subscription.getDateDepart(),
                    subscription.getDateArrival(), carsInfo);

            //Посылаем кнопку "Отписаться" с ID подписки
            String unsubscribeData = String.format("%s|%s", CallbackQueryType.UNSUBSCRIBE, subscription.getId());
            telegramBot.sendInlineKeyBoardMessage(message.getChatId(), subscriptionInfo, "Отписаться", unsubscribeData);
        }

        userDataCache.setUsersCurrentBotState(message.getFrom().getId(), BotState.SHOW_MAIN_MENU);

        return messagesService.getSuccessReplyMessage(message.getChatId(), "reply.subscriptions.listLoaded");
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SHOW_SUBSCRIPTIONS_MENU;
    }


}
