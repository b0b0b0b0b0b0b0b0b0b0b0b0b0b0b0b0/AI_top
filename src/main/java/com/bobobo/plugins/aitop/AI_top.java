package com.bobobo.plugins.aitop;

import com.bobobo.plugins.aitop.com.Commands;
import com.bobobo.plugins.aitop.com.TabCompleterHandler;
import com.bobobo.plugins.aitop.conf.Config;
import com.bobobo.plugins.aitop.utils.ChatListener;
import com.bobobo.plugins.aitop.utils.CommandRegistry;
import com.bobobo.plugins.aitop.utils.Message;
import com.bobobo.plugins.aitop.utils.UP;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AI_top extends JavaPlugin {

    public static final String PREFIX = "\u001B[37m[\u001B[90mAI-top\u001B[37m]\u001B[0m ";

    private static AI_top instance;
    private Config config;
    private BukkitAudiences audience;
    String version = getDescription().getVersion();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        config = new Config(this);
        audience = BukkitAudiences.create(this);
        Message.loadMessages();
        int pluginId = 23754;
        new Metrics(this, pluginId);
        Bukkit.getScheduler().runTaskLater(this, () -> UP.checkVersion(version), 60L);
        if (!validateApiKeys()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerPrimaryCommand();
        registerCustomCommand(config.getAskCommand(), new Commands(this, audience));
        new ChatListener(this);
    }

    private void registerPrimaryCommand() {
        PluginCommand command = getCommand("ai");
        if (command != null) {
            command.setExecutor(new Commands(this, audience));
            command.setTabCompleter(new TabCompleterHandler(this));
            logSuccess("Primary command 'ai' registered.");
        } else {
            logError("Failed to find command 'ai' in plugin.yml.");
        }
    }

    private void registerCustomCommand(String name, Commands executor) {
        try {
            logInfo("Registering custom command: " + name);
            CommandRegistry registry = new CommandRegistry();
            registry.registerCommand(name, this, executor);
        } catch (ReflectiveOperationException e) {
            logError("Failed to register command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (audience != null) {
            audience.close();
        }
    }

    public static AI_top getInstance() {
        return instance;
    }

    public Config getPluginConfig() {
        return config;
    }

    public BukkitAudiences getAudience() {
        return audience;
    }

    private boolean validateApiKeys() {
        String selectedAI = config.getAI();
        String gptApiKey = config.getGptApiKey();
        String geminiApiKey = config.getGeminiApiKey();

        if ("GPT".equalsIgnoreCase(selectedAI) && "your_openai_api_key".equals(gptApiKey)) {
            logError("Please set a valid GPT API key.");
            return false;
        }
        if ("GEMINI".equalsIgnoreCase(selectedAI) && "your_gemini_api_key".equals(geminiApiKey)) {
            logError("Please set a valid Gemini API key.");
            return false;
        }
        return true;
    }

    // Logging methods with ANSI colors and a consistent prefix
    public void logInfo(String message) {
        getLogger().info( "\u001B[34m" + message + "\u001B[0m");
    }

    public void logWarning(String message) {
        getLogger().warning( "\u001B[33m" + message + "\u001B[0m");
    }

    public void logError(String message) {
        getLogger().severe("\u001B[31m" + message + "\u001B[0m");
    }

    public void logSuccess(String message) {
        getLogger().info( "\u001B[32m" + message + "\u001B[0m");
    }
}
