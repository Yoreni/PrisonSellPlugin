package com.yoreni.sellplugin.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Menu implements Listener
{
    public static Map<UUID, Menu> openedMenus = new HashMap<UUID, Menu>();

    protected Inventory inv;

    public Menu(int rows, String title)
    {
        inv = Bukkit.createInventory(null, rows * 9, title);
    }

    public void open(Player player)
    {
        player.openInventory(inv);
        openedMenus.put(player.getUniqueId(), this);
    }

    abstract protected void onClick(InventoryClickEvent event);

    protected ItemStack makeFillerItem(Material item)
    {
        return new ItemBuilder(item).setName(" ").toItemStack();
    }
}
