package com.bobobo.plugins.aitop.AI.utils;

import com.bobobo.plugins.aitop.AI.OpenAI;
import com.bobobo.plugins.aitop.AI.Gemini;
import com.bobobo.plugins.aitop.AI_top;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.io.IOException;

public class MessageHandler {

    public static String handleMessage(Component playerMessage) {
        AI_top plugin = AI_top.getInstance();
        String aiType = plugin.getPluginConfig().getAI();

        try {
            switch (aiType.toUpperCase()) {
                case "GPT":
                    return handleGPT(PlainTextComponentSerializer.plainText().serialize(playerMessage));
                case "GEMINI":
                    return handleGemini(playerMessage);
                default:
                    return "Error: Unknown AI type - " + aiType;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred while processing the request: " + e.getMessage();
        }
    }

    private static String handleGPT(String prompt) throws IOException {
        AI_top plugin = AI_top.getInstance();
        String apiKey = plugin.getPluginConfig().getGptApiKey();
        String model = plugin.getPluginConfig().getOpenAIModel();
        int maxTokens = plugin.getPluginConfig().getMaxTokens();
        String systemRole = plugin.getPluginConfig().getSystemRole();

        OpenAI openAI = new OpenAI(apiKey, model, maxTokens, systemRole);
        return openAI.askQuestion(prompt);
    }

    private static String handleGemini(Component playerMessage) throws IOException {
        AI_top plugin = AI_top.getInstance();
        String apiKey = plugin.getPluginConfig().getGeminiApiKey();
        String systemRole = plugin.getPluginConfig().getSystemRole();
        String model = plugin.getPluginConfig().getGeminiModel();

        Gemini gemini = new Gemini(apiKey, model);
        return gemini.getResponse(playerMessage, systemRole);
    }
}
