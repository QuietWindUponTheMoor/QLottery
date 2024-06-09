package com.quietwind01.Listeners;

import com.quietwind01.Utils.EconomyUtils;
import com.quietwind01.YAML.PlayerStats;
import com.quietwind01.QLottery;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static com.quietwind01.Utils.Formatting.formatNumber;
import static com.quietwind01.Utils.Formatting.playerMessage;

public class TicketSell implements CommandExecutor {
    
    private final QLottery plugin;
    private PlayerStats stats;

    public TicketSell(QLottery plugin, File dataFolder) {

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
        if (!player.hasPermission("qlottery.ticket.sell")) {
            playerMessage(player, plugin.chatPrefix + "{red}You do not have permission to use this command.");
            return true;
        }

        // Debug
        //playerMessage(player, "TicketSell Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 3) {
            playerMessage(player, plugin.chatPrefix + "{yellow}Usage: /ql ticket sell <amount>");
            return true;
        }

        // Check if the first two arguments are "ticket" and "buy"
        if (!args[0].equalsIgnoreCase("ticket") || !args[1].equalsIgnoreCase("sell")) {
            playerMessage(player, plugin.chatPrefix + "{red}Usage: /ql ticket sell <amount>");
            return true;
        }

        // Check that amount is valid
        int amount;
        int maxTickets = plugin.getConfig().getInt("max-tickets");
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 1 || amount > maxTickets) {
                playerMessage(player, plugin.chatPrefix + "{red}You can only sell between {blue}1 {red}and {blue}" + formatNumber(maxTickets) + " {red}tickets.");
                return true;
            }
        } catch (NumberFormatException e) {
            playerMessage(player, plugin.chatPrefix + "{red}Invalid amount. Please specify a valid number between {blue}1 {red}and {blue}" + formatNumber(maxTickets) + "{red}.");
            return true;
        }

        // Get the purchase price of each ticket
        double ticketCost = plugin.getConfig().getDouble("ticket-cost");

        // Calculate the cost of the tickets
        double totalCostOfSale = ticketCost * amount;

        // Check if the player is selling tickets they actually own
        ConcurrentHashMap<String, Integer> playerTickets = plugin.getPlayerTickets();
        Integer playerTicketsAmount = playerTickets.get(player.getName());
        if (playerTicketsAmount == null) { // If player hasn't bought any tickets (playerTicketsAmount is null)
            playerMessage(player, plugin.chatPrefix + "{red}You have not purchased any tickets yet. You have no tickets to sell.");
            return false;
        }
        if (playerTicketsAmount >= amount) { // If player has purchased tickets but doesn't have enough to sell the desired amount
            playerMessage(player, plugin.chatPrefix + "{red}You do not have enough tickets to sell {white}" + formatNumber(amount) + " {red}tickets! You only have {white}" + formatNumber(playerTicketsAmount) + "{red}tickets.");
            return false;
        }

        // Add totalCostOfSale from player's account and then store to ConcurrentHashMap
        EconomyUtils econUtility = new EconomyUtils(plugin);
        double playerBalance = econUtility.getPlayerBalance(player); // Get player balance
        econUtility.addBalance(player, totalCostOfSale);

        // Update player's total purchased tickets
        stats.createNewPlayer(player.getName());
        stats.updateTotalTicketsPurchased(player.getName(), -amount);

        // Update server stats
        stats.updateTotalTicketsPurchased("__LOTTERY__", -amount);

        // Update the HashMap with the player's ticket count
        Integer newTicketCount = playerTicketsAmount - amount;
        playerTickets.put(player.getName(), newTicketCount);

        // Update total pool
        plugin.updateTotalPool(-totalCostOfSale);

        // Send success message
        playerMessage(player, plugin.chatPrefix + "{darkgreen}You have sold {white}" + formatNumber(amount) + " {darkgreen}tickets for {yellow}$" + formatNumber(totalCostOfSale) + "{darkgreen}, congratulations!");
        playerMessage(player, plugin.chatPrefix + "{yellow}You now have a balance of {green}$" + formatNumber(((playerBalance + totalCostOfSale))) + "{yellow}!");
        playerMessage(player, plugin.chatPrefix + "{yellow}You now have {green}" + formatNumber(playerTickets.get(player.getName())) + "{yellow} tickets!");

        return true;

    }

}
