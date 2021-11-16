package com.Azat9011.MTranstelegrambot.botapi;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.Azat9011.MTranstelegrambot.botapi.handlers.InputMessageHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines message handlers for each state.
 *
 * @author Azat9011
 */
@Component
public class BotStateContext {
    private Map<BotState, InputMessageHandler> messageHandlers = new HashMap<>();

    public BotStateContext(List<InputMessageHandler> messageHandlers) {
        messageHandlers.forEach(handler -> this.messageHandlers.put(handler.getHandlerName(), handler));
    }

    public SendMessage processInputMessage(BotState currentState, Message message) {
        InputMessageHandler currentMessageHandler = findMessageHandler(currentState);
        return currentMessageHandler.handle(message);
    }

    private InputMessageHandler findMessageHandler(BotState currentState) {
        if (isBusSearchState(currentState)) {
            return messageHandlers.get(BotState.BUSES_SEARCH);
        }

        if (isBusStationSearchState(currentState)) {
            return messageHandlers.get(BotState.BUS_STATIONS_SEARCH);
        }

        return messageHandlers.get(currentState);
    }

    private boolean isBusSearchState(BotState currentState) {
        switch (currentState) {
            case BUSES_SEARCH:
            case ASK_DATE_DEPART:
            case DATE_DEPART_RECEIVED:
            case ASK_BUS_STATION_ARRIVAL:
            case ASK_BUS_STATION_DEPART:
            case BUSES_SEARCH_STARTED:
            case BUS_INFO_RESPONCE_AWAITING:
            case BUSES_SEARCH_FINISH:
                return true;
            default:
                return false;
        }
    }

    private boolean isBusStationSearchState(BotState currentState) {
        switch (currentState) {
            case SHOW_BUS_STATIONS_BOOK_MENU:
            case ASK_BUS_STATION_NAMEPART:
            case BUS_STATION_NAMEPART_RECEIVED:
            case BUS_STATIONS_SEARCH:
                return true;
            default:
                return false;
        }
    }

}





