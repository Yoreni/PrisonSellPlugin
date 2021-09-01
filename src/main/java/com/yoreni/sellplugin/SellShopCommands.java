package com.yoreni.sellplugin;

import com.yoreni.sellplugin.inventory.ListShops;
import com.yoreni.sellplugin.inventory.ShopInfo;
import com.yoreni.sellplugin.utils.Placeholder;
import com.yoreni.sellplugin.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SellShopCommands implements CommandExecutor, TabCompleter
{
    SellPlugin main = null;

    public SellShopCommands(SellPlugin main)
    {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if(args.length == 0)
        {
            showHelpMenu(sender);
            return true;
        }

        if(args[0].equalsIgnoreCase("create"))
        {
            if(args.length < 2)
            {
                sender.sendMessage("Usage: /sellshops create <name>");
                return true;
            }

            if(Shop.get(args[1]) == null)
            {
                Shop shop = new Shop(args[1]);
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-created-success",
                        new Placeholder("%shop%", shop.getName()));
            }
            else
            {
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-already-exists",
                        new Placeholder("%shop%", args[1]));
            }

            return true;
        }
        else if(args[0].equalsIgnoreCase("add"))
        {
            if(args.length < 4)
            {
                sender.sendMessage("Usage: /sellshops create <name> <item> <price>");
                return true;
            }

            Shop shop = Shop.get(args[1]);
            if(shop == null)
            {
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-doesnt-exist");
                return true;
            }

            Material item = Material.matchMaterial(args[2]);
            if(item == null)
            {
                SellPlugin.getMessageHandler().sendMessage(sender,"item-doesnt-exist");
                return true;
            }

            if(!Utils.isValidDouble(args[3]))
            {
                SellPlugin.getMessageHandler().sendMessage(sender, "invalid-number-input");
                return true;
            }
            double price = Double.parseDouble(args[3]);

            shop.addItem(item, price);
            SellPlugin.getMessageHandler().sendMessage((Player) sender,"add-item-to-shop-success",
                new Placeholder("%item%", Utils.materialToEnglish(item)),
                new Placeholder("%price%", Utils.formatMoney(price, true)),
                new Placeholder("%shop%", shop.getName()));

            return true;
        }
        else if(args[0].equalsIgnoreCase("remove"))
        {
            if(args.length < 3)
            {
                sender.sendMessage("Usage: /sellshops remove <name> <item>");
                return true;
            }

            Shop shop = Shop.get(args[1]);
            if(shop == null)
            {
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-doesnt-exist");
                return true;
            }

            if(args[2].equals("*"))
            {
                shop.removeEveryItem();
                SellPlugin.getMessageHandler().sendMessage(sender, "remove-every-item-from-shop-success",
                        new Placeholder("%shop%", shop.getName()));
            }

            Material item = Material.matchMaterial(args[2]);
            if(item == null)
            {
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"item-doesnt-exist");
                return true;
            }
            if(!shop.buysItem(item))
            {
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-doesnt-buy-item",
                        new Placeholder("%item%", Utils.materialToEnglish(item)),
                        new Placeholder("%shop%", shop.getName()));
                return true;
            }

            shop.removeItem(item);
            SellPlugin.getMessageHandler().sendMessage((Player) sender,"remove-item-from-shop-success",
                    new Placeholder("%item%", Utils.materialToEnglish(item)),
                    new Placeholder("%shop%", shop.getName()));
            return true;
        }
        else if(args[0].equalsIgnoreCase("info"))
        {
            if(args.length < 2)
            {
                sender.sendMessage("Usage: /sellshops info <shop>");
                return true;
            }

            Shop shop = Shop.get(args[1]);
            if(shop == null)
            {
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-doesnt-exist");
                return true;
            }

            //displays what the shop is selling in an invertory
            Player player = (Player) sender;
            ShopInfo inv = new ShopInfo(shop);
            inv.open(player);
            return true;
        }
        else if(args[0].equalsIgnoreCase("list"))
        {
            if(sender instanceof Player)
            {
                Player player = (Player) sender;
                new ListShops().open(player);
            }
        }
        else if(args[0].equalsIgnoreCase("settings"))
        {
            if (args.length < 3)
            {
                sender.sendMessage("Usage: /sellshops settings <shop> <setting> (value)");
                return true;
            }

            Shop shop = Shop.get(args[1]);
            if (shop == null)
            {
                SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-doesnt-exist");
                return true;
            }

            if (args[2].equalsIgnoreCase("setParent"))
            {
                if (args.length < 4)
                {
                    if(shop.getParent() != null)
                    {
                        try
                        {
                            shop.setParent(null);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        SellPlugin.getMessageHandler().sendMessage((Player) sender,"remove-parent-from-shop-success",
                                new Placeholder("%shop%", shop.getName()));
                    }
                    else
                    {
                        SellPlugin.getMessageHandler().sendMessage((Player) sender,"remove-parent-from-shop-fail",
                                new Placeholder("%shop%", shop.getName()));
                    }
                    return true;
                }

                Shop parent = Shop.get(args[3]);
                if (parent == null)
                {
                    SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-doesnt-exist");
                    return true;
                }

                try
                {
                    shop.setParent(parent);
                    SellPlugin.getMessageHandler().sendMessage((Player) sender,"add-parent-to-shop-success",
                            new Placeholder("%shop%", shop.getName()),
                            new Placeholder("%parentshop%", parent.getName()));
                }
                catch (Exception e)
                {
                    SellPlugin.getMessageHandler().sendMessage((Player) sender,"shop-cant-inherit-shop");
                }
                return true;
            }
        }
        else if(args[0].equalsIgnoreCase("help"))
        {
            showHelpMenu(sender);
        }
        return false;
    }

    public void showHelpMenu(CommandSender sender)
    {
        SellPlugin.getMessageHandler().sendMessage(sender, "help-menu");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        if(!sender.hasPermission("sellshops.admin"))
        {
            return null;
        }

        List<String> showOnTabComplete = new ArrayList<String>();

        if (args.length == 1)
        {
            String[] commands = {"create","add", "remove", "settings", "info", "list", "help"};
            StringUtil.copyPartialMatches(args[0], Arrays.asList(commands), showOnTabComplete);
            return showOnTabComplete;
        }
        else if (args.length == 2)
        {
            final List<String> subcommandBlacklist = Arrays.asList("create", "list", "help");
            if(!subcommandBlacklist.contains(args[0]))
            {
                List<String> options = new ArrayList<String>();
                for(Shop shop : Shop.getShops())
                {
                    options.add(shop.getName());
                }

                StringUtil.copyPartialMatches(args[1], options, showOnTabComplete);
                return showOnTabComplete;
            }
            else //this is so nothing gets returned to the player cos there is nothing more to type
            {
                return new ArrayList<String>();
            }
        }
        else if(args.length == 3)
        {
            if(args[0].equalsIgnoreCase("add"))
            {
                List<String> options = Utils.getListOfItems();
                StringUtil.copyPartialMatches(args[2], options, showOnTabComplete);
                return showOnTabComplete;
            }
            else if(args[0].equalsIgnoreCase("remove"))
            {
                Shop shop = Shop.get(args[1]);
                if (shop != null)
                {
                    List<Material> items = shop.getBuying();
                    List<String> options = new ArrayList<String>();
                    for (Material item : items)
                    {
                        String materialName = item.getKey().asString().split(":")[1];
                        options.add(materialName);
                    }

                    StringUtil.copyPartialMatches(args[2], options, showOnTabComplete);
                    return showOnTabComplete;
                }
            }
            else if(args[0].equalsIgnoreCase("settings"))
            {
                String[] commands = {"setParent"};
                StringUtil.copyPartialMatches(args[2], Arrays.asList(commands), showOnTabComplete);
                return showOnTabComplete;
            }
        }
        return null;
    }
}
