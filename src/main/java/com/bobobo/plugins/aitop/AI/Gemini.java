package com.bobobo.plugins.aitop.AI;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class Gemini {

    private final String apiKey;
    private final String model;
    private final OkHttpClient httpClient;

    public Gemini(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String getResponse(Component messageComponent, String systemRole) throws IOException {
        // Преобразуем Component в текст
        String playerMessage = PlainTextComponentSerializer.plainText().serialize(messageComponent);

        // Создаем массив сообщений для разговора
        JSONArray conversation = new JSONArray();

        // Добавляем системное сообщение
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("content", systemRole);
        conversation.put(systemMessage);

        // Добавляем сообщение игрока
        JSONObject playerMessageObj = new JSONObject();
        playerMessageObj.put("content", playerMessage);
        conversation.put(playerMessageObj);

        // Формируем JSON-запрос
        JSONObject json = new JSONObject();
        JSONArray parts = new JSONArray();

        for (int i = 0; i < conversation.length(); i++) {
            JSONObject message = conversation.getJSONObject(i);
            parts.put(new JSONObject().put("text", message.getString("content")));
        }

        JSONObject content = new JSONObject().put("parts", parts);
        JSONArray contents = new JSONArray().put(content);
        json.put("contents", contents);

        //System.out.println("Отправляемый JSON-запрос: " + json.toString(2));

        // Формируем URL с использованием модели из конфига
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        // Формируем HTTP-запрос
        RequestBody body = RequestBody.create(
                json.toString(), MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        int retries = 3;
        while (retries > 0) {
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response: " + response);
                }
                return parseGeminiResponse(response.body().string());

            } catch (SocketTimeoutException e) {
                retries--;
                if (retries == 0) {
                    throw new IOException("Произошёл таймаут при подключении к Gemini.", e);
                }
            }
        }
        return null;
    }

    private String parseGeminiResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            if (jsonObject.has("error")) {
                JSONObject error = jsonObject.getJSONObject("error");
                String errorMessage = error.getString("message");
                return "Ошибка от API: " + errorMessage;
            }

            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            String text;
            if (firstCandidate.has("output")) {
                text = firstCandidate.getString("output");
            } else if (firstCandidate.has("content")) {
                JSONArray parts = firstCandidate.getJSONObject("content").getJSONArray("parts");
                text = parts.getJSONObject(0).getString("text");
            } else {
                return "Ошибка: Поле 'output' или 'content' не найдено в первом кандидате.";
            }

            return text.trim();
        } catch (Exception e) {
            return "Ошибка при разборе ответа: " + e.getMessage();
        }
    }

}
