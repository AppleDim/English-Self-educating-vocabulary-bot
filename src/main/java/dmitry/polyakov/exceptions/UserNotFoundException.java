package dmitry.polyakov.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserNotFoundException extends Exception{
    public UserNotFoundException(Long chatId) {
        super(String.valueOf(chatId));
        log.warn("Error occurred while finding the user with id = " + chatId);
    }
}
