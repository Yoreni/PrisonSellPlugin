package com.yoreni.sellplugin.inventory;

import com.yoreni.sellplugin.SellPlugin;
import com.yoreni.sellplugin.Shop;
import com.yoreni.sellplugin.utils.Placeholder;
import com.yoreni.sellplugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShopInfo extends Menu
{
    private Shop shop;

    public ShopInfo(Shop shop)
    {
        super(6, shop.getName());
        this.shop = shop;

        //init the inventory
        updateInventory();
    }

    @Override
    protected void onClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        final boolean clickedInTheirInv = event.getRawSlot() >= event.getInventory().getSize();

        //if the player clicked on the items shown in the shop
        if(event.getSlot() >= 18 && !clickedInTheirInv)
        {
            if(event.getSlot() - 18 < shop.getBuying().size())
            {
                Material item = shop.getBuying().get(event.getSlot() - 18);
                if(event.isLeftClick())
                {
                    player.closeInventory();
                    //TODO message
                    SellPlugin.getMessageHandler().sendMessage(player,"prompt-price-change",
                            new Placeholder("%item%", Utils.materialToEnglish(item)));
                    SellPlugin.getMessageHandler().sendMessage(player,"prompt-cancel");
                    new Prompt(player, Prompt.Type.CHANGE_PRICE, shop, item);
                }
                else if(event.isRightClick() && event.isShiftClick())
                {
                    shop.removeItem(item);
                    updateInventory();
                }
            }
        }
        else if(clickedInTheirInv)
        {
            @Nullable ItemStack item = event.getCurrentItem();
            if(item != null)
            {
                if (!shop.buysItem(item.getType()))
                {
                    shop.addItem(item.getType(), 10);
                    updateInventory();
                }
            }
        }
        event.setCancelled(true);
    }

    private void updateInventory()
    {
        inv.setItem(0, makeInfoButton());
        for(int i = 9; i < 18; i++)
        {
            inv.setItem(i, makeFillerItem(Material.GRAY_STAINED_GLASS_PANE));
        }
        for(int i = 18; i < 54; i++)
        {
            if(i - 18 < shop.getBuying().size())
            {
                Material item = shop.getBuying().get(i - 18);
                double price = shop.getPrice(item);
                inv.setItem(i, makeItemDisplay(item, price));
            }
            else
            {
                inv.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    private ItemStack makeInfoButton()
    {
        final String name = SellPlugin.getMessageHandler().get("shop-info-gui.info-item-name");
        final List<String> lore = SellPlugin.getMessageHandler().getAsList("shop-info-gui.info-item-lore");

        return new ItemBuilder(Material.OAK_SIGN).setName(name).setLore(lore).toItemStack();
    }

    private ItemStack makeItemDisplay(Material item, double price)
    {
        final List<String> lore = SellPlugin.getMessageHandler().getAsList("shop-info-gui.buys-item-lore",
                new Placeholder("%pricestack%", Utils.formatMoney(price, true)),
                new Placeholder("%priceone%", Utils.formatMoney(price / item.getMaxStackSize(), true)));

        return new ItemBuilder(item).setLore(lore).toItemStack();
    }
}
