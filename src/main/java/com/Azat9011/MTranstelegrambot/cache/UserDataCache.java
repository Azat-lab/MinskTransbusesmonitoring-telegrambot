package com.Azat9011.MTranstelegrambot.cache;

import com.Azat9011.MTranstelegrambot.botapi.BotState;
import com.Azat9011.MTranstelegrambot.botapi.handlers.bussearch.BusSearchRequestData;
import com.Azat9011.MTranstelegrambot.model.Bus;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * In-memory cache.
 *
 * usersBotStates: user_id and user's bot state
 * trainSearchUsersData: used_id and TrainSearchData
 * searchFoundedTrains: chat_id and List of founded trains.
 *
 * @author Azat9011
 */
@Service
public class UserDataCache implements DataCache {
    private Map<Integer, BotState> usersBotStates = new HashMap<>();
    private Map<Integer, BusSearchRequestData> busSearchUsersData = new HashMap<>();
    private Map<Long, List<Bus>> searchFoundedBuses = new HashMap<>();

    @Override
    public void setUsersCurrentBotState(int userId, BotState botState) {
        usersBotStates.put(userId, botState);
    }

    @Override
    public BotState getUsersCurrentBotState(int userId) {
        BotState botState = usersBotStates.get(userId);
        if (botState == null) {
            botState = BotState.SHOW_MAIN_MENU;
        }

        return botState;
    }

    @Override
    public void saveBusSearchData(int userId, BusSearchRequestData busSearchData) {
        busSearchUsersData.put(userId, busSearchData);
    }

    @Override
    public BusSearchRequestData getUserBusSearchData(int userId) {
        BusSearchRequestData busSearchData = busSearchUsersData.get(userId);
        if (busSearchData == null) {
            busSearchData = new BusSearchRequestData();
        }

        return busSearchData;
    }

    @Override
    public void saveSearchFoundedBuses(long chatId, List<Bus> foundBuses) {
        searchFoundedBuses.put(chatId, foundBuses);
    }

    @Override
    public List<Bus> getSearchFoundedBuses(long chatId) {
        List<Bus> foundedBuses = searchFoundedBuses.get(chatId);

        return Objects.isNull(foundedBuses) ? Collections.emptyList() : foundedBuses;
    }

}
