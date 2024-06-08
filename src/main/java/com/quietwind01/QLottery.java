package com.quietwind01;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.quietwind01.Listeners.CommandManager;
import com.quietwind01.Utils.EconomyUtils;

public class QLottery extends JavaPlugin {

    public ConcurrentHashMap<String, Integer> playerTickets;
    public double totalPool;
    public double poolDefaultAmount = getConfig().getDouble("pool-default-amount");
    public int drawInterval = getConfig().getInt("draw-interval");
    private final AtomicInteger interval;
    ConcurrentHashMap<String, String> allTickets = new ConcurrentHashMap<>();
    private int taskID = -1;
    public String chatPrefix = "§5[§a§l§nQLottery§5]§f "; // Get plugin name for chat

    public QLottery() {

        totalPool = poolDefaultAmount;
        interval = new AtomicInteger(drawInterval);

    }
    
    @Override
    public final void onEnable() {

        try {

            // Initialize hashmaps
            playerTickets = new ConcurrentHashMap<>();

            // Create the config file if it doesn't exist
            saveDefaultConfig();

            // Send startup messages
            startMessages(this.chatPrefix);

            // Start DrawInterval timer
            startMainTimer(interval);

            // Register commands
            getCommand("ql").setExecutor(new CommandManager(this));

        } catch (Exception e) {

            Bukkit.getServer().broadcastMessage(chatPrefix + "§cSomething went severely wrong. Please check logs. QLottery has been disabled.");
            e.printStackTrace();
            disablePlugin();
            return;

        }

    }

    public void disablePlugin() {

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.disablePlugin(this);

    }

    public void startMessages(String chatPrefix) {
        getServer().broadcastMessage(chatPrefix + "§2The lottery has started!");
    }

    public ConcurrentHashMap<String, Integer> getPlayerTickets() {
        return playerTickets;
    }

    public boolean updateTotalPool(double addAmount) {

        this.totalPool += addAmount;

        return true;
    }

    public void startMainTimer(AtomicInteger interval) {

        // Check if interval is valid. Minimum draw time is 10 seconds
        if (interval.get() < 10) {
            Bukkit.getServer().broadcastMessage(chatPrefix + "§cFATAL ERROR: 'draw-interval' must be higher than 10 in the config file. It is currently set to §f" + interval.get() + "§c. Disabling QLottery...");
            disablePlugin();
            return;
        }

        taskID = Bukkit.getScheduler().runTaskTimer(this, () -> {

            // Current interval
            int currentInterval = interval.decrementAndGet();

            // Check if timer is finished
            if (currentInterval <= 0) {

                // Check that enough players actually purchased tickets. Minimum of 2 players have to purchase tickets
                int playersCount = playerTickets.size();
                if (playersCount < 2) {
                    Bukkit.getServer().broadcastMessage(chatPrefix + "§eUnfortunately, not enough players purchased tickets. The countdown will now reset. Players that already purchased tickets will keep theirs until the next successful draw.");
                    // Clear temp data
                    clearTempData();

                    // Restart DrawTimer
                    stopTimer(); // Stop previous timer
                    AtomicInteger newInterval = new AtomicInteger(drawInterval);
                    startMainTimer(newInterval);
                    return;
                }

                // Announce that lottery is drawing
                Bukkit.getServer().broadcastMessage(chatPrefix + "§aIt's time to select a winner! Drawing...");

                // Check if anybody purchased tickets
                boolean nobodyPurchased = playerTickets.isEmpty();
                if (nobodyPurchased == true) {
                    Bukkit.getServer().broadcastMessage(chatPrefix + "§eUnfortunately, nobody has purchased tickets. The countdown will now reset.");
                    // Clear temp data
                    clearTempData();

                    // Restart DrawTimer
                    stopTimer(); // Stop previous timer
                    AtomicInteger newInterval = new AtomicInteger(drawInterval);
                    startMainTimer(newInterval);
                    return;
                }

                // Select a winner
                String playerName = selectWinner();
                Player player = Bukkit.getPlayer(playerName);

                // Calculate amount to pay out
                double drawMultiplier = getConfig().getDouble("draw-multiplier");
                double amountToAddFromMultiplier = totalPool * drawMultiplier;
                double payout = totalPool + amountToAddFromMultiplier;

                // Announce winner
                Bukkit.getServer().broadcastMessage(chatPrefix + "");

                // Payout
                EconomyUtils econUtility = new EconomyUtils(this);
                econUtility.addBalance(player, payout);
                player.sendMessage(chatPrefix + "§a" + payout + "§fhas been added to your account! Congratulations! You now have §a$" + econUtility.getPlayerBalance(player) + "§f.");

                // Clear temp data
                clearTempData();

                // Restart DrawTimer
                stopTimer(); // Stop previous timer
                AtomicInteger newInterval = new AtomicInteger(drawInterval);
                startMainTimer(newInterval);
                return;
            }

            // Check time left
            timeLeft(currentInterval);

        }, 0L, 20L).getTaskId(); // Run every 20 ticks/1 seconds

    }

    public void stopTimer() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1; // Reset the task ID
        }
    }

    private void timeLeft(int currentInterval) {

        // Get percentages of interval
        double P50 = interval.get() * 0.5;
        double P25 = interval.get() * 0.25;
        double P15 = interval.get() * 0.15;
        double P05 = interval.get() * 0.05;

        // Check time left is equal to one of the percentages
        if (currentInterval == P50) {
            Bukkit.getServer().broadcastMessage(chatPrefix + "§f" + currentInterval + "§6seconds left before the lottery is drawn!");
        }
        if (currentInterval == P25) {
            Bukkit.getServer().broadcastMessage(chatPrefix + "§f" + currentInterval + "§6seconds left before the lottery is drawn!");
        }
        if (currentInterval == P15) {
            Bukkit.getServer().broadcastMessage(chatPrefix + "§f" + currentInterval + "§6seconds left before the lottery is drawn!");
        }
        if (currentInterval == P05) {
            Bukkit.getServer().broadcastMessage(chatPrefix + "§f" + currentInterval + "§6seconds left before the lottery is drawn!");
        }

    }

    private String selectWinner() {

        // Populate allTickets from playerTickets
        for (Map.Entry<String, Integer> entry : playerTickets.entrySet()) {
            String playerName = entry.getKey();
            int ticketCount = entry.getValue();
            for (int i = 1; i <= ticketCount; i++) {
                allTickets.put(playerName + "_" + i, playerName);
            }
        }

        // Select a random ticket
        List<String> keys = new ArrayList<>(allTickets.keySet());
        if (keys.isEmpty()) {
            return null; // No tickets available
        }
        Random random = new Random();
        String randomKey = keys.get(random.nextInt(keys.size()));

        String playerName = allTickets.get(randomKey);
        Player player = Bukkit.getPlayer(playerName);

        // Check validity of player
        if (player == null) {
            selectWinner(); // Draw again
        }

        // Return player's name and now the player object. Faster than encapsulation.
        return playerName;

    }

    private void clearTempData() {

        // Reset totalPool, drawInterval, playerTickets, and allTickets
        totalPool = poolDefaultAmount;
        playerTickets.clear();
        allTickets.clear();

    }
}