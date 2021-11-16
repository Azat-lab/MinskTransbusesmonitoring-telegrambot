package com.Azat9011.MTranstelegrambot.botapi.handlers.callbackquery;

import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.model.UserTicketsSubscription;
import com.Azat9011.MTranstelegrambot.utils.Emojis;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import com.Azat9011.MTranstelegrambot.service.ParseQueryDataService;
import com.Azat9011.MTranstelegrambot.service.ReplyMessagesService;
import com.Azat9011.MTranstelegrambot.service.UserTicketsSubscriptionService;

import java.util.Optional;

/**
 * Обрабатывает запрос "Отписаться" от уведомлений по ценам.
 *
 * @author Azat9011
 */
@Component
public class UnsubscribeTicketsInfoQueryHandler implements CallbackQueryHandler {
    private static final CallbackQueryType HANDLER_QUERY_TYPE = CallbackQueryType.UNSUBSCRIBE;
    private UserTicketsSubscriptionService subscriptionService;
    private ParseQueryDataService parseService;
    private ReplyMessagesService messagesService;
    private MinskTransTelegramBot telegramBot;

    public UnsubscribeTicketsInfoQueryHandler(UserTicketsSubscriptionService subscriptionService,
                                              ParseQueryDataService parseService,
                                              ReplyMessagesService messagesService,
                                              @Lazy MinskTransTelegramBot telegramBot) {
        this.subscriptionService = subscriptionService;
        this.parseService = parseService;
        this.messagesService = messagesService;
        this.telegramBot = telegramBot;
    }

    @Override
    public CallbackQueryType getHandlerQueryType() {
        return HANDLER_QUERY_TYPE;
    }

    @Override
    public SendMessage handleCallbackQuery(CallbackQuery callbackQuery) {
        final long chatId = callbackQuery.getMessage().getChatId();

        final String subscriptionID = parseService.parseSubscriptionIdFromUnsubscribeQuery(callbackQuery);
        Optional<UserTicketsSubscription> optionalUserSubscription = subscriptionService.getUsersSubscriptionById(subscriptionID);
        if (optionalUserSubscription.isEmpty()) {
            return messagesService.getWarningReplyMessage(chatId, "reply.query.train.userHasNoSubscription");
        }

        UserTicketsSubscription userSubscription = optionalUserSubscription.get();
        subscriptionService.deleteUserSubscription(subscriptionID);

        telegramBot.sendChangedInlineButtonText(callbackQuery,
                String.format("%s %s", Emojis.SUCCESS_UNSUBSCRIBED, UserChatButtonStatus.UNSUBSCRIBED),
                CallbackQueryType.QUERY_PROCESSED.name());

        return messagesService.getReplyMessage(chatId, "reply.query.bus.unsubscribed", userSubscription.getBusNumber(),
                userSubscription.getDateDepart());
    }


}
