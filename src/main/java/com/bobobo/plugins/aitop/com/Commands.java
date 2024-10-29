package com.bobobo.plugins.aitop.com;

import com.bobobo.plugins.aitop.AI_top;
import com.bobobo.plugins.aitop.AI.utils.MessageHandler;
import com.bobobo.plugins.aitop.utils.Message;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Commands implements CommandExecutor {

    private final AI_top plugin;
    private final BukkitAudiences audience;
    private final Map<Player, Long> cooldowns = new HashMap<>();

    public Commands(AI_top plugin, BukkitAudiences audience) {
        this.plugin = plugin;
        this.audience = audience;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        String askCommand = plugin.getPluginConfig().getAskCommand();

        // Проверка пользовательской команды AI
        if (label.equalsIgnoreCase(askCommand)) {
            if (!(sender instanceof Player)) {
                audience.sender(sender).sendMessage(Message.getMessage("no-permission"));
                return true;
            }

            Player player = (Player) sender;

            // Проверка кулдауна
            if (isOnCooldown(player)) {
                long remainingTime = getRemainingCooldownTime(player);
                audience.sender(sender).sendMessage(
                        Message.getMessage("cooldown", Map.of("time", String.valueOf(remainingTime)))
                );
                return true;
            }

            if (args.length < 1) {
                audience.sender(sender).sendMessage(Message.getMessage("usage-ask"));
                return true;
            }

            // Устанавливаем время последнего запроса
            cooldowns.put(player, System.currentTimeMillis());

            // Обрабатываем запрос
            handleAskCommand(player, args);
            return true;
        }

        // Команда /ai
        if (label.equalsIgnoreCase("ai")) {
            if (!sender.hasPermission("aitop.admin")) {
                audience.sender(sender).sendMessage(Message.getMessage("no-permission"));
                return true;
            }

            if (args.length < 1) {
                audience.sender(sender).sendMessage(Message.getMessage("usage-ai"));
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    plugin.reloadConfig(); // Перезагрузка конфига
                    plugin.getPluginConfig().reload();
                    Message.reloadMessages();
                    audience.sender(sender).sendMessage(Message.getMessage("reload-success"));
                    break;

                default:
                    audience.sender(sender).sendMessage(Message.getMessage("unknown-command"));
                    break;
            }
        }

        return false;
    }


    private boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player)) return false;

        long lastUsed = cooldowns.get(player);
        long cooldownSeconds = plugin.getPluginConfig().getCooldownSeconds();
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastUsed) < cooldownSeconds * 1000;
    }

    private long getRemainingCooldownTime(Player player) {
        long lastUsed = cooldowns.get(player);
        long cooldownSeconds = plugin.getPluginConfig().getCooldownSeconds();
        long currentTime = System.currentTimeMillis();

        return (cooldownSeconds * 1000 - (currentTime - lastUsed)) / 1000;
    }

    private void handleAskCommand(Player player, String[] args) {
        String questionString = String.join(" ", args);

        // Отправляем сообщение в чат
        Component chatMessage = Component.text("<" + player.getName() + "> " + questionString);
        Bukkit.getServer().broadcast(chatMessage);

        // Асинхронная обработка запроса
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String response = MessageHandler.handleMessage(Component.text(questionString));

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player.getName());
            placeholders.put("response", response);

            Component resultMessage = Message.getMessage("ask-success", placeholders);

            // Отправляем ответ
            Bukkit.getScheduler().runTask(plugin, () -> {
                audience.all().sendMessage(resultMessage);
            });
        });
    }
}
