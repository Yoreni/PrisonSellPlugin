package com.yoreni.sellplugin;

import com.yoreni.sellplugin.utils.Placeholder;
import com.yoreni.sellplugin.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class SellSigns implements Listener
{
    //TODO add permission nodes on who can create these signs
    @EventHandler
    public void onSignCreation(SignChangeEvent event)
    {
        final String sellAllName = getSellAllSignName();
        final String sellName = getSellSignName();
        final String viewPricesName = getViewPricesSignName();

        if(event.getLine(0).equalsIgnoreCase(uncolour(sellAllName)))
        {
            Shop shop = Shop.get(event.getLine(1));
            if(shop == null)
            {
                event.setLine(0, ChatColor.RED + uncolour(sellAllName));
            }
            else
            {
                event.setLine(0, sellAllName);
            }
        }
        else if(event.getLine(0).equalsIgnoreCase(uncolour(sellName)))
        {
            Shop shop = Shop.get(event.getLine(1));
            Material item = Material.matchMaterial(event.getLine(2));
            if(shop == null || item == null)
            {
                event.setLine(0, ChatColor.RED + uncolour(sellName));
            }
            else
            {
                event.setLine(0, sellName);
                event.setLine(3, "$" + Utils.formatMoney(shop.getPrice(item), true));
            }
        }
        else if(event.getLine(0).equalsIgnoreCase(uncolour(viewPricesName)))
        {
            Shop shop = Shop.get(event.getLine(1));
            if(shop == null)
            {
                event.setLine(0, ChatColor.RED + uncolour(viewPricesName));
            }
            else
            {
                event.setLine(0, viewPricesName);
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if(event.getClickedBlock() == null)
        {
            return;
        }

        if (event.getClickedBlock().getState() instanceof Sign)
        {
            final String sellAllName = getSellAllSignName();
            final String sellName = getSellSignName();
            final String viewPricesName = getViewPricesSignName();

            Sign sign = (Sign) event.getClickedBlock().getState();
            Shop shop = Shop.get(sign.getLine(1));
            if(shop == null)
            {
                return;
            }

            if(sign.getLine(0).equalsIgnoreCase(sellAllName))
            {
                shop.sellAll(player);
            }
            else if(sign.getLine(0).equalsIgnoreCase(sellName))
            {
                Material item = Material.matchMaterial(sign.getLine(2));
                shop.sell(player, item, item.getMaxStackSize());

                //updateing the price on the sign if it has changed
                if(!sign.getLine(3).equals("$" + Utils.formatMoney(shop.getPrice(item), true)))
                {
                    sign.setLine(3, "$" + Utils.formatMoney(shop.getPrice(item), true));
                    sign.update();
                }
            }
            else if(sign.getLine(0).equalsIgnoreCase(viewPricesName))
            {
                ItemStack book = makePriceBook(shop);
                player.openBook(book);
            }
        }
    }

    private String getSellAllSignName()
    {
        return SellPlugin.getMessageHandler().get("sell-all-sign");
    }

    private String getSellSignName()
    {
        return SellPlugin.getMessageHandler().get("sell-sign");
    }

    private String uncolour(String string)
    {
        return ChatColor.stripColor(string);
    }

    private String getViewPricesSignName()
    {
        return SellPlugin.getMessageHandler().get("view-prices-sign");
    }

    //TODO move this to somwhere relevant
    private ItemStack makePriceBook(Shop shop)
    {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        //List<BaseComponent[]> pages = new ArrayList<BaseComponent[]>();

        // you can have 14 lines per book. for each item we will use 2 lines one for the name another for the price
        // 14 / 2 = 7
        final int numOfPages = (int) Math.ceil(shop.getBuying().size() / 7d);

        for(int page = 0; page < numOfPages; page++)
        {
            //this ist of times we will have in this page
            List<Material> items = shop.getBuying().subList(page * 7, Math.min((page + 1) * 7, shop.getBuying().size()));
            String text = "";

            for(int i = 0; i < items.size(); i++)
            {
                final String colour = i % 2 == 0 ? "&8" : "&7";
                final String itemName = Utils.materialToEnglish(items.get(i));
                final String priceDisplay = SellPlugin.getMessageHandler().get("price-display-in-book"
                        , new Placeholder("%price%", Utils.formatMoney(shop.getPrice(items.get(i)))));

                text += colour + itemName + "\n";
                text += priceDisplay + "\n";
            }

            BaseComponent[] components = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', text)).create();
            meta.spigot().addPage(components);
        }

        meta.setAuthor("Yoreni");
        meta.setTitle("Shop Prices");
        book.setItemMeta(meta);
        return book;
    }
}
