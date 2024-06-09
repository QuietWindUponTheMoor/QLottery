package com.quietwind01.Timers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.quietwind01.Utils.EconomyUtils;
import com.quietwind01.QLottery;
import com.quietwind01.YAML.PlayerStats;
import static com.quietwind01.Utils.Formatting.formatNumber;
import static com.quietwind01.Utils.Formatting.broadcast;
import static com.quietwind01.Utils.Formatting.playerMessage;

public class MainDrawTimer {
    
    QLottery plugin;
    PlayerStats stats;
    private int taskID = -1;
    private int drawInterval;
    private double poolDefaultAmount;
    private AtomicInteger interval;
    ConcurrentHashMap<String, String> allTickets = new ConcurrentHashMap<>();

    public MainDrawTimer(QLottery plugin) {

        this.plugin = plugin;
        this.stats = new PlayerStats(plugin.getDataFolder());
        this.drawInterval = plugin.getConfig().getInt("draw-interval");
        this.poolDefaultAmount = plugin.getConfig().getDouble("pool-default-amount");
        this.interval = new AtomicInteger(this.drawInterval);

    }

    public void startMainTimer(AtomicInteger interval) {

        // Check if interval is valid. Minimum draw time is 10 seconds
        if (interval.get() < 10) {
            broadcast(plugin.chatPrefix + "{red}FATAL ERROR: 'draw-interval' must be higher than 10 in the config file. It is currently set to {white}" + formatNumber(interval.get()) + "{red}. Disabling QLottery...");
            plugin.disablePlugin();
            return;
        }

        taskID = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            // Current interval
            int currentInterval = interval.decrementAndGet();

            // Check if timer is finished
            if (currentInterval <= 0) {

                // Check that enough players actually purchased tickets. Minimum of 2 players have to purchase tickets
                int playersCount = plugin.playerTickets.size();
                if (playersCount < 2 && playersCount > 0) {
                    plugin.updateTotalPool(poolDefaultAmount); // This will add the old pool amount to the starting pool amount for the next draw
                    broadcast(plugin.chatPrefix + "{darkaqua}Nobody won this draw. The timer will restart. The pool is now {green}$" + formatNumber(plugin.totalPool) + "{darkaqua}!");
                    // Clear temp data
                    clearTempData();

                    // Restart DrawTimer
                    stopTimer(); // Stop previous timer
                    AtomicInteger newInterval = new AtomicInteger(drawInterval);
                    startMainTimer(newInterval);
                    return;
                }

                // Announce that lottery is drawing
                broadcast(plugin.chatPrefix + "{green}It's time to select a winner! Drawing...");

                // Check if anybody purchased tickets
                boolean nobodyPurchased = plugin.playerTickets.isEmpty();
                if (nobodyPurchased == true) {
                    plugin.updateTotalPool(poolDefaultAmount); // This will add the old pool amount to the starting pool amount for the next draw
                    broadcast(plugin.chatPrefix + "{darkaqua}Nobody won this draw. The timer will restart. The pool is now {green}$" + formatNumber(plugin.totalPool) + "{darkaqua}!");
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
                int totalCurrentTickets = plugin.playerTickets.values().stream().mapToInt(Integer::intValue).sum();
                double arbitraryAmount_ = totalCurrentTickets * plugin.getConfig().getDouble("arbitrary-tickets-multiplier");
                Integer arbitraryAmount = (int) Math.ceil(arbitraryAmount_);
                plugin.playerTickets.put("__LOTTERY__", arbitraryAmount);
                
                // Update __LOTTERY__'s total-tickets-purchased
                stats.updateTotalTicketsPurchased("__LOTTERY__", arbitraryAmount_);

                // Select a 1st place winner
                String playerName = selectWinner();

                // Before going further, check if the winner is __LOTTERY__
                if (playerName.equals("__LOTTERY__")) {
                    // Calculate the new pool size and set it
                    plugin.updateTotalPool(poolDefaultAmount); // This will add the old pool amount to the starting pool amount for the next draw

                    // Announce that nobody won, and that the timer will reset and the pool will transfer over
                    broadcast(plugin.chatPrefix + "{darkaqua}Nobody won this draw. The timer will restart. The pool is now {green}$" + formatNumber(plugin.totalPool) + "{darkaqua}!");

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
                double payoutTaxMultiplier = plugin.getConfig().getDouble("payout-tax");
                double drawMultiplier = plugin.getConfig().getDouble("draw-multiplier");
                double amountToAddFromMultiplier = plugin.totalPool * drawMultiplier;

                // Get payout amounts & update players' stats
                double payout = plugin.totalPool + amountToAddFromMultiplier;
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
                    payout2nd = payout * plugin.getConfig().getDouble("second-place-multiplier");
                    payout2nd -= payout2ndTax;
                    double p2CurrentWon = stats.getTotalAmountWon(playerName2nd);
                    stats.updateTotalAmountWon(playerName2nd, p2CurrentWon + payout2nd);
                    stats.updateTotalWins(playerName2nd, 1);
                    stats.updateServerWinsTotal(1);
                    stats.updateServerPayoutTotal(payout2nd);
                }
                if (playersCount > 3) {
                    payout3rd = payout * plugin.getConfig().getDouble("third-place-multiplier");
                    payout3rd -= payout3rdTax;
                    double p3CurrentWon = stats.getTotalAmountWon(playerName3rd);
                    stats.updateTotalAmountWon(playerName3rd, p3CurrentWon + payout3rd);
                    stats.updateTotalWins(playerName3rd, 1);
                    stats.updateServerWinsTotal(1);
                    stats.updateServerPayoutTotal(payout3rd);
                }
                if (playersCount > 4) {
                    payoutBonus = plugin.getConfig().getDouble("bonus-winner-amount"); // NOT TAXED
                    double pBonusCurrentWon = stats.getTotalAmountWon(playerBonusName);
                    stats.updateTotalAmountWon(playerBonusName, pBonusCurrentWon + payoutBonus);
                    stats.updateTotalWins(playerBonusName, 1);
                    stats.updateServerWinsTotal(1);
                    stats.updateServerPayoutTotal(payoutBonus);
                }

                // Update server total draws
                stats.updateServerDrawsTotal(1);

                // Announce winners
                broadcast(plugin.chatPrefix + "" + player.getDisplayName() + " {darkaqua}won first place with {green}$" + formatNumber(payout) + "{darkaqua}! {gold}Congratulations!");
                if (payout2nd > 0) {
                    broadcast(plugin.chatPrefix + "" + player2nd.getDisplayName() + " {darkaqua}won second place with {green}$" + formatNumber(payout2nd) + "{darkaqua}! {gold}Congratulations!");
                }
                if (payout3rd > 0) {
                    broadcast(plugin.chatPrefix + "" + player3rd.getDisplayName() + " {darkaqua}won third place with {green}$" + formatNumber(payout3rd) + "{darkaqua}! {gold}Congratulations!");
                }
                if (payoutBonus > 0) {
                    broadcast(plugin.chatPrefix + "" + playerBonus.getDisplayName() + " {darkaqua}won the bonus prize with {green}$" + formatNumber(payoutBonus) + "{darkaqua}! {gold}Congratulations!");
                }

                // Payouts
                EconomyUtils econUtility = new EconomyUtils(plugin);
                econUtility.addBalance(player, payout);
                playerMessage(player, plugin.chatPrefix + "{green}" + payout + "{white}has been added to your account! Congratulations! You now have {green}$" + formatNumber(econUtility.getPlayerBalance(player)) + "{white}.");
                playerMessage(player, plugin.chatPrefix + "{yellow}You were taxed {red}$" + formatNumber(payoutTax) + " {yellow}for this prize.");
                if (payout2nd > 0) {
                    econUtility.addBalance(player2nd, payout2nd);
                    playerMessage(player, plugin.chatPrefix + "{green}" + payout2nd + "{white}has been added to your account! Congratulations! You now have {green}$" + formatNumber(econUtility.getPlayerBalance(player2nd)) + "{white}.");
                    playerMessage(player, plugin.chatPrefix + "{yellow}You were taxed {red}$" + formatNumber(payout2ndTax) + " {yellow}for this prize.");
                }
                if (payout3rd > 0) {
                    econUtility.addBalance(player2nd, payout3rd);
                    playerMessage(player, plugin.chatPrefix + "{green}" + payout3rd + "{white}has been added to your account! Congratulations! You now have {green}$" + formatNumber(econUtility.getPlayerBalance(player3rd)) + "{white}.");
                    playerMessage(player, plugin.chatPrefix + "{yellow}You were taxed {red}$" + formatNumber(payout3rdTax) + " {yellow}for this prize.");
                }
                if (payoutBonus > 0) {
                    econUtility.addBalance(playerBonus, payoutBonus);
                    playerMessage(player, plugin.chatPrefix + "{green}" + payoutBonus + "{white}has been added to your account! Congratulations! You now have {green}$" + formatNumber(econUtility.getPlayerBalance(playerBonus)) + "{white}.");
                    playerMessage(player, plugin.chatPrefix + "{yellow}You were taxed {red}$0 {yellow}for this prize."); // Remember, not taxed :)
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

    private void timeLeft(int currentInterval) {

        // Get percentages of interval
        double P50 = interval.get() * 0.5;
        double P25 = interval.get() * 0.25;
        double P15 = interval.get() * 0.15;
        double P05 = interval.get() * 0.05;

        // Check time left is equal to one of the percentages
        if (currentInterval == P50) {
            broadcast(plugin.chatPrefix + "{white}" + formatNumber(currentInterval) + "{gold} seconds left before the lottery is drawn!");
        }
        if (currentInterval == P25) {
            broadcast(plugin.chatPrefix + "{white}" + formatNumber(currentInterval) + "{gold} seconds left before the lottery is drawn!");
        }
        if (currentInterval == P15) {
            broadcast(plugin.chatPrefix + "{white}" + formatNumber(currentInterval) + "{gold} seconds left before the lottery is drawn!");
        }
        if (currentInterval == P05) {
            broadcast(plugin.chatPrefix + "{white}" + formatNumber(currentInterval) + "{gold} seconds left before the lottery is drawn!");
        }

    }

    private void clearTempDataOnNullDraw() {

        // Reset playerTickets, and allTickets
        plugin.playerTickets.clear();
        allTickets.clear();

    }

    private String select2ndOr3rdWinner() {

        String player2nd = selectWinner();
        if (player2nd.equals("__LOTTERY__")) {
            return select2ndOr3rdWinner();
        }
        return player2nd;

    }

    private String selectWinner() {

        // Populate allTickets from playerTickets
        for (Map.Entry<String, Integer> entry : plugin.playerTickets.entrySet()) {
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

        // Reset totalPool, playerTickets, and allTickets
        plugin.updateTotalPool(poolDefaultAmount);
        plugin.playerTickets.clear();
        allTickets.clear();

    }

    private void stopTimer() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1; // Reset the task ID
        }
    }

}
