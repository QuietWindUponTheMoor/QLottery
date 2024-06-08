package com.quietwind01.Utils;

import com.quietwind01.QLottery;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyUtils {

    private Economy economy;

    public EconomyUtils(QLottery plugin) {

        setupEconomy(plugin);

    }

    private void setupEconomy(Plugin plugin) {

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }

    }

    public double getPlayerBalance(Player player) {

        if (economy == null) {
            return -1; // Return -1 if economy is not set up
        }
        return economy.getBalance(player);

    }

    public boolean subtractBalance(Player player, double amount) {

        if (economy == null) {
            return false; // Economy not set up, cannot subtract amount
        }

        if (economy.getBalance(player) < amount) {
            return false; // Player does not have enough money
        }

        economy.withdrawPlayer(player, amount);

        return true; // Successfully subtracted amount

    }

    public boolean addBalance(Player player, double amount) {

        if (economy == null) {
            return false; // Economy not set up, cannot subtract amount
        }

        economy.depositPlayer(player, amount);

        return true; // Successfully subtracted amount

    }

}
