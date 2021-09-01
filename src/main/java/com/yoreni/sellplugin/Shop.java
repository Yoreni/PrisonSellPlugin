package com.yoreni.sellplugin;

import com.yoreni.sellplugin.utils.Placeholder;
import com.yoreni.sellplugin.utils.Utils;
import com.yoreni.sellplugin.utils.Yml;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class Shop
{
    /**
     * a list of all the shops
     */
    private static List<Shop> shops = new ArrayList<Shop>();

    /**
     * a HashMap containing how much the shop will buy each item
     */
    private Map<Material, Double> prices;

    /**
     * The name of the shop
     */
    private String name;

    /**
     * the shops parent
     * (if its not null if this shop doesnt buy any requested items its parent will try to buy it)
     */
    private Shop parent;

    public Shop(String name)
    {
        this.prices = new HashMap<Material, Double>();
        this.name = name;

        shops.add(this);
    }

    /**
     * gets a shop from the Shops list based on its name
     *
     * @param name the name of the shop
     * @return the shop instance (null if the shop doesnt exist)
     */
    public static Shop get(String name)
    {
        Shop shop = null;
        for(Shop s : shops)
        {
            if(s.getName().equals(name))
            {
                shop = s;
            }
        }

        return shop;
    }

    /**
     * @return the whole shop list
     */
    public static List<Shop> getShops()
    {
        return shops;
    }

    /**
     *  loads all shops from config files
     */
    public static void initShops()
    {
        List<Shop> shops = new ArrayList<Shop>();

        //make the shops directoary if it doesnt exist
        String shopsFilePath = "plugins/" + SellPlugin.getInstance().getName() + "/shops";
        File shopsDir = new File(shopsFilePath);
        shopsDir.mkdir();

        for (File file : shopsDir.listFiles())
        {
            Yml yaml = new Yml(SellPlugin.getInstance(), file);
            Shop shop = Shop.readFromYml(yaml);
            shops.add(shop);
        }
        Shop.shops = shops;

        //we have at init the inhertience part after the other things
        for(Shop shop : shops)
        {
            Yml yaml = new Yml(SellPlugin.getInstance(), "shops/" + shop.getName());
            String parentName = yaml.getOrDefault("inherits", "");
            if(!parentName.equals(""))
            {
                Shop parent = Shop.get(parentName);
                if(parent != null)
                {
                    try
                    {
                        shop.setParent(parent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * saves all shops from config files
     */
    public static void saveShops()
    {
        if(shops.size() == 0)
        {
            return;
        }

        for(Shop shop : shops)
        {
            shop.writeToYml();
        }
    }

    /**
     * a check if this shop buys an item
     *
     * @param item
     * @return a boolean if its true then it buys the item
     */
    public boolean buysItem(Material item)
    {
        return prices.containsKey(item);
    }

    /**
     * removes an item from the hash map that the shop buys
     * @param item
     */
    public void removeItem(Material item)
    {
        prices.remove(item);
    }

    public void removeEveryItem()
    {
        prices = new HashMap<Material, Double>();
    }

    public String getName()
    {
        return name;
    }

    /**
     * makes it so the shop also buys the item
     * @param item the item that the shop will buy
     * @param price the price the shop will buy it for
     */
    public void addItem(Material item, double price)
    {
        prices.put(item, price);
    }

    /**
     * sells all the items in the players inventory that is sellable
     * @param player
     */
    public void sellAll(Player player)
    {
        int itemsSold = 0;
        double moneyMade = 0;

        for(int i = 0; i <= 35; i++)
        {
            ItemStack itemInSlot = player.getInventory().getItem(i);
            if(itemInSlot != null && getPrice(itemInSlot.getType()) > 0)
            {
                itemsSold += itemInSlot.getAmount();
                moneyMade += getPrice(itemInSlot.getType()) * ((double) itemInSlot.getAmount() / itemInSlot.getType().getMaxStackSize());
                player.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
        }

        SellPlugin.getInstance().getEconomy().depositPlayer(player, moneyMade);
        if(itemsSold == 0)
        {
            SellPlugin.getMessageHandler().sendActionbar(player, "sell-message-fail");
        }
        else
        {
            SellPlugin.getMessageHandler().sendActionbar(player, "sell-message-success",
                    new Placeholder("%items%", Utils.toCommas(itemsSold)),
                    new Placeholder("%money%", Utils.formatMoney(moneyMade)));

            final String soundName = SellPlugin.config().getString("sound-on-sell");
            if(!soundName.equalsIgnoreCase("NONE"))
            {
                player.playSound(player.getEyeLocation(), Sound.valueOf(soundName), 1, 1);
            }
        }
    }

    /**
     * sells all of a particlur item to a shop
     *
     * @param player
     * @param item
     */
    public void sell(Player player, Material item)
    {
        sell(player, item, 2304);
    }

    /**
     * gets the price of an item. if this shop doesnt buy it then it returns the price listed in its parent if one is set
     * @param item
     * @return
     */
    public double getPrice(Material item)
    {
        if(prices.containsKey(item))
        {
            return prices.get(item);
        }
        else
        {
            Shop lookingAt = this;
            while(lookingAt.getParent() != null)
            {
                lookingAt = lookingAt.getParent();
                if(lookingAt.prices.containsKey(item))
                {
                    return lookingAt.prices.get(item);
                }
            }
            return 0D;
        }
    }

    /**
     * @return a set that contaions what this shop wants to buy
     */
    public List<Material> getBuying()
    {
        List<Material> buyingList = new ArrayList<Material>();
        prices.keySet().forEach((item) -> buyingList.add(item));
        return buyingList;
    }

    /**
     * sells an amount of a particlur item to a shop
     *
     * @param player
     * @param item
     * @param amount
     */
    public void sell(Player player, Material item, int amount)
    {
        int itemsSold = 0;
        int moneyMade = 0;

        for(int i = 0; i <= 35; i++)
        {
            ItemStack itemInSlot = player.getInventory().getItem(i);
            if(itemInSlot == null)
            {
                continue;
            }
            else if(itemInSlot.getType() == item && getPrice(item) > 0)
            {
                int itemsSoldInThisSlot = Math.min(amount - itemsSold, itemInSlot.getAmount());
                itemsSold += itemsSoldInThisSlot;
                moneyMade += getPrice(item) * ((double) itemsSoldInThisSlot / item.getMaxStackSize());
                itemInSlot.setAmount(itemInSlot.getAmount() - itemsSoldInThisSlot);
            }
        }

        SellPlugin.getInstance().getEconomy().depositPlayer(player, moneyMade);
        if(itemsSold == 0)
        {
            SellPlugin.getMessageHandler().sendActionbar(player, "sell-message-fail");
        }
        else
        {
            SellPlugin.getMessageHandler().sendActionbar(player, "sell-message-success",
                    new Placeholder("%items%", Utils.toCommas(itemsSold)),
                    new Placeholder("%money%", Utils.formatMoney(moneyMade)));

            final String soundName = SellPlugin.config().getString("sound-on-sell");
            if(!soundName.equalsIgnoreCase("NONE"))
            {
                player.playSound(player.getEyeLocation(), Sound.valueOf(soundName), 1, 1);
            }
        }
    }

    public Shop getParent()
    {
        return parent;
    }

    /**
     * sets the parent of a shop
     *
     * @param parent the parent you want to set it to
     * @throws Exception if its the same shop or a shop that will cause an infinite loop
     */
    public void setParent(Shop parent) throws Exception
    {
        if(parent == null)
        {
            this.parent = parent;
        }

        if(getName().equals(parent.getName()))
        {
            throw new Exception("A shop can not inherit its self");
        }

        Shop lookingAt = parent;
        while(lookingAt.getParent() != null)
        {
            if(lookingAt.getParent().getName().equals(getName()))
            {
                throw new Exception("This shop can not inherit a shop which ultimately inherits this one");
            }

            lookingAt = lookingAt.getParent();
        }

        this.parent = parent;
    }

    /**
     * loads a shop from a yml file
     *
     * @param file the file to read it from
     * @return retuns the shop it made
     */
    private static Shop readFromYml(Yml file)
    {
        Shop shop = new Shop(file.getString("name"));

        //setting the shops prices
        if(file.isSet("prices"))
        {
            Map<String, Object> prices = file.getValues("prices");
            for (String itemName : prices.keySet())
            {
                Material item = Material.getMaterial(itemName);
                if (item != null)
                {
                    double price = (Double) prices.get(itemName);
                    if (price > 0)
                    {
                        shop.addItem(item, price);
                    }
                }
            }
        }

        return shop;
    }

    /**
     *  saves shop to a yml file
     */
    private void writeToYml()
    {
        Yml file = new Yml(SellPlugin.getInstance(), "shops/" + getName());

        file.set("name", getName());
        file.set("inherits", parent != null ? parent.getName() : "");

        //this removes the blocks in the mine stated in the config file that isnt in the prices list
        //this is to stop the shop messing up when an item has been removed
        if(file.isSet("prices"))
        {
            Map<String, Object> existingPrices = file.getValues("prices");
            for(String key : existingPrices.keySet())
            {
                Material item = Material.getMaterial(key);
                if (item != null)
                {
                    if(!buysItem(item))
                    {
                        file.set("prices." + item.toString(), 0D);
                    }
                }
            }
        }

        file.setDefault("prices", "");
        for(Material item : prices.keySet())
        {
            file.set("prices." + item.toString(), getPrice(item));
        }
    }


























}
