package com.yoreni.sellplugin.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler
{
    private Yml messages;

    public MessageHandler(Plugin plugin)
    {
        messages = new Yml(plugin, "messages");
        messages.setDefaultsFromJar();
    }

    /**
     * gets a string from the speficated path in the messages.yml
     * it also applys colour and placeholders
     *
     * if the path doesnt exist it will return that path
     *
     * @param key
     * @return
     */
    public String get(String key, Placeholder... placeholders)
    {
        messages.reload();
        if(messages.isSet(key))
        {
            String message = messages.getString(key);

            for(Placeholder placeholder : placeholders)
            {
                message = placeholder.apply(message);
            }
            message = colourise(message);

            return message;
        }
        else
        {
            return key;
        }
    }

    /**
     * gets a list from the speficated path in the messages.yml
     * it also applys colour and placeholders
     *
     * if the path doesnt exist it will return that path
     * if the path speficised contains a String instead of a list it will return a list with that string in it.
     *
     * @param key
     * @return
     */
    public List<String> getAsList(String key, Placeholder... placeholders)
    {
        messages.reload();
        if(messages.isSet(key))
        {
            List<String> text = messages.getStringList(key);
            List<String> out = new ArrayList<String>();

            if(text.size() == 0)
            {
                out.add(get(key, placeholders));
            }
            else
            {
                for(int i = 0; i < text.size(); i++)
                {
                    String line = text.get(i);

                    for(Placeholder placeholder : placeholders)
                    {
                        line = placeholder.apply(line);
                    }
                    line = colourise(line);

                    out.add(line);
                }
            }

            return out;
        }
        else
        {
            List<String> text = new ArrayList<String>();
            text.add(key);
            return text;
        }
    }

    public void sendMessage(CommandSender player, String key, Placeholder... placeholders)
    {
        List<String> text = getAsList(key, placeholders);

        for(String line : text)
        {
            player.sendMessage(line);
        }
    }

    public void sendActionbar(Player player, String key, Placeholder... placeholders)
    {
        String text = get(key, placeholders);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }

    /**
     * Adds colour to a string.
     * (It shows up in game)
     *
     * @param string
     * @return
     */
    private String colourise(String string)
    {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}

