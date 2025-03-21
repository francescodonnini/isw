package io.github.francescodonnini.csv.converters;

import com.opencsv.bean.AbstractBeanField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends AbstractBeanField<LocalDate, Void> {
    @Override
    protected Object convert(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
