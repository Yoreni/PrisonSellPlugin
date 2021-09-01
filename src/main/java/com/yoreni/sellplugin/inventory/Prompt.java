package com.yoreni.sellplugin.inventory;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Prompt
{
    public static Map<UUID, Prompt> prompts = new HashMap<UUID, Prompt>();

    enum Type
    {
        CHANGE_PRICE,
        CREATE_SHOP
    }

    private Type type;
    private Object[] args;

    public Prompt(Player player, Type type, Object... otherValues)
    {
        this.type = type;
        this.args = otherValues;
        prompts.put(player.getUniqueId(), this);
    }

    public static Map<UUID, Prompt> getPrompts()
    {
        return prompts;
    }

    public Type getType()
    {
        return type;
    }

    public Object[] getArgs()
    {
        return args;
    }
}
