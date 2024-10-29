package com.bobobo.plugins.aitop.utils;

import com.bobobo.plugins.aitop.AI_top;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Message {

    private static FileConfiguration messagesConfig;
    private static final Map<String, String> messages = new HashMap<>();
    private static final AI_top plugin = AI_top.getInstance();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void loadMessages() {
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messageFile.exists()) {
            plugin.saveResource("messages.yml", false);
            plugin.logInfo("messages.yml created.");
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messageFile);
        ConfigurationSection messagesSection = messagesConfig.getConfigurationSection("messages");

        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, messagesConfig.getString("messages." + key));
            }
            plugin.logSuccess("Messages loaded successfully.");
        } else {
            plugin.logError("Failed to find the 'messages' section in messages.yml.");
        }
    }

    public static Component getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, "Message not found for key: " + key);

        TagResolver resolver = TagResolver.empty();
        if (placeholders != null && !placeholders.isEmpty()) {
            TagResolver.Builder resolverBuilder = TagResolver.builder();
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                resolverBuilder.resolver(Placeholder.parsed(entry.getKey(), entry.getValue()));
            }
            resolver = resolverBuilder.build();
        }

        return miniMessage.deserialize(message, resolver);
    }

    public static Component getMessage(String key) {
        return getMessage(key, null);
    }

    public static void reloadMessages() {
        loadMessages();
        plugin.logInfo("Messages reloaded.");
    }
}
