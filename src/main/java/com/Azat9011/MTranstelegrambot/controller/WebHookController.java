package com.Azat9011.MTranstelegrambot.controller;

import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.model.UserTicketsSubscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.Azat9011.MTranstelegrambot.service.UserTicketsSubscriptionService;

import java.util.List;


@Slf4j
@RestController
public class WebHookController {
    private final MinskTransTelegramBot telegramBot;
    private final UserTicketsSubscriptionService subscriptionService;

    public WebHookController(MinskTransTelegramBot telegramBot, UserTicketsSubscriptionService subscriptionService) {
        this.telegramBot = telegramBot;
        this.subscriptionService = subscriptionService;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }

    @GetMapping(value = "/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserTicketsSubscription> index() {
        List<UserTicketsSubscription> userTicketsSubscriptions = subscriptionService.getAllSubscriptions();
        return userTicketsSubscriptions;
    }

}
