package com.quietwind01.Listeners;

import com.quietwind01.Utils.EconomyUtils;
import com.quietwind01.QLottery;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.quietwind01.YAML.PlayerStats;

public class TicketBuy implements CommandExecutor {
    
    private final QLottery plugin;
    private PlayerStats stats;

    public TicketBuy(QLottery plugin, File dataFolder) {

        this.plugin = plugin;
        this.stats = new PlayerStats(dataFolder);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.chatPrefix + "§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Check that player has proper permissions
        if (!player.hasPermission("qlottery.ticket.buy")) {
            player.sendMessage(plugin.chatPrefix + "§cYou do not have permission to use this command.");
            return true;
        }

        // Debug
        //player.sendMessage("TicketBuy Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 3) {
            player.sendMessage(plugin.chatPrefix + "§eUsage: /ql ticket buy <amount>");
            return true;
        }

        // Check if the first two arguments are "ticket" and "buy"
        if (!args[0].equalsIgnoreCase("ticket") || !args[1].equalsIgnoreCase("buy")) {
            player.sendMessage(plugin.chatPrefix + "§cUsage: /ql ticket buy <amount>");
            return true;
        }

        // Check that amount is valid
        int amount;
        int maxTickets = plugin.getConfig().getInt("max-tickets");
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 1 || amount > maxTickets) {
                player.sendMessage(plugin.chatPrefix + "§cYou can only purchase between §91 §cand §9" + maxTickets + " §ctickets.");
                return true;
            } else if (canPlayerBuyTheseTickets(player, amount) == false) {
                player.sendMessage(plugin.chatPrefix + "§cYou can only purchase between §91 §cand §9" + maxTickets + " §ctickets.");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.chatPrefix + "§cInvalid amount. Please specify a valid number between §91 §cand §9" + maxTickets + "§c.");
            return true;
        }

        // Get the purchase price of each ticket
        double ticketCost = plugin.getConfig().getDouble("ticket-cost");

        // Calculate the cost of the tickets
        double totalCostOfPurchase = ticketCost * amount;

        // Get player balance
        EconomyUtils econUtility = new EconomyUtils(plugin);
        double playerBalance = econUtility.getPlayerBalance(player);

        // Check if the player has enough money
        if (playerBalance < totalCostOfPurchase) {
            player.sendMessage(plugin.chatPrefix + "§cYou do not have enough money to buy §f" + amount + " §ctickets!");
            return false;
        }

        // Remove totalCostOfPurchase from player's account and then store to ConcurrentHashMap
        econUtility.subtractBalance(player, totalCostOfPurchase);

        // Update player's total purchased tickets
        stats.createNewPlayer(player.getName());
        stats.updateTotalTicketsPurchased(player.getName(), amount);
        
        // Update server stats
        stats.updateTotalTicketsPurchased("__LOTTERY__", amount);

        // Update the HashMap with the player's ticket count
        ConcurrentHashMap<String, Integer> playerTickets = plugin.getPlayerTickets();
        playerTickets.put(player.getName(), playerTickets.getOrDefault(player.getName(), 0) + amount);

        // Update total pool
        plugin.updateTotalPool(totalCostOfPurchase);

        // Send success message
        player.sendMessage(plugin.chatPrefix + "§2You have purchased §f" + amount + " §2tickets for §e$" + totalCostOfPurchase + "§2, congratulations!");
        player.sendMessage(plugin.chatPrefix + "§eYou now have a balance of §a$" + playerBalance + "§e!");
        player.sendMessage(plugin.chatPrefix + "§eYou now have  §a" + playerTickets.get(player.getName()) + "§e tickets!");

        return true;

    }

    private boolean canPlayerBuyTheseTickets(Player player, Integer amount) {

        // Initialize canBuy
        boolean canBuy = false;

        // Max tickets allowed to own
        int maxTickets = plugin.getConfig().getInt("max-tickets");

        // Get the player's map record
        Integer currentTicketsOwned = plugin.playerTickets.get(player.getName());

        // If the player has no tickets
        if (currentTicketsOwned == null || currentTicketsOwned <= 0) {
            if (amount <= maxTickets && amount > 0) {
                canBuy = true;
            } else if (amount > maxTickets || amount <= 0) {
                canBuy = false;
            }
        }

        // If player already has tickets
        if (currentTicketsOwned != null && currentTicketsOwned > 0) {
            Integer newTotal = currentTicketsOwned + amount;
            if (newTotal > maxTickets) {
                canBuy = false;
            } else if (newTotal > 0 && newTotal <= maxTickets) {
                canBuy = true;
            }
        }

        // Return result
        return canBuy;

    }

}
