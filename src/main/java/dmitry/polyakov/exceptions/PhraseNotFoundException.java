package dmitry.polyakov.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j

public class PhraseNotFoundException extends Exception{
    public PhraseNotFoundException (Long id) {
        super(String.valueOf(id));
        log.warn("Error occurred while finding for phrase with id = " + id);
    }
}
