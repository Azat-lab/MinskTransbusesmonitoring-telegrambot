package com.Azat9011.MTranstelegrambot.botapi.handlers.menu;

import com.Azat9011.MTranstelegrambot.botapi.BotState;
import com.Azat9011.MTranstelegrambot.utils.Emojis;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.Azat9011.MTranstelegrambot.botapi.handlers.InputMessageHandler;
import com.Azat9011.MTranstelegrambot.service.MainMenuService;
import com.Azat9011.MTranstelegrambot.service.ReplyMessagesService;


@Component
public class HelpMenuHandler implements InputMessageHandler {
    private final MainMenuService mainMenuService;
    private final ReplyMessagesService messagesService;

    public HelpMenuHandler(MainMenuService mainMenuService, ReplyMessagesService messagesService) {
        this.mainMenuService = mainMenuService;
        this.messagesService = messagesService;
    }

    @Override
    public SendMessage handle(Message message) {
        return mainMenuService.getMainMenuMessage(message.getChatId(),
                messagesService.getEmojiReplyText("reply.helpMenu.welcomeMessage", Emojis.HELP_MENU_WELCOME));
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SHOW_HELP_MENU;
    }
}
