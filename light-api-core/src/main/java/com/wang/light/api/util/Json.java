package com.wang.light.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wang.light.api.exception.LightApiException;

import java.io.IOException;

/**
 * 全框架共享的 Jackson 入口（HTTP 响应映射、断言助手、结果推送共用一个 MAPPER）。
 */
public final class Json {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Json() {
    }

    public static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new LightApiException("不是合法的 JSON: " + snippet(json), e);
        }
    }

    public static <T> T parseObject(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new LightApiException("JSON 反序列化为 " + type.getSimpleName() + " 失败: "
                    + snippet(json), e);
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new LightApiException("JSON 序列化失败: " + value, e);
        }
    }

    public static <T> T convert(Object from, Class<T> to) {
        return MAPPER.convertValue(from, to);
    }

    public static String snippet(String text) {
        if (text == null) {
            return "null";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        return compact.length() > 300 ? compact.substring(0, 300) + "...(截断)" : compact;
    }
}
