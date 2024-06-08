package com.quietwind01.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quietwind01.QLottery;

public class TotalPool implements CommandExecutor {
    
    private final QLottery plugin;

    public TotalPool(QLottery plugin) {

        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.chatPrefix + "§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Check that player has proper permissions
        if (!player.hasPermission("qlottery.pool.amount")) {
            player.sendMessage(plugin.chatPrefix + "§cYou do not have permission to use this command.");
            return true;
        }

        // Debug
        //player.sendMessage("TicketBuy Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 2) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql pool amount");
            return true;
        }

        // Check if the two arguments are "pool" and "amount"
        if (!args[0].equalsIgnoreCase("pool") || !args[1].equalsIgnoreCase("amount")) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql pool amount");
            return true;
        }

        // Fetch the pool amount
        double poolAmount = plugin.totalPool;

        // Send the pool amount to the player
        player.sendMessage(plugin.chatPrefix + "The amount in the pool: §a$" + poolAmount);

        return true;

    }

}
