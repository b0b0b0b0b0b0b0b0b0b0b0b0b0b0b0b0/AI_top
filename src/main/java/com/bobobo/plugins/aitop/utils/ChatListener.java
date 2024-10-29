package com.bobobo.plugins.aitop.utils;

import com.bobobo.plugins.aitop.AI.utils.MessageHandler;
import com.bobobo.plugins.aitop.AI_top;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListener implements Listener {

    private final AI_top plugin;
    private final BukkitAudiences audience;

    public ChatListener(AI_top plugin) {
        this.plugin = plugin;
        this.audience = BukkitAudiences.create(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Получаем префиксы из конфига
        List<String> prefixes = plugin.getPluginConfig().getPrefixes();

        // Проверяем, начинается ли сообщение с одного из префиксов
        boolean hasPrefix = prefixes.stream().anyMatch(message::startsWith);

        // Если префиксов нет или сообщение не содержит их — выходим
        if (prefixes.isEmpty() || !hasPrefix) {
            return;
        }

        // Асинхронная обработка сообщения
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String response = MessageHandler.handleMessage(Component.text(message));
            String playerName = player.getName();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            placeholders.put("response", response);

            Component resultMessage = Message.getMessage("ask-success", placeholders);

            // Отправляем сообщение всем через Adventure
            Bukkit.getScheduler().runTask(plugin, () -> {
                audience.all().sendMessage(resultMessage);
            });
        });
    }
}
