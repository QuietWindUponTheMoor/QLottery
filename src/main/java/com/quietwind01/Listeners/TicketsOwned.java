package com.quietwind01.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quietwind01.QLottery;
import static com.quietwind01.Utils.Formatting.formatNumber;
import static com.quietwind01.Utils.Formatting.playerMessage;

public class TicketsOwned implements CommandExecutor {

    private final QLottery plugin;

    public TicketsOwned(QLottery plugin) {

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
        if (!player.hasPermission("qlottery.ticket.count")) {
            playerMessage(player, plugin.chatPrefix + "{red}You do not have permission to use this command.");
            return true;
        }

        // Debug
        //playerMessage(player, "TicketsOwned Args: " + args.length);

        // Check that the command has valid amount of arguments
        if (args.length < 1) {
            playerMessage(player, plugin.chatPrefix + "{yellow}Usage: /ql tickets");
            return true;
        }

        // Check if the argument is 'tickets'
        if (!args[0].equalsIgnoreCase("tickets")) {
            playerMessage(player, plugin.chatPrefix + "{yellow}Usage: /ql tickets");
            return true;
        }

        // Notify player
        Integer ticketsCount = plugin.playerTickets.get(player.getName());
        if (ticketsCount == null) {
            ticketsCount = 0;
        }
        playerMessage(player, plugin.chatPrefix + "You own {green}" + formatNumber(ticketsCount) + " {white}tickets!");

        return true;

    }

}
