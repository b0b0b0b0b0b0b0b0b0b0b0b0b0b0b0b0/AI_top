package com.bobobo.plugins.aitop.conf;

import com.bobobo.plugins.aitop.AI_top;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {

    private AI_top plugin;
    private FileConfiguration config;

    public Config(AI_top plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getAI() {
        return config.getString("ai", "GPT");
    }

    public long getCooldownSeconds() {
        return config.getLong("cooldown-seconds", 30);
    }
    public String getAskCommand() {
        return config.getString("ask-command", "ask");
    }
    public List<String> getPrefixes() {
        return config.getStringList("prefixes");
    }

    public String getGptApiKey() {
        return config.getString("gpt-api-key", "");
    }

    public String getOpenAIModel() {
        return config.getString("openai-model", "gpt-3.5-turbo");
    }

    public int getMaxTokens() {
        return config.getInt("max-tokens", 500);
    }

    public String getSystemRole() {
        return config.getString("system-role", "Ты помощник для Minecraft сервера. Не используй код в своих ответах.");
    }

    public String getGeminiApiKey() {
        return config.getString("gemini-api-key", "");
    }

    public String getGeminiModel() {
        return config.getString("gemini-model", "gemini-1.0-pro-latest");
    }

}
