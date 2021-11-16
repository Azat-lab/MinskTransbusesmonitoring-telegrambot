package com.Azat9011.MTranstelegrambot.botapi.handlers.callbackquery;

import lombok.AllArgsConstructor;

/**Статус кнопки клавиатуры пользователя.
 *
 * @author Azat9011
 */
@AllArgsConstructor
public enum UserChatButtonStatus {
    SUBSCRIBED("Подписался"), UNSUBSCRIBED("Отписался");

    private String buttonStatus;

    @Override
    public String toString() {
        return buttonStatus;
    }
}

