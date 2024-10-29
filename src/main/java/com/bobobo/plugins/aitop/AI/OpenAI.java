package com.bobobo.plugins.aitop.AI;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OpenAI {

    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final String systemRole;
    private final OkHttpClient httpClient;

    public OpenAI(String apiKey, String model, int maxTokens, String systemRole) {
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
        this.systemRole = systemRole;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String askQuestion(String prompt) throws IOException {
        // Создаём JSON с запросом
        JSONObject json = new JSONObject();
        json.put("model", model);
        json.put("max_tokens", maxTokens);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemRole));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));

        json.put("messages", messages);

        // Формируем HTTP-запрос
        RequestBody body = RequestBody.create(
                json.toString(), MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            if (response.body() == null) {
                throw new IOException("Response body is null");
            }

            JSONObject jsonResponse = new JSONObject(response.body().string());
            JSONArray choices = jsonResponse.optJSONArray("choices");

            if (choices == null || choices.isEmpty()) {
                throw new IOException("No choices found in the response");
            }

            JSONObject messageObject = choices.getJSONObject(0).optJSONObject("message");
            if (messageObject == null) {
                throw new IOException("Message object is missing in the response");
            }

            String content = messageObject.optString("content", "Empty response from AI");
            return content.trim();
        }
    }

}
