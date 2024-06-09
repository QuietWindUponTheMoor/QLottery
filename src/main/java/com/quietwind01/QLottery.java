package com.quietwind01;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.quietwind01.Listeners.CommandManager;
import com.quietwind01.Timers.MainDrawTimer;
import static com.quietwind01.Utils.Formatting.broadcast;

public class QLottery extends JavaPlugin {

    public String chatPrefix = "{purple}[{green}{bold}{underline}QLottery{purple}]{white} "; // Get plugin name for chat
    public double totalPool;
    public ConcurrentHashMap<String, Integer> playerTickets = new ConcurrentHashMap<>();
    
    @Override
    public final void onEnable() {

        try {

            // Create the config file if it doesn't exist
            saveDefaultConfig();

            // Create the stats.yml file if it doesn't exist
            saveStatsYAML();

            // Send startup messages
            startMessages(chatPrefix);

            // Setup default/total pool
            this.totalPool = getConfig().getDouble("pool-default-amount");

            // Start DrawInterval timer
            MainDrawTimer mainDrawTimer = new MainDrawTimer(this);
            AtomicInteger startingMainInterval = new AtomicInteger(getConfig().getInt("draw-interval"));
            mainDrawTimer.startMainTimer(startingMainInterval);

            // Register commands
            getCommand("ql").setExecutor(new CommandManager(this, getDataFolder()));

        } catch (Exception e) {

            broadcast(chatPrefix + "{red}Something went severely wrong. Please check logs. QLottery has been disabled.");
            e.printStackTrace();
            disablePlugin();
            return;

        }

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

            // Save the config
            config.save(file);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private FileConfiguration createStatsCode(FileConfiguration config) {

        //config.getString("all-time-payout-total");
        config.addDefault("all-time-payout-total", 0);
        //config.getString("all-time-draws-total");
        config.addDefault("all-time-draws-total", 0);
        //config.getString("all-time-wins");
        config.addDefault("all-time-wins", 0);
        //config.getString("player-stats.__LOTTERY__.total-tickets-purchased");
        config.addDefault("player-stats.__LOTTERY__.total-tickets-purchased", 0);

        config.options().copyDefaults(true);

        return config;

    }

    public void disablePlugin() {

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.disablePlugin(this);

    }

    private void startMessages(String chatPrefix) {
        broadcast(chatPrefix + "{darkgreen}The lottery has started!");
    }

    public boolean updateTotalPool(double addAmount) {

        this.totalPool += addAmount;

        return true;
    }

    public ConcurrentHashMap<String, Integer> getPlayerTickets() {
        return this.playerTickets;
    }

}