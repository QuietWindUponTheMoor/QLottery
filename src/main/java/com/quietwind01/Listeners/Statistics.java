package com.quietwind01.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quietwind01.QLottery;
import com.quietwind01.YAML.PlayerStats;

public class Statistics implements CommandExecutor {
    
    private final QLottery plugin;
    private PlayerStats stats;

    public Statistics(QLottery plugin) {

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
        if (!player.hasPermission("qlottery.stats")) {
            player.sendMessage(plugin.chatPrefix + "§cYou do not have permission to use this command.");
            return true;
        }

        // Debug
        //player.sendMessage("TicketBuy Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 1) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql stats");
            return true;
        }

        // Check if the two arguments are "pool" and "amount"
        if (!args[0].equalsIgnoreCase("stats")) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql stats");
            return true;
        }

        // Fetch the stats for the server
        double serverTotalPayouts = stats.getServerPayoutTotal();
        double serverTotalDraws = stats.getServerDrawsTotal();
        double serverTotalWins = stats.getServerWinsTotal();
        double lotteryTotalTickets = stats.getTotalTicketsPurchased("__LOTTERY__");
        // Player stats
        String playerName = player.getName();
        stats.createNewPlayer(playerName); // Create the player in the stats file if they don't exist yet
        double playerTotalWins = stats.getTotalWins(playerName);
        double playerTotalPayout = stats.getTotalAmountWon(playerName);
        double playerTotalTickets = stats.getTotalTicketsPurchased(playerName);

        // Send the stats to the player
        player.sendMessage(plugin.chatPrefix + "§3You have §6" + playerTotalWins + " §3wins, you've purchased §6" + playerTotalTickets + " §3tickets, and you've won a total of §a$" + playerTotalPayout + "§3!");
        player.sendMessage(plugin.chatPrefix + "§3The server has drawn §6" + serverTotalDraws + " §3times, paid out §a$" + serverTotalPayouts + " §3and there have been a total of §6" + serverTotalWins + " §3jackpots won! There have been §6" + lotteryTotalTickets + " §3total tickets sold.");

        return true;

    }

}
