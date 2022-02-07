package com.epam.adminservice.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDeserializer extends StdDeserializer<Date> {

    private static DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public DateDeserializer() {
        this(null);
    }

    public DateDeserializer(Class<?> c) {
        super(c);
    }

    @Override
    public Date deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext
    ) throws IOException, JsonProcessingException {
        return null;
    }
}
