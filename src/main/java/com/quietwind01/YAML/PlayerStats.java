package com.quietwind01.YAML;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerStats extends JavaPlugin {

    File file = new File(getDataFolder(), "stats.yml");
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
    
    public double getTotalWins(String playerName) {
        return config.getDouble("player-stats." + playerName + ".total-wins");
    }

    public double getTotalAmountWon(String playerName) {
        return config.getDouble("player-stats." + playerName + ".total-amount-won");
    }

    public double getTotalTicketsPurchased(String playerName) {
        return config.getDouble("player-stats." + playerName + ".total-tickets-purchased");
    }

    public boolean updateTotalWins(String playerName, double newValue) {
        config.set("player-stats." + playerName + ".total-wins", newValue);
        return true;
    }

    public boolean updateTotalAmountWon(String playerName, double newValue) {
        config.set("player-stats." + playerName + ".total-amount-won", newValue);
        return true;
    }

    public boolean updateTotalTicketsPurchased(String playerName, double newValue) {
        config.set("player-stats." + playerName + ".total-tickets-purchased", newValue);
        return true;
    }

    public boolean createNewPlayer(String playerName) {

        if (!config.contains("player-stats." + playerName)) {
            config.set("player-stats." + playerName + ".total-wins", 0);
            config.set("player-stats." + playerName + ".total-amount-won", 0);
            config.set("player-stats." + playerName + ".total-tickets-purchased", 0);
            return true;
        }

        return false; // Player already exists with this name

    }

}
