package io.github.francescodonnini.csv.converters;

import com.opencsv.bean.AbstractBeanField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter extends AbstractBeanField<LocalDateTime, Void> {
    @Override
    protected Object convert(String s) {
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss]"));
    }
}
