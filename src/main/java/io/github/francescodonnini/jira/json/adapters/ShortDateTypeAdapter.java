package io.github.francescodonnini.jira.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShortDateTypeAdapter extends TypeAdapter<LocalDate> {
    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
        jsonWriter.jsonValue(String.format("\"%s\"", localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)));
    }

    @Override
    public LocalDate read(JsonReader jsonReader) throws IOException {
        return LocalDate.parse(jsonReader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
