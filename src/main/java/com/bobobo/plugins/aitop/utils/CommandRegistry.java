package com.bobobo.plugins.aitop.utils;

import com.bobobo.plugins.aitop.AI_top;
import com.bobobo.plugins.aitop.com.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

public class CommandRegistry {

    private final CommandMap commandMap;
    private final AI_top plugin = AI_top.getInstance();

    public CommandRegistry() throws ReflectiveOperationException {
        Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        this.commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
    }

    public void registerCommand(String name, Plugin plugin, Commands executor) {
        try {
            Command existingCommand = commandMap.getCommand(name);
            if (existingCommand != null) {
                this.plugin.logWarning("Command '" + name + "' already exists. Overwriting...");
                unregisterCommand(existingCommand);
            }

            Command command = new BukkitCommand(name) {
                @Override
                public boolean execute(@NotNull org.bukkit.command.CommandSender sender,
                                       @NotNull String commandLabel, String[] args) {
                    return executor.onCommand(sender, this, commandLabel, args);
                }
            };

            commandMap.register(plugin.getName(), command);
            this.plugin.logSuccess("Command '" + name + "' successfully registered!");

        } catch (Exception e) {
            this.plugin.logError("Failed to register command '" + name + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void unregisterCommand(Command command) {
        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            knownCommands.remove(command.getName());
            this.plugin.logInfo("Old command '" + command.getName() + "' successfully unregistered.");
        } catch (NoSuchFieldException e) {
            this.plugin.logWarning("Field 'knownCommands' not found. Skipping command removal.");
        } catch (Exception e) {
            this.plugin.logError("Error while unregistering command: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
