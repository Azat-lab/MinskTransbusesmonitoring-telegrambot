package com.Azat9011.MTranstelegrambot.botapi.handlers.menu;

import com.Azat9011.MTranstelegrambot.botapi.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.Azat9011.MTranstelegrambot.botapi.handlers.InputMessageHandler;
import com.Azat9011.MTranstelegrambot.service.MainMenuService;
import com.Azat9011.MTranstelegrambot.service.ReplyMessagesService;

@Component
public class MainMenuHandler implements InputMessageHandler {
    private final ReplyMessagesService messagesService;
    private final MainMenuService mainMenuService;

    public MainMenuHandler(ReplyMessagesService messagesService, MainMenuService mainMenuService) {
        this.messagesService = messagesService;
        this.mainMenuService = mainMenuService;
    }

    @Override
    public SendMessage handle(Message message) {
        return mainMenuService.getMainMenuMessage(message.getChatId(), messagesService.getReplyText("reply.mainMenu.welcomeMessage"));
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SHOW_MAIN_MENU;
    }


}
