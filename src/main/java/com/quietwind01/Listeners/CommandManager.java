package com.quietwind01.Listeners;

import com.quietwind01.QLottery;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {

    private final QLottery plugin;
    boolean debugMode = false;
    File dataFolder;

    public CommandManager(QLottery plugin, File dataFolder) {

        this.plugin = plugin;
        this.dataFolder = dataFolder;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Check args length
        if (args.length == 0) {
            sender.sendMessage(plugin.chatPrefix + "§cUsage: /ql <subcommand>");
            return true;
        }

        // Get parent subcommand
        String parentSubcommand = args[0].toLowerCase();
        String childSubcommand = "";
        if (args.length > 1) {
            childSubcommand = args[1].toLowerCase();
        }

        if (debugMode == true) {
            Player player = (Player) sender;
            player.sendMessage(plugin.chatPrefix + "Parent subcommand: " + parentSubcommand);
            player.sendMessage(plugin.chatPrefix + "Child subcommand: " + childSubcommand);
            player.sendMessage(plugin.chatPrefix + "Args Length: " + args.length);
            player.sendMessage(plugin.chatPrefix + "Is Ticket Command: " + "ticket".equals(parentSubcommand));
            player.sendMessage(plugin.chatPrefix + "Is Pool Command: " + "pool".equals(parentSubcommand));
            player.sendMessage(plugin.chatPrefix + "Is Stats Command: " + "stats".equals(parentSubcommand));
        }

        // Ticket subcommands
        if ("ticket".equals(parentSubcommand)) {
            switch (childSubcommand) {
                case "buy":
                    return new TicketBuy(plugin, dataFolder).onCommand(sender, command, label, args);
                case "sell":
                    return new TicketSell(plugin, dataFolder).onCommand(sender, command, label, args);
                case "cost":
                    return new TicketCost(plugin).onCommand(sender, command, label, args);
                default:
                    sender.sendMessage(plugin.chatPrefix + "§cUnknown subcommand. Usage: /ql <subcommand>");
            }
        }

        // Tickets command
        if ("tickets".equals(parentSubcommand)) {
            return new TicketsOwned(plugin).onCommand(sender, command, label, args);
        }

        // Pool subcommands
        if ("pool".equals(parentSubcommand)) {
            return new TotalPool(plugin).onCommand(sender, command, label, args);
        }

        // Stats command
        if ("stats".equals(parentSubcommand)) {
            return new Statistics(plugin, dataFolder).onCommand(sender, command, label, args);
        }

        return true;

    }
}