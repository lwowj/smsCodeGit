package com.hero.sms.common.exception;

public class ServiceException extends Exception {

    private static final long serialVersionUID = 7026452581404169688L;

    public ServiceException(String message) {
        super(message);
    }
}
