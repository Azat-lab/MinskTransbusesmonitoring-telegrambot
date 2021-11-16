package com.Azat9011.MTranstelegrambot.botapi.handlers.callbackquery;

import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.cache.UserDataCache;
import com.Azat9011.MTranstelegrambot.model.Bus;
import com.Azat9011.MTranstelegrambot.model.Car;
import com.Azat9011.MTranstelegrambot.model.UserTicketsSubscription;
import com.Azat9011.MTranstelegrambot.utils.Emojis;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import com.Azat9011.MTranstelegrambot.service.ParseQueryDataService;
import com.Azat9011.MTranstelegrambot.service.ReplyMessagesService;
import com.Azat9011.MTranstelegrambot.service.UserTicketsSubscriptionService;

import java.util.List;
import java.util.Optional;

/**
 * Обрабатывает запрос "Подписаться" на уведомления по ценам.
 *
 * @author Azat9011
 */
@Component
public class SubscribeTicketsInfoQueryHandler implements CallbackQueryHandler {
    private static final CallbackQueryType HANDLER_QUERY_TYPE = CallbackQueryType.SUBSCRIBE;
    private final UserTicketsSubscriptionService subscriptionService;
    private final ParseQueryDataService parseService;
    private final ReplyMessagesService messagesService;
    private final UserDataCache userDataCache;
    private final MinskTransTelegramBot telegramBot;


    public SubscribeTicketsInfoQueryHandler(UserTicketsSubscriptionService subscribeService,
                                            ParseQueryDataService parseService,
                                            ReplyMessagesService messagesService,
                                            UserDataCache userDataCache,
                                            @Lazy MinskTransTelegramBot telegramBot) {
        this.subscriptionService = subscribeService;
        this.parseService = parseService;
        this.messagesService = messagesService;
        this.userDataCache = userDataCache;
        this.telegramBot = telegramBot;
    }


    @Override
    public CallbackQueryType getHandlerQueryType() {
        return HANDLER_QUERY_TYPE;
    }

    @Override
    public SendMessage handleCallbackQuery(CallbackQuery callbackQuery) {
        final long chatId = callbackQuery.getMessage().getChatId();
        final String busNumber = parseService.parseBusNumberFromSubscribeQuery(callbackQuery);
        final String dateDepart = parseService.parseDateDepartFromSubscribeQuery(callbackQuery);

        Optional<UserTicketsSubscription> userSubscriptionOptional = parseQueryData(callbackQuery);
        if (userSubscriptionOptional.isEmpty()) {
            return messagesService.getWarningReplyMessage(chatId, "reply.query.searchAgain");
        }

        UserTicketsSubscription userSubscription = userSubscriptionOptional.get();
        if (subscriptionService.hasTicketsSubscription(userSubscription)) {
            return messagesService.getWarningReplyMessage(chatId, "reply.query.bus.userHasSubscription");
        }

        subscriptionService.saveUserSubscription(userSubscription);

        telegramBot.sendChangedInlineButtonText(callbackQuery,
                String.format("%s %s", Emojis.SUCCESS_SUBSCRIBED, UserChatButtonStatus.SUBSCRIBED),
                CallbackQueryType.QUERY_PROCESSED.name());

        return messagesService.getReplyMessage(chatId, "reply.query.bus.subscribed", busNumber, dateDepart);

    }


    private Optional<UserTicketsSubscription> parseQueryData(CallbackQuery usersQuery) {
        List<Bus> foundedBuses = userDataCache.getSearchFoundedBuses(usersQuery.getMessage().getChatId());
        final long chatId = usersQuery.getMessage().getChatId();

        final String busNumber = parseService.parseBusNumberFromSubscribeQuery(usersQuery);
        final String dateDepart = parseService.parseDateDepartFromSubscribeQuery(usersQuery);

        Optional<Bus> queriedBusOptional = foundedBuses.stream().
                filter(bus -> bus.getNumber().equals(busNumber) && bus.getDateDepart().equals(dateDepart)).
                findFirst();

        if (queriedBusOptional.isEmpty()) {
            return Optional.empty();
        }

        Bus queriedBus = queriedBusOptional.get();
        final String busName = queriedBus.getBrand();
        final String stationDepart = queriedBus.getBusStationDepart();
        final String stationArrival = queriedBus.getBusStationArrival();
        final String dateArrival = queriedBus.getDateArrival();
        final String timeDepart = queriedBus.getTimeDepart();
        final String timeArrival = queriedBus.getTimeArrival();
        final List<Car> availableCars = queriedBus.getAvailableCars();

        return Optional.of(new UserTicketsSubscription(chatId, busNumber, busName, stationDepart, stationArrival,
                dateDepart, dateArrival, timeDepart, timeArrival, availableCars));
    }


}
