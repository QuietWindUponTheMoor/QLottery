package com.quietwind01.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quietwind01.QLottery;

public class TicketCost implements CommandExecutor {
    
    private final QLottery plugin;

    public TicketCost(QLottery plugin) {

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
        if (!player.hasPermission("qlottery.ticket.cost")) {
            player.sendMessage(plugin.chatPrefix + "§cYou do not have permission to use this command.");
            return true;
        }

        // Debug
        //player.sendMessage("TicketCost Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 2) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql ticket cost");
            return true;
        }

        // Check if the two arguments are "pool" and "amount"
        if (!args[0].equalsIgnoreCase("ticket") || !args[1].equalsIgnoreCase("cost")) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql ticket cost");
            return true;
        }

        // Send the pool amount to the player
        player.sendMessage(plugin.chatPrefix + "Tickets currently cost: §a$" + plugin.getConfig().getDouble("ticket-cost"));

        return true;

    }

}
