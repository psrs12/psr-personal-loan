package com.personalloan.applicationmanagement.infrastructure.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

public class SensitiveDataMaskingConverter extends MessageConverter {

    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{9}\\b");
    private static final Pattern SSN_DASHED_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        return mask(message);
    }

    public String maskForTest(String message) {
        return mask(message);
    }

    private String mask(String message) {
        message = SSN_DASHED_PATTERN.matcher(message).replaceAll("***-**-****");
        message = SSN_PATTERN.matcher(message).replaceAll("*********");
        return message;
    }
}
