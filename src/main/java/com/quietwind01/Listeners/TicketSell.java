package com.quietwind01.Listeners;

import com.quietwind01.Utils.EconomyUtils;
import com.quietwind01.YAML.PlayerStats;
import com.quietwind01.QLottery;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TicketSell implements CommandExecutor {
    
    private final QLottery plugin;
    private PlayerStats stats;

    public TicketSell(QLottery plugin) {

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
        if (!player.hasPermission("qlottery.ticket.sell")) {
            player.sendMessage(plugin.chatPrefix + "§cYou do not have permission to use this command.");
            return true;
        }

        // Debug
        //player.sendMessage("TicketSell Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 3) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql ticket sell <amount>");
            return true;
        }

        // Check if the first two arguments are "ticket" and "buy"
        if (!args[0].equalsIgnoreCase("ticket") || !args[1].equalsIgnoreCase("sell")) {
            player.sendMessage(plugin.chatPrefix + "§cUsage: /ql ticket sell <amount>");
            return true;
        }

        // Check that amount is valid
        int amount;
        int maxTickets = plugin.getConfig().getInt("max-tickets");
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 1 || amount > maxTickets) {
                player.sendMessage(plugin.chatPrefix + "§cYou can only sell between §91 §cand §9" + maxTickets + " §ctickets.");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.chatPrefix + "§cInvalid amount. Please specify a valid number between §91 §cand §9" + maxTickets + "§c.");
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
            player.sendMessage(plugin.chatPrefix + "§cYou have not purchased any tickets yet. You have no tickets to sell.");
            return false;
        }
        if (playerTicketsAmount >= amount) { // If player has purchased tickets but doesn't have enough to sell the desired amount
            player.sendMessage(plugin.chatPrefix + "§cYou do not have enough tickets to sell §f" + amount + " §ctickets! You only have §f" + playerTicketsAmount + "§ctickets.");
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
        player.sendMessage(plugin.chatPrefix + "§2You have sold §f" + amount + " §2tickets for §e$" + totalCostOfSale + "§2, congratulations!");
        player.sendMessage(plugin.chatPrefix + "§eYou now have a balance of §a$" + playerBalance + "§e!");
        player.sendMessage(plugin.chatPrefix + "§eYou now have  §a" + playerTickets.get(player.getName()) + "§e tickets!");

        return true;

    }

}
