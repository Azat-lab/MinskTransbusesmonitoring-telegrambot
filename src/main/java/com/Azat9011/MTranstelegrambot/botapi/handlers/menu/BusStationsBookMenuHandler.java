package com.Azat9011.MTranstelegrambot.botapi.handlers.menu;

import com.Azat9011.MTranstelegrambot.botapi.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.Azat9011.MTranstelegrambot.botapi.handlers.InputMessageHandler;
import com.Azat9011.MTranstelegrambot.cache.UserDataCache;
import com.Azat9011.MTranstelegrambot.service.BusStationBookService;
import com.Azat9011.MTranstelegrambot.service.ReplyMessagesService;



@Component
public class BusStationsBookMenuHandler implements InputMessageHandler {
    private final ReplyMessagesService messagesService;
    private final BusStationBookService busStationsBookService;
    private final UserDataCache userDataCache;

    public BusStationsBookMenuHandler(ReplyMessagesService messagesService, BusStationBookService busStationsBookService,
                                      UserDataCache userDataCache) {
        this.messagesService = messagesService;
        this.busStationsBookService = busStationsBookService;
        this.userDataCache = userDataCache;
    }

    @Override
    public SendMessage handle(Message message) {
        if (userDataCache.getUsersCurrentBotState(message.getFrom().getId()).equals(BotState.BUS_STATIONS_SEARCH)) {
            userDataCache.setUsersCurrentBotState(message.getFrom().getId(), BotState.ASK_BUS_STATION_NAMEPART);
            return messagesService.getReplyMessage(message.getChatId(), "reply.stationBookMenu.searchHelpMessage");
        }
        return processUsersInput(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.BUS_STATIONS_SEARCH;
    }

    private SendMessage processUsersInput(Message inputMsg) {
        String usersInput = inputMsg.getText();
        long chatId = inputMsg.getChatId();
        Integer userId = inputMsg.getFrom().getId();

        SendMessage replyToUser = messagesService.getWarningReplyMessage(chatId, "reply.query.failed");
        BotState botState = userDataCache.getUsersCurrentBotState(userId);

        if (botState.equals(BotState.ASK_BUS_STATION_NAMEPART)) {
            replyToUser = busStationsBookService.processBusStationNamePart(chatId, usersInput);
            userDataCache.setUsersCurrentBotState(userId, BotState.ASK_BUS_STATION_NAMEPART);
        }

        return replyToUser;
    }
}
