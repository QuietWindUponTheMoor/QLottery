package com.quietwind01;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.quietwind01.Listeners.CommandManager;
import com.quietwind01.Utils.EconomyUtils;
import com.quietwind01.YAML.PlayerStats;

public class QLottery extends JavaPlugin {

    public ConcurrentHashMap<String, Integer> playerTickets;
    public double totalPool;
    public double poolDefaultAmount = getConfig().getDouble("pool-default-amount");
    public int drawInterval = getConfig().getInt("draw-interval");
    private final AtomicInteger interval;
    ConcurrentHashMap<String, String> allTickets = new ConcurrentHashMap<>();
    private int taskID = -1;
    public String chatPrefix = "§5[§a§l§nQLottery§5]§f "; // Get plugin name for chat
    private PlayerStats stats;

    public QLottery() {

        totalPool = poolDefaultAmount;
        interval = new AtomicInteger(drawInterval);

    }

    private boolean saveStatsYAML() {

        try {

            // QLottery folder
            File dir = getDataFolder();

            // Create file object
            File file = new File(dir, "stats.yml");

            // Create the file itself, if it doesn't already exist
            if (!file.exists()) {
                file.createNewFile();
            }

            // Add file contents
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config = createStatsCode(config);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private FileConfiguration createStatsCode(FileConfiguration config) {

        config.getString("# The amount of money paid out TOTAL over the lifespan of the server");
        config.getString("all-time-payout-total");
        config.set("all-time-payout-total", 0);
        config.getString("# The amount of total draws over the lifespan of the server");
        config.getString("all-time-draws-total");
        config.set("all-time-draws-total", 0);
        config.getString("# The amount of total wins over the lifespan of the server");
        config.getString("all-time-wins");
        config.set("all-time-wins", 0);
        config.getString("# Player stats");
        config.getString("player-stats.__LOTTERY__.total-tickets-purchased");
        config.set("player-stats.__LOTTERY__.total-tickets-purchased", 0);

        return config;

    }
    
    @Override
    public final void onEnable() {

        try {

            // Initialize hashmaps
            playerTickets = new ConcurrentHashMap<>();

            // Create the config file if it doesn't exist
            saveDefaultConfig();

            // Create the stats.yml file if it doesn't exist
            saveStatsYAML();

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

                // Insert a bunch of arbitrary records in the playerTickets map
                // This is for making it possible for there to not always be a winner :)
                int totalCurrentTickets = playerTickets.values().stream().mapToInt(Integer::intValue).sum();
                double arbitraryAmount_ = totalCurrentTickets * getConfig().getDouble("arbitrary-tickets-multiplier");
                Integer arbitraryAmount = (int) Math.ceil(arbitraryAmount_);
                playerTickets.put("__LOTTERY__", arbitraryAmount);
                
                // Update __LOTTERY__'s total-tickets-purchased
                stats.updateTotalTicketsPurchased("__LOTTERY__", arbitraryAmount_);

                // Select a 1st place winner
                String playerName = selectWinner();

                // Before going further, check if the winner is __LOTTERY__
                if (playerName.equals("__LOTTERY__")) {
                    // Calculate the new pool size and set it
                    updateTotalPool(poolDefaultAmount); // This will add the old pool amount to the starting pool amount for the next draw

                    // Announce that nobody won, and that the timer will reset and the pool will transfer over
                    Bukkit.getServer().broadcastMessage(chatPrefix + "§3Nobody won this draw. The timer will restart. The pool is now §a$" + totalPool + "§3!");

                    // Reset playerTickets, and allTickets and restart
                    clearTempDataOnNullDraw();

                    // Restart DrawTimer
                    stopTimer(); // Stop previous timer
                    AtomicInteger newInterval = new AtomicInteger(drawInterval);
                    startMainTimer(newInterval);
                    return;
                }
                Player player = Bukkit.getPlayer(playerName);


                // 2nd Place
                String playerName2nd = select2ndOr3rdWinner();
                Player player2nd = Bukkit.getPlayer(playerName2nd);
                // 3rd Place
                String playerName3rd = select2ndOr3rdWinner();
                Player player3rd = Bukkit.getPlayer(playerName3rd);
                String playerBonusName = select2ndOr3rdWinner();
                Player playerBonus = Bukkit.getPlayer(playerBonusName);

                // Calculate amount to add from multiplier
                double payoutTaxMultiplier = getConfig().getDouble("payout-tax");
                double drawMultiplier = getConfig().getDouble("draw-multiplier");
                double amountToAddFromMultiplier = totalPool * drawMultiplier;

                // Get payout amounts & update players' stats
                double payout = totalPool + amountToAddFromMultiplier;
                double payoutTax = payout * payoutTaxMultiplier;
                payout -= payoutTax;
                double p1CurrentWon = stats.getTotalAmountWon(playerName);
                stats.updateTotalAmountWon(playerName, p1CurrentWon + payout);
                stats.updateTotalWins(playerName, 1);
                stats.updateServerWinsTotal(1);
                stats.updateServerPayoutTotal(payout);
                double payout2nd = 0.00;
                double payout2ndTax = payout2nd * payoutTaxMultiplier;
                double payout3rd = 0.00;
                double payout3rdTax = payout3rd * payoutTaxMultiplier;
                double payoutBonus = 0.00;
                if (playersCount > 2) {
                    payout2nd = payout * getConfig().getDouble("second-place-multiplier");
                    payout2nd -= payout2ndTax;
                    double p2CurrentWon = stats.getTotalAmountWon(playerName2nd);
                    stats.updateTotalAmountWon(playerName2nd, p2CurrentWon + payout2nd);
                    stats.updateTotalWins(playerName2nd, 1);
                    stats.updateServerWinsTotal(1);
                    stats.updateServerPayoutTotal(payout2nd);
                }
                if (playersCount > 3) {
                    payout3rd = payout * getConfig().getDouble("third-place-multiplier");
                    payout3rd -= payout3rdTax;
                    double p3CurrentWon = stats.getTotalAmountWon(playerName3rd);
                    stats.updateTotalAmountWon(playerName3rd, p3CurrentWon + payout3rd);
                    stats.updateTotalWins(playerName3rd, 1);
                    stats.updateServerWinsTotal(1);
                    stats.updateServerPayoutTotal(payout3rd);
                }
                if (playersCount > 4) {
                    payoutBonus = getConfig().getDouble("bonus-winner-amount"); // NOT TAXED
                    double pBonusCurrentWon = stats.getTotalAmountWon(playerBonusName);
                    stats.updateTotalAmountWon(playerBonusName, pBonusCurrentWon + payoutBonus);
                    stats.updateTotalWins(playerBonusName, 1);
                    stats.updateServerWinsTotal(1);
                    stats.updateServerPayoutTotal(payoutBonus);
                }

                // Update server total draws
                stats.updateServerDrawsTotal(1);

                // Announce winners
                Bukkit.getServer().broadcastMessage(chatPrefix + "" + player.getDisplayName() + "§3won first place with §a$" + payout + "§3! §6Congratulations!");
                if (payout2nd > 0) {
                    Bukkit.getServer().broadcastMessage(chatPrefix + "" + player2nd.getDisplayName() + "§3won second place with §a$" + payout2nd + "§3! §6Congratulations!");
                }
                if (payout3rd > 0) {
                    Bukkit.getServer().broadcastMessage(chatPrefix + "" + player3rd.getDisplayName() + "§3won third place with §a$" + payout3rd + "§3! §6Congratulations!");
                }
                if (payoutBonus > 0) {
                    Bukkit.getServer().broadcastMessage(chatPrefix + "" + playerBonus.getDisplayName() + "§3won the bonus prize with §a$" + payoutBonus + "§3! §6Congratulations!");
                }

                // Payouts
                EconomyUtils econUtility = new EconomyUtils(this);
                econUtility.addBalance(player, payout);
                player.sendMessage(chatPrefix + "§a" + payout + "§fhas been added to your account! Congratulations! You now have §a$" + econUtility.getPlayerBalance(player) + "§f.");
                player.sendMessage(chatPrefix + "§eYou were taxed §c$" + payoutTax + " §efor this prize.");
                if (payout2nd > 0) {
                    econUtility.addBalance(player2nd, payout2nd);
                    player.sendMessage(chatPrefix + "§a" + payout2nd + "§fhas been added to your account! Congratulations! You now have §a$" + econUtility.getPlayerBalance(player2nd) + "§f.");
                    player.sendMessage(chatPrefix + "§eYou were taxed §c$" + payout2ndTax + " §efor this prize.");
                }
                if (payout3rd > 0) {
                    econUtility.addBalance(player2nd, payout3rd);
                    player.sendMessage(chatPrefix + "§a" + payout3rd + "§fhas been added to your account! Congratulations! You now have §a$" + econUtility.getPlayerBalance(player3rd) + "§f.");
                    player.sendMessage(chatPrefix + "§eYou were taxed §c$" + payout3rdTax + " §efor this prize.");
                }
                if (payoutBonus > 0) {
                    econUtility.addBalance(playerBonus, payoutBonus);
                    player.sendMessage(chatPrefix + "§a" + payoutBonus + "§fhas been added to your account! Congratulations! You now have §a$" + econUtility.getPlayerBalance(playerBonus) + "§f.");
                    player.sendMessage(chatPrefix + "§eYou were taxed §c$0 §efor this prize."); // Remember, not taxed :)
                }

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

    private void clearTempDataOnNullDraw() {

        // Reset playerTickets, and allTickets
        playerTickets.clear();
        allTickets.clear();

    }

    private String select2ndOr3rdWinner() {

        String player2nd = selectWinner();
        if (player2nd.equals("__LOTTERY__")) {
            return select2ndOr3rdWinner();
        }
        return player2nd;

    }

    private void clearTempData() {

        // Reset totalPool, playerTickets, and allTickets
        totalPool = poolDefaultAmount;
        playerTickets.clear();
        allTickets.clear();

    }
}