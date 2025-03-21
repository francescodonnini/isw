package io.github.francescodonnini.jira.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LongDateTypeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxxx");

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime dateTime) throws IOException {
        jsonWriter.jsonValue(String.format("\"%s\"", dateTime.atOffset(ZoneOffset.UTC).format(formatter)));
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        return LocalDateTime.parse(jsonReader.nextString(), formatter);
    }
}
