package com.quietwind01.YAML;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerStats {

    private File file;
    private YamlConfiguration config;

    public PlayerStats(File dataFolder) {

        try {

            // Create file object
            this.file = new File(dataFolder, "stats.yml");

            // Get config
            this.config = YamlConfiguration.loadConfiguration(file);

        } catch (Exception e) {

            Bukkit.broadcastMessage("§cCouldn't fetch stats.yml file.");
            e.printStackTrace();

        }

        

    }

    public boolean createNewPlayer(String playerName) {

        try {

            if (!config.contains("player-stats." + playerName)) {
                config.set("player-stats." + playerName + ".total-wins", 0);
                config.set("player-stats." + playerName + ".total-amount-won", 0);
                config.set("player-stats." + playerName + ".total-tickets-purchased", 0);
                config.save(file);
            }

            return true;

        } catch (IOException e) {

            Bukkit.broadcastMessage("§cCouldn't create new player (§b" + playerName + "§c).");
            e.printStackTrace();
            return false;
            
        }

    }

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

        try {

            config.set("player-stats.all-time-payout-total", amountToAdd + getServerPayoutTotal());
            config.save(file);
            return true;

        } catch (IOException e) {

            Bukkit.broadcastMessage("§cCouldn't update total payout (server).");
            e.printStackTrace();
            return false;

        }
        
    }

    public boolean updateServerDrawsTotal(double amountToAdd) {

        try {

            config.set("player-stats.all-time-draws-total", amountToAdd + getServerDrawsTotal());
            config.save(file);
            return true;

        } catch (IOException e) {

            Bukkit.broadcastMessage("§cCouldn't update total draws (server).");
            e.printStackTrace();
            return false;
            
        }

    }

    public boolean updateServerWinsTotal(double amountToAdd) {

        try {

            config.set("player-stats.all-time-wins", amountToAdd + getServerWinsTotal());
            config.save(file);
            return true;

        } catch (IOException e) {

            Bukkit.broadcastMessage("§cCouldn't update total wins (server).");
            e.printStackTrace();
            return false;
            
        }

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

        try {

            config.set("player-stats." + playerName + ".total-wins", amountToAdd + getTotalWins(playerName));
            config.save(file);
            return true;

        } catch (IOException e) {

            Bukkit.broadcastMessage("§cCouldn't update total wins. (Player)");
            e.printStackTrace();
            return false;
            
        }

    }

    public boolean updateTotalAmountWon(String playerName, double amountToAdd) {

        try {

            config.set("player-stats." + playerName + ".total-amount-won", amountToAdd + getTotalAmountWon(playerName));
            config.save(file);
            return true;

        } catch (IOException e) {

            Bukkit.broadcastMessage("§cCouldn't update total amount won (player).");
            e.printStackTrace();
            return false;
            
        }

    }

    public boolean updateTotalTicketsPurchased(String playerName, double amountToAdd) {

        try {

            config.set("player-stats." + playerName + ".total-tickets-purchased", amountToAdd + getTotalTicketsPurchased(playerName));
            config.save(file);
            return true;

        } catch (IOException e) {

            Bukkit.broadcastMessage("§cCouldn't update total tickets purchased (player).");
            e.printStackTrace();
            return false;
            
        }

    }

}
