package com.bobobo.plugins.aitop.com;

import com.bobobo.plugins.aitop.AI_top;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterHandler implements TabCompleter {

    private final AI_top plugin;

    public TabCompleterHandler(AI_top plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, String alias, String[] args) {
        String askCommand = plugin.getPluginConfig().getAskCommand();

        if (alias.equalsIgnoreCase("ai")) {
            if (args.length == 1) {
                List<String> subCommands = new ArrayList<>();

                // Добавляем подкоманды в зависимости от прав
                if (sender.hasPermission("aitop.admin")) {
                    subCommands.add("reload");
                }
                if (sender.hasPermission("aitop.ask")) {
                    subCommands.add(askCommand);
                }

                List<String> result = new ArrayList<>();
                for (String subCommand : subCommands) {
                    if (subCommand.startsWith(args[0].toLowerCase())) {
                        result.add(subCommand);
                    }
                }
                return result;
            }
        }
        return null;
    }
}
