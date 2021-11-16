package com.Azat9011.MTranstelegrambot.botapi.handlers.bussearch;

import com.Azat9011.MTranstelegrambot.botapi.BotState;
import com.Azat9011.MTranstelegrambot.botapi.handlers.InputMessageHandler;
import com.Azat9011.MTranstelegrambot.cache.UserDataCache;
import com.Azat9011.MTranstelegrambot.model.Bus;
import com.Azat9011.MTranstelegrambot.service.BusStationCodeService;
import com.Azat9011.MTranstelegrambot.service.BusTicketsGetInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.Azat9011.MTranstelegrambot.service.ReplyMessagesService;
import com.Azat9011.MTranstelegrambot.service.SendTicketsInfoService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Формирует  запрос на поиск поездов,
 * сохраняет и обрабатывает ввод пользователя.
 *
 * @author Azat9011
 */

@Slf4j
@Component
public class BusSearchHandler implements InputMessageHandler {
    private final UserDataCache userDataCache;
    private final BusTicketsGetInfoService busTicketsService;
    private final BusStationCodeService busStationCodeService;
    private final SendTicketsInfoService sendTicketsInfoService;
    private final ReplyMessagesService messagesService;

    public BusSearchHandler(UserDataCache userDataCache, BusTicketsGetInfoService busTicketsService,
                            BusStationCodeService busStationCodeService, ReplyMessagesService messagesService,
                            SendTicketsInfoService sendTicketsInfoService) {
        this.userDataCache = userDataCache;
        this.busTicketsService = busTicketsService;
        this.busStationCodeService = busStationCodeService;
        this.sendTicketsInfoService = sendTicketsInfoService;
        this.messagesService = messagesService;
    }

    @Override
    public SendMessage handle(Message message) {
        if (userDataCache.getUsersCurrentBotState(message.getFrom().getId()).equals(BotState.BUSES_SEARCH)) {
            userDataCache.setUsersCurrentBotState(message.getFrom().getId(), BotState.ASK_BUS_STATION_DEPART);
        }
        return processUsersInput(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.BUSES_SEARCH;
    }

    private SendMessage processUsersInput(Message inputMsg) {
        String usersAnswer = inputMsg.getText();
        int userId = inputMsg.getFrom().getId();
        long chatId = inputMsg.getChatId();
        SendMessage replyToUser = messagesService.getWarningReplyMessage(chatId, "reply.busSearch.tryAgain");
        BusSearchRequestData requestData = userDataCache.getUserBusSearchData(userId);

        BotState botState = userDataCache.getUsersCurrentBotState(userId);
        if (botState.equals(BotState.ASK_BUS_STATION_DEPART)) {
            replyToUser = messagesService.getReplyMessage(chatId, "reply.busSearch.enterStationDepart");
            userDataCache.setUsersCurrentBotState(userId, BotState.ASK_BUS_STATION_ARRIVAL);
        }

        if (botState.equals(BotState.ASK_BUS_STATION_ARRIVAL)) {

            int departureBusStationCode = busStationCodeService.getStationCode(usersAnswer);
            if (departureBusStationCode == -1) {
                return messagesService.getWarningReplyMessage(chatId, "reply.busSearch.stationNotFound");
            }

            requestData.setDepartureBusStationCode(departureBusStationCode);
            replyToUser = messagesService.getReplyMessage(chatId, "reply.busSearch.enterStationArrival");
            userDataCache.setUsersCurrentBotState(userId, BotState.ASK_DATE_DEPART);
        }

        if (botState.equals(BotState.ASK_DATE_DEPART)) {
            int arrivalBusStationCode = busStationCodeService.getStationCode(usersAnswer);
            if (arrivalBusStationCode == -1) {
                return messagesService.getWarningReplyMessage(chatId, "reply.busSearch.stationNotFound");
            }

            if (arrivalBusStationCode == requestData.getDepartureBusStationCode()) {
                return messagesService.getWarningReplyMessage(chatId, "reply.busSearch.stationsEquals");
            }

            requestData.setArrivalBusStationCode(arrivalBusStationCode);
            replyToUser = messagesService.getReplyMessage(chatId, "reply.busSearch.enterDateDepart");
            userDataCache.setUsersCurrentBotState(userId, BotState.DATE_DEPART_RECEIVED);
        }

        if (botState.equals(BotState.DATE_DEPART_RECEIVED)) {
            Date dateDepart;
            try {
                dateDepart = new SimpleDateFormat("dd.MM.yyyy").parse(usersAnswer);
            } catch (ParseException e) {
                return messagesService.getWarningReplyMessage(chatId, "reply.busSearch.wrongTimeFormat");
            }
            requestData.setDateDepart(dateDepart);

            List<Bus> busList = busTicketsService.getBusTicketsList(chatId, requestData.getDepartureBusStationCode(),
                    requestData.getArrivalBusStationCode(), dateDepart);
            if (busList.isEmpty()) {
                return messagesService.getReplyMessage(chatId, "reply.busSearch.busesNotFound");
            }

            sendTicketsInfoService.sendTicketsInfo(chatId, busList);
            userDataCache.setUsersCurrentBotState(userId, BotState.SHOW_MAIN_MENU);
            replyToUser = messagesService.getSuccessReplyMessage(chatId, "reply.busSearch.finishedOK");

        }
        userDataCache.saveBusSearchData(userId, requestData);
        return replyToUser;
    }


}



