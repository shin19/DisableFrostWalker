package com.github.shin19;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisableFrostWalker extends JavaPlugin implements Listener {
    private List<Pattern> enableWorldsPatternList = new ArrayList<Pattern>();
    private final String prefix = "[DisableFrostWalker] ";
    private final String version = "v1.0.0";

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        List<String> enableWorlds = getConfig().getStringList("enableWorlds");
        for (String world : enableWorlds) {
            enableWorldsPatternList.add(Pattern.compile(world));
        }
        this.getCommand("disablefrostwalker").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
                if (!sender.hasPermission("disablefrostwalker.reload")) {
                    sender.sendMessage(prefix + "You don't have permission");
                    return true;
                }
                if (args.length != 1) {
                    sender.sendMessage(prefix + "Invalid argument");
                    return false;
                }
                // </disablefrostwalker reload>
                if (args[0].equalsIgnoreCase("reload")) {
                    reloadEnableWorlds();
                    sender.sendMessage(prefix + "Config file reload has been completed");
                    return true;
                }
                return false;
            }
        });
        this.getCommand("disablefrostwalker").setTabCompleter(new TabCompleter() {
            public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
                if (args.length == 1) {
                    return new ArrayList<String>(Collections.singletonList("reload"));
                }
                return new ArrayList<String>(Collections.singletonList(""));
            }
        });
        Logger.getLogger("Minecraft").log(Level.INFO, prefix + "Enabled DisableFrostWalker " + version);
    }

    @Override
    public void onDisable() {
        Logger.getLogger("Minecraft").log(Level.INFO, prefix + "Disabled DisableFrostWalker " + version);
    }

    @EventHandler
    public void onEntityBlockFrost(EntityBlockFormEvent event) {
        // block form by player
        if (event.getEntity() instanceof Player) {
            String eventWorldName = event.getEntity().getWorld().getName();
            boolean isEnableWorld = false;
            for (Pattern worldPattern : enableWorldsPatternList) {
                Matcher matcher = worldPattern.matcher(eventWorldName);
                if (matcher.matches()) {
                    isEnableWorld = true;
                    break;
                }
            }
            // world not found at enable list
            if (!isEnableWorld) {
                event.setCancelled(true);
            }
        }
    }

    private void reloadEnableWorlds() {
        this.reloadConfig();
        enableWorldsPatternList.clear();
        List<String> enableWorlds = getConfig().getStringList("enableWorlds");
        for (String world : enableWorlds) {
            enableWorldsPatternList.add(Pattern.compile(world));
        }
    }
}
