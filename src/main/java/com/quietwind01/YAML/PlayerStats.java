package com.quietwind01.YAML;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerStats extends JavaPlugin {

    File file = new File(getDataFolder(), "stats.yml");
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    /* 
     * Server stats
     */

    public double getServerPayoutTotal() {
        return config.getDouble("player-stats.all-time-payout-total");
    }

    public double getServerDrawsTotal() {
        return config.getDouble("player-stats.all-time-draws-total");
    }

    public double getServerWinsTotal() {
        return config.getDouble("player-stats.all-time-wins");
    }

    public boolean updateServerPayoutTotal(double amountToAdd) {
        config.set("player-stats.all-time-payout-total", amountToAdd + getServerPayoutTotal());
        return true;
    }

    public boolean updateServerDrawsTotal(double amountToAdd) {
        config.set("player-stats.all-time-draws-total", amountToAdd + getServerDrawsTotal());
        return true;
    }

    public boolean updateServerWinsTotal(double amountToAdd) {
        config.set("player-stats.all-time-wins", amountToAdd + getServerWinsTotal());
        return true;
    }

    /*
     * Player stats
     */
    
    public double getTotalWins(String playerName) {
        return config.getDouble("player-stats." + playerName + ".total-wins");
    }

    public double getTotalAmountWon(String playerName) {
        return config.getDouble("player-stats." + playerName + ".total-amount-won");
    }

    public double getTotalTicketsPurchased(String playerName) {
        return config.getDouble("player-stats." + playerName + ".total-tickets-purchased");
    }

    public boolean updateTotalWins(String playerName, double amountToAdd) {
        config.set("player-stats." + playerName + ".total-wins", amountToAdd + getTotalWins(playerName));
        return true;
    }

    public boolean updateTotalAmountWon(String playerName, double amountToAdd) {
        config.set("player-stats." + playerName + ".total-amount-won", amountToAdd + getTotalAmountWon(playerName));
        return true;
    }

    public boolean updateTotalTicketsPurchased(String playerName, double amountToAdd) {
        config.set("player-stats." + playerName + ".total-tickets-purchased", amountToAdd + getTotalTicketsPurchased(playerName));
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
