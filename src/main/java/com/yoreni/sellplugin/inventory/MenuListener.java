package com.yoreni.sellplugin.inventory;

import com.yoreni.sellplugin.SellPlugin;
import com.yoreni.sellplugin.Shop;
import com.yoreni.sellplugin.utils.NumValidator;
import com.yoreni.sellplugin.utils.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener
{
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if(Menu.openedMenus.containsKey(player.getUniqueId()))
        {
            Menu.openedMenus.put(player.getUniqueId(), null);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(Menu.openedMenus.containsKey(player.getUniqueId()))
        {
            Menu.openedMenus.put(player.getUniqueId(), null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        Menu inv = Menu.openedMenus.get(player.getUniqueId());
        if(inv != null)
        {
            inv.onClick(event);
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event)
    {
        Player player = event.getPlayer();
        if(!Prompt.prompts.containsKey(player.getUniqueId()))
        {
            return;
        }

        Prompt prompt = Prompt.getPrompts().get(player.getUniqueId());
        if(prompt == null)
        {
            return;
        }

        event.setCancelled(true);
        if(event.getMessage().equalsIgnoreCase("cancel"))
        {
            Prompt.getPrompts().put(player.getUniqueId(), null);
            player.sendMessage("Input cancelled.");
            return;
        }

        switch(prompt.getType())
        {
            case CHANGE_PRICE:
                //this is risky there are no checks
                Shop shop = (Shop) prompt.getArgs()[0];
                Material item = (Material) prompt.getArgs()[1];

                if(!new NumValidator<Double>().setMin(0D).setMax(1e300).validate(event.getMessage()))
                {
                    SellPlugin.getMessageHandler().sendMessage(player,"invalid-input");
                    return;
                }
                double price = Double.parseDouble(event.getMessage());

                shop.addItem(item, price);
                new ShopInfo(shop).open(player);
                Prompt.getPrompts().put(player.getUniqueId(), null);
                break;
            case CREATE_SHOP:
                if(Shop.get(event.getMessage()) != null)
                {
                    SellPlugin.getMessageHandler().sendMessage(player,"shop-already-exists",
                            new Placeholder("%shop%", event.getMessage()));
                    return;
                }

                Shop createdShop = new Shop(event.getMessage());
                new ShopInfo(createdShop).open(player);
                Prompt.getPrompts().put(player.getUniqueId(), null);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + prompt.getType());
        }
    }
}
