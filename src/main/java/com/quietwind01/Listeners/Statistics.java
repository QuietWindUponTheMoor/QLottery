package com.quietwind01.Listeners;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quietwind01.QLottery;
import com.quietwind01.YAML.PlayerStats;
import static com.quietwind01.Utils.Formatting.formatNumber;
import static com.quietwind01.Utils.Formatting.playerMessage;

public class Statistics implements CommandExecutor {
    
    private final QLottery plugin;
    private final PlayerStats stats;

    public Statistics(QLottery plugin, File dataFolder) {

        this.plugin = plugin;
        this.stats = new PlayerStats(dataFolder);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.chatPrefix + "{red}Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Check that player has proper permissions
        if (!player.hasPermission("qlottery.stats")) {
            playerMessage(player, plugin.chatPrefix + "{red}You do not have permission to use this command.");
            return true;
        }

        // Debug
        //playerMessage(player, "TicketBuy Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 1) {
            playerMessage(player, plugin.chatPrefix + "{yellow}Usage: /ql stats");
            return true;
        }

        // Check if the two arguments are "pool" and "amount"
        if (!args[0].equalsIgnoreCase("stats")) {
            playerMessage(player, plugin.chatPrefix + "{yellow}Usage: /ql stats");
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
        playerMessage(player, plugin.chatPrefix + "{darkaqua}You have {gold}" + formatNumber(playerTotalWins) + " {darkaqua}wins, you've purchased {gold}" + formatNumber(playerTotalTickets) + " {darkaqua}tickets, and you've won a total of {green}$" + formatNumber(playerTotalPayout) + "{darkaqua}!");
        playerMessage(player, plugin.chatPrefix + "{darkaqua}The server has drawn {gold}" + formatNumber(serverTotalDraws) + " {darkaqua}times, paid out {green}$" + formatNumber(serverTotalPayouts) + " {darkaqua}and there have been a total of {gold}" + formatNumber(serverTotalWins) + " {darkaqua}jackpots won! There have been {gold}" + formatNumber(lotteryTotalTickets) + " {darkaqua}total tickets sold.");

        return true;

    }

}
