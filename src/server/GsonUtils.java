package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import handler.DurationAdapter;
import handler.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

public class GsonUtils {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public static Gson getGson() {
        return GSON;
    }
}
