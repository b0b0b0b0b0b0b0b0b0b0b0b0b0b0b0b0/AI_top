package com.bobobo.plugins.aitop.utils;


import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UP {

    private static final String VERSION_URL = "https://b0b0b0.dev/aitopRaw.txt";
    private static final String PREFIX = "\u001B[37m[\u001B[90mAI-top\u001B[37m]\u001B[0m ";
    private static final ConsoleCommandSender console = Bukkit.getConsoleSender();

    /**
     * Checks the plugin version and displays a message if an update is needed.
     * @param currentVersion The current version of the plugin.
     */
    public static void checkVersion(String currentVersion) {
        try {
            String latestVersion = fetchLatestVersion();

            if (latestVersion == null) {
                logError("Failed to fetch the latest version. Skipping version check.");
                return;
            }

            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                console.sendMessage(PREFIX + " ");
                logWarning("You are using an outdated version!");
                logWarning("Current version: \u001B[90m" + currentVersion +
                        "\u001B[0m, latest version: \u001B[32m" + latestVersion + "\u001B[0m.");
                logWarning("Please update the plugin. The latest version is already available on our website:");

                console.sendMessage(PREFIX + "\u001B[36m Black-Minecraft: \u001B[0mhttps://black-minecraft.com/resources/6787/");
                console.sendMessage(PREFIX + " ");
            } else {
                logInfo("You are using the latest version of the plugin! Version: \u001B[32m" + currentVersion + "\u001B[0m.");
            }
        } catch (Exception e) {
            logError("Error during version check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetches the latest version from the specified URL.
     * @return A string with the version or null if an error occurred.
     */
    private static String fetchLatestVersion() {
        try {
            URL url = new URL(VERSION_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return in.readLine().trim(); // Reads the first line with the version.
            }
        } catch (IOException e) {
            logError("Connection error to " + VERSION_URL + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Logs an informational message.
     * @param message The message to display in the console.
     */
    private static void logInfo(String message) {
        console.sendMessage(PREFIX + "\u001B[32m" + message + "\u001B[0m");
    }

    /**
     * Logs a warning message.
     * @param message The message to display in the console.
     */
    private static void logWarning(String message) {
        console.sendMessage(PREFIX + "\u001B[33m" + message + "\u001B[0m");
    }

    /**
     * Logs an error message.
     * @param message The message to display in the console.
     */
    private static void logError(String message) {
        console.sendMessage(PREFIX + "\u001B[31m" + message + "\u001B[0m");
    }
}
