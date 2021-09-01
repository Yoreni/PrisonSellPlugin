package com.yoreni.sellplugin;

import com.yoreni.sellplugin.inventory.MenuListener;
import com.yoreni.sellplugin.utils.MessageHandler;
import com.yoreni.sellplugin.utils.Yml;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SellPlugin extends JavaPlugin
{
    private static SellPlugin instance;
    private static MessageHandler messageHandler;
    private static Yml config;
    private static Economy econ = null;

    @Override
    public void onEnable()
    {
        instance = this;

        //creating the plugins config folder
        String pluginPath = "plugins/" + SellPlugin.getInstance().getName();
        File pluginDir = new File(pluginPath);
        pluginDir.mkdir();

        //init the config files
        messageHandler = new MessageHandler(this);
        config = new Yml(this, "config");
        config.setDefaultsFromJar();

        setupEconomy();

        this.getCommand("sellshops").setExecutor(new SellShopCommands(this));
        this.getCommand("sellshops").setTabCompleter(new SellShopCommands(this));

        Shop.initShops();
        Bukkit.getLogger().info(String.format("Loaded %d shop(s).", Shop.getShops().size()));

        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new SellSigns(), this);
    }

    @Override
    public void onDisable()
    {
        Shop.saveShops();
    }

    public Economy getEconomy()
    {
        return econ;
    }

    public static SellPlugin getInstance()
    {
        return instance;
    }

    public static MessageHandler getMessageHandler()
    {
        return messageHandler;
    }

    public static Yml config()
    {
        return config;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player))
        {
            return true;
        }

        Player player = (Player) sender;

        if (command.getLabel().equals("sellall"))
        {
            if(args.length < 1)
            {
                sender.sendMessage("Usage: /sellall <shop>");
                return true;
            }

            Shop shop = Shop.get(args[0]);
            if(shop == null)
            {
                getMessageHandler().sendMessage((Player) sender,"shop-doesnt-exist");
                return true;
            }

            shop.sellAll(player);
            return true;
        }

        return false;
    }

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
