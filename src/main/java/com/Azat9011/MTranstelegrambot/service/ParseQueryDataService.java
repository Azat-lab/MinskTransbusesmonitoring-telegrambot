package com.Azat9011.MTranstelegrambot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

/**
 * Парсит данные из запрос от клавиатуры.
 */
@Service
public class ParseQueryDataService {

    public String parseBusNumberFromSubscribeQuery(CallbackQuery callbackQuery) {

        return callbackQuery.getData().split("\\|")[1];
    }

    public String parseDateDepartFromSubscribeQuery(CallbackQuery callbackQuery) {
        return callbackQuery.getData().split("\\|")[2];
    }

    public String parseSubscriptionIdFromUnsubscribeQuery(CallbackQuery callbackQuery) {
        return callbackQuery.getData().split("\\|")[1];
    }
}