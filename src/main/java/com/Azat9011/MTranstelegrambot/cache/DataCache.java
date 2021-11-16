package com.Azat9011.MTranstelegrambot.cache;

import com.Azat9011.MTranstelegrambot.botapi.BotState;
import com.Azat9011.MTranstelegrambot.botapi.handlers.bussearch.BusSearchRequestData;
import com.Azat9011.MTranstelegrambot.model.Bus;

import java.util.List;


public interface DataCache {
    void setUsersCurrentBotState(int userId, BotState botState);

    BotState getUsersCurrentBotState(int userId);

    void saveBusSearchData(int userId, BusSearchRequestData busSearchData);

    BusSearchRequestData getUserBusSearchData(int userId);

    void saveSearchFoundedBuses(long chatId, List<Bus> foundBuses);

    List<Bus> getSearchFoundedBuses(long chatId);
}
