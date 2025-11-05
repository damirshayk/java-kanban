package com.yandex.app.http.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * Адаптер для сериализации и десериализации Duration.
 * В JSON значение представляется числом минут. При чтении null или пустое значение
 * преобразуются в null, иначе создаётся Duration.ofMinutes().
 */
public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        // Сериализуем Duration как количество минут
        return new JsonPrimitive(src.toMinutes());
    }

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        try {
            // Пробуем прочитать значение как long
            long minutes = json.getAsLong();
            // Если значение отрицательное, выбрасываем исключение
            return Duration.ofMinutes(minutes);
        } catch (NumberFormatException e) {
            throw new JsonParseException("Некорректное значение продолжительности", e);
        }
    }
}