package net.broder.trades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public final class Trades extends JavaPlugin {
    String balance_path = "plugins/Trades/balances.json";
    String balance_dir = "plugins/Trades";
    Logger logger;
    TreeMap<UUID, Balance> player_balances = new TreeMap<>();
    Map<UUID, Balance>NPC_balances = null;
    Server server;
    HashMap<Integer, Commands.Shop.ShopItem>prices = new HashMap<>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = getServer().getLogger();
        UploadBalance();
        server = getServer();
        // register commands
        logger.severe("NIGGER");
        getCommand("balance").setExecutor(new Commands.Balance(logger, player_balances));
        getCommand("earn").setExecutor(new Commands.Earn(logger, player_balances));
        getCommand("shop").setExecutor(new Commands.Shop(logger, player_balances, server, prices));
        getCommand("baltop").setExecutor(new Commands.Baltop(logger, player_balances, server));
        getCommand("pay").setExecutor(new Commands.Pay(logger, player_balances, server));
        for (String commandName : getDescription().getCommands().keySet())
            getLogger().severe("Registered command: " + commandName);
        // register listeners
        getServer().getPluginManager().registerEvents(new Listeners.PlayerFirstJoinListener(logger, player_balances), this);
        getServer().getPluginManager().registerEvents(new Listeners.ShopInventoryListener(logger, player_balances, server, prices), this);

        // add items to shop
        prices.put(0, new Commands.Shop.ShopItem(new ItemStack(Material.BIRCH_WOOD, 1), 4, 2));
        prices.put(1, new Commands.Shop.ShopItem(new ItemStack(Material.CHERRY_WOOD, 1), 4, 2));
        prices.put(2, new Commands.Shop.ShopItem(new ItemStack(Material.ACACIA_WOOD, 1), 4, 2));
        prices.put(3, new Commands.Shop.ShopItem(new ItemStack(Material.DARK_OAK_WOOD, 1), 4, 2));
        prices.put(4, new Commands.Shop.ShopItem(new ItemStack(Material.JUNGLE_WOOD, 1), 4, 2));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        File folder = new File(balance_dir);
        if (!folder.exists())
            folder.mkdir();
        try(FileWriter writer = new FileWriter(balance_path)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(player_balances));
            writer.flush();
        }
        catch (IOException ex) {
            logger.severe(ex.toString());
        }
    }
    public void UploadBalance() {
        Path path = Paths.get(balance_path);
        if (Files.exists(path)) {
            logger.severe("IF");
           try {
               Gson gson = new GsonBuilder().setPrettyPrinting().create();
               FileReader reader = new FileReader(balance_path);
               Type type = new TypeToken<TreeMap<UUID, Balance>>() {}.getType();
               player_balances = gson.fromJson(reader, type);
               reader.close();
           }
           catch (IOException ex) {
               logger.severe(ex.toString());
           }

        }
        else {
            logger.severe("ELES");
            Balance balance = new Balance(0);
            for (OfflinePlayer player : getServer().getOfflinePlayers())
                player_balances.put(player.getUniqueId(), balance);
        }
        logger.severe(String.valueOf(player_balances.size()));
        for (Map.Entry<UUID, Balance>entry : player_balances.entrySet()) {
            logger.severe(entry.getKey() + ":" + entry.getValue().getBalance());
        }
    }

}