package com.Azat9011.MTranstelegrambot.appconfig;

import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.botapi.TelegramFacade;
import com.Azat9011.MTranstelegrambot.botconfig.MinskTransTelegramBotConfig;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;


@Configuration
public class AppConfig {
    private final MinskTransTelegramBotConfig botConfig;

    public AppConfig(MinskTransTelegramBotConfig minskTransTelegramBotConfig) {
        this.botConfig = minskTransTelegramBotConfig;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public MinskTransTelegramBot MinskTransTelegramBot(TelegramFacade telegramFacade) {
        DefaultBotOptions options = ApiContext
                .getInstance(DefaultBotOptions.class);

        options.setProxyHost(botConfig.getProxyHost());
        options.setProxyPort(botConfig.getProxyPort());
        options.setProxyType(botConfig.getProxyType());

        MinskTransTelegramBot minskTransTelegramBot = new MinskTransTelegramBot(options, telegramFacade);
        minskTransTelegramBot.setBotUsername(botConfig.getUserName());
        minskTransTelegramBot.setBotToken(botConfig.getBotToken());
        minskTransTelegramBot.setBotPath(botConfig.getWebHookPath());

        return minskTransTelegramBot;
    }
}
