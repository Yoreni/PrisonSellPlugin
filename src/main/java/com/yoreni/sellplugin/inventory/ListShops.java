package com.yoreni.sellplugin.inventory;

import com.yoreni.sellplugin.SellPlugin;
import com.yoreni.sellplugin.Shop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ListShops extends Menu
{
    public ListShops()
    {
        super(6, "All shops");

        for(int slot = 0 ; slot < 54; slot++)
        {
            if(slot < Shop.getShops().size())
            {
                inv.setItem(slot, getShopItem(Shop.getShops().get(slot)));
            }
            else if(slot == Shop.getShops().size())
            {
                inv.setItem(slot, getAddShopItem());
            }
            else
            {
                inv.setItem(slot, new ItemStack(Material.AIR));
            }
        }
    }

    @Override
    protected void onClick(InventoryClickEvent event)
    {
        event.setCancelled(true);
        final boolean clickedInTheirInv = event.getRawSlot() >= event.getInventory().getSize();
        Player player = (Player) event.getWhoClicked();

        if(event.getSlot() < Shop.getShops().size() && !clickedInTheirInv)
        {
            Shop shop = Shop.getShops().get(event.getSlot());
            new ShopInfo(shop).open(player);
        }
        else if(event.getSlot() == Shop.getShops().size() && !clickedInTheirInv)
        {
            player.closeInventory();
            SellPlugin.getMessageHandler().sendMessage(player,"prompt-enter-shop-name");
            SellPlugin.getMessageHandler().sendMessage(player,"prompt-cancel");
            new Prompt(player, Prompt.Type.CREATE_SHOP);
        }
    }

    private ItemStack getShopItem(Shop shop)
    {
        final List<String> lore = SellPlugin.getMessageHandler().getAsList("shop-list-gui.shop-display-lore");

        return new ItemBuilder(Material.CHEST).setName("&r" + shop.getName()).setLore(lore).toItemStack();
    }

    private ItemStack getAddShopItem()
    {
        final String name = SellPlugin.getMessageHandler().get("shop-list-gui.create-shop-name");
        final List<String> lore = SellPlugin.getMessageHandler().getAsList("shop-list-gui.create-shop-lore");

        return new ItemBuilder(Material.GOLD_NUGGET).setName(name).setLore(lore).toItemStack();
    }
}
