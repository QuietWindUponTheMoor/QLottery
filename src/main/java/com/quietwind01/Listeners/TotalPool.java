package com.quietwind01.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quietwind01.QLottery;
import static com.quietwind01.Utils.Formatting.formatNumber;
import static com.quietwind01.Utils.Formatting.playerMessage;

public class TotalPool implements CommandExecutor {
    
    private final QLottery plugin;

    public TotalPool(QLottery plugin) {

        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.chatPrefix + "{red}Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Check that player has proper permissions
        if (!player.hasPermission("qlottery.pool.amount")) {
            playerMessage(player, plugin.chatPrefix + "{red}You do not have permission to use this command.");
            return true;
        }

        // Debug
        //playerMessage(player, "TicketBuy Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 2) {
            playerMessage(player, plugin.chatPrefix + "{yellow}Usage: /ql pool amount");
            return true;
        }

        // Check if the two arguments are "pool" and "amount"
        if (!args[0].equalsIgnoreCase("pool") || !args[1].equalsIgnoreCase("amount")) {
            playerMessage(player, plugin.chatPrefix + "{yellow}Usage: /ql pool amount");
            return true;
        }

        // Fetch the pool amount
        double poolAmount = plugin.totalPool;

        // Send the pool amount to the player
        playerMessage(player, plugin.chatPrefix + "The amount in the pool: {green}$" + formatNumber(poolAmount));

        return true;

    }

}
