package com.quietwind01.Utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Placeholder {

    // Define a map to store the color codes and their corresponding placeholders
    private static final Map<String, String> colorCodes = new HashMap<>();

    static {
        colorCodes.put("{black}", "§0");
        colorCodes.put("{darkblue}", "§1");
        colorCodes.put("{darkgreen}", "§2");
        colorCodes.put("{darkaqua}", "§3");
        colorCodes.put("{darkred}", "§4");
        colorCodes.put("{darkpurple}", "§5");
        colorCodes.put("{gold}", "§6");
        colorCodes.put("{gray}", "§7");
        colorCodes.put("{darkgray}", "§8");
        colorCodes.put("{blue}", "§9");
        colorCodes.put("{green}", "§a");
        colorCodes.put("{aqua}", "§b");
        colorCodes.put("{red}", "§c");
        colorCodes.put("{purple}", "§d");
        colorCodes.put("{yellow}", "§e");
        colorCodes.put("{white}", "§f");
        colorCodes.put("{obfuscated}", "§k");
        colorCodes.put("{bold}", "§l");
        colorCodes.put("{strikethrough}", "§m");
        colorCodes.put("{underline}", "§n");
        colorCodes.put("{italic}", "§o");
        colorCodes.put("{reset}", "§r");
    }

    public static void broadcast(String input) {

        // Iterate over the entries in the map and replace each placeholder with the corresponding color code
        String output = "";
        for (Map.Entry<String, String> entry : colorCodes.entrySet()) {
            output = input.replace(entry.getKey().toLowerCase(), entry.getValue());
        }
        Bukkit.getServer().broadcastMessage(output);
        
    }

    public static void playerMessage(Player player, String input) {

        // Iterate over the entries in the map and replace each placeholder with the corresponding color code
        String output = "";
        for (Map.Entry<String, String> entry : colorCodes.entrySet()) {
            output = input.replace(entry.getKey().toLowerCase(), entry.getValue());
        }
        player.sendMessage(output);
        
    }

}
