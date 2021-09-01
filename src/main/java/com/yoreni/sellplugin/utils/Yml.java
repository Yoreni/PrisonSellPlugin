package com.yoreni.sellplugin.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Yml
{
    FileConfiguration config = null;
    File file = null;
    Plugin plugin;

    public Yml(Plugin plugin, String path)
    {
        if (!plugin.getDataFolder().exists())
        {
            plugin.getDataFolder().mkdirs();
        }

        //cutting the .yml on the end if there is one
        path = fixFilePath(path);

        //sperating the name and the path
        String name = path;
        if(path.contains("/"))
        {
            name = path.split("/")[path.split("/").length - 1];
            path = "/" + path.substring(0,(path.length() - name.length()) - 1);
        }
        else
        {
            path = "";
        }

        // created all the sub directries if there are any
        file = new File(new File(plugin.getDataFolder() + path),name + ".yml");
        if(!path.equals(""))
        {
            File subDir = new File(plugin.getDataFolder(),path);
            subDir.mkdir();
        }

        //actaully creating the file
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
                InputStream stream =  getClass().getResourceAsStream("/" + name +".yml");
                //copyFile(stream,file);
            }
            catch (Exception Exception)
            {
                Exception.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        this.plugin = plugin;
    }

    public Yml(Plugin plugin, File file)
    {
        this.file = file;
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
                //InputStream stream =  getClass().getResourceAsStream("/" + name +".yml");
                //copyFile(stream,file);
            }
            catch (Exception Exception)
            {
                Exception.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        this.plugin = plugin;
    }

    public void setDefault(String key, Object value)
    {
        if(config.isSet(key))
        {
            config.set(key, value);
            save();
        }
    }

    public String getOrDefault(String key, Object defaultValue)
    {
        if(config.isSet(key))
        {
            return config.get(key).toString();
        }
        else
        {
            return defaultValue.toString();
        }
    }

    public String getOrSetDefault(String key, Object defaultValue)
    {
        if(config.isSet(key))
        {
            return config.get(key).toString();
        }
        else
        {
            config.set(key, defaultValue);
            return defaultValue.toString();
        }
    }

    public void set(String s,Object x)
    {
        config.set(s,x);
        save();
    }

    public void changeDouble(String s,double x)
    {
        config.set(s,config.getDouble(s) + x);
        save();
    }

    public void changeInt(String s,int x)
    {
        config.set(s,config.getInt(s) + x);
        save();
    }

    public void changeLong(String s,long x)
    {
        config.set(s,config.getLong(s) + x);
        save();
    }

    public double getDouble(String s)
    {
        return config.getDouble(s);
    }

    public int getInt(String s)
    {
        return config.getInt(s);
    }

    public long getLong(String s)
    {
        return config.getLong(s);
    }

    public String getString(String s)
    {
        return config.getString(s);
    }

    public boolean getBoolean(String s)
    {
        return config.getBoolean(s);
    }

    public List<String> getStringList(String s)
    {
        return config.getStringList(s);
    }

    public Object get(String s)
    {
        return config.get(s);
    }

    public boolean isSet(String s)
    {
        return config.isSet(s);
    }

    public @NotNull Map<String, Object> getValues(String path)
    {
        return config.getConfigurationSection(path).getValues(true);
    }

    public void reload()
    {
        try
        {
            config.load(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    public String getName()
    {
        return file.getName();
    }

    public void setDefaultsFromJar()
    {
        try
        {
            ConfigUpdater.update(plugin, file.getName(), file, Arrays.asList("config-version"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        reload();
    }

    private void save()
    {
        try
        {
            config.save(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * fixes a path if it has an extra .yml
     * eg hello.yml become hello
     * hello.txt.jdh.yml becomes hello.txt.jdh
     *
     * @param path
     * @return
     */
    private String fixFilePath(String path)
    {
        if(!path.contains("."))
        {
            return path;
        }

        String out = "";
        for(int i = 0; i < path.split("//.").length - 1; i++)
        {
            out += path.split("//.")[i];
        }

        return out;
    }
}

/**
 * A class to update/add new sections/keys to your config while keeping your current values and keeping your comments
 * Algorithm:
 * Read the new file and scan for comments and ignored sections, if ignored section is found it is treated as a comment.
 * Read and write each line of the new config, if the old config has value for the given key it writes that value in the new config.
 * If a key has an attached comment above it, it is written first.
 * @author tchristofferson
 */
class ConfigUpdater
{
    /**
     * Update a yaml file from a resource inside your plugin jar
     * @param plugin Your plugin
     * @param resourceName The yaml file name to update from, typically config.yml
     * @param toUpdate The yaml file to update
     * @param ignoredSections List of sections to ignore and copy from the current config
     * @throws IOException If an IOException occurs
     */
    public static void update(Plugin plugin, String resourceName, File toUpdate, List<String> ignoredSections) throws IOException
    {
            BufferedReader newReader = new BufferedReader(new InputStreamReader(plugin.getResource(resourceName), StandardCharsets.UTF_8));
            List<String> newLines = newReader.lines().collect(Collectors.toList());
            newReader.close();

            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(toUpdate);
            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(resourceName)));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(toUpdate), StandardCharsets.UTF_8));

            List<String> ignoredSectionsArrayList = new ArrayList<>(ignoredSections);
            //ignoredSections can ONLY contain configurations sections
            ignoredSectionsArrayList.removeIf(ignoredSection -> !newConfig.isConfigurationSection(ignoredSection));

            Yaml yaml = new Yaml();
            Map<String, String> comments = parseComments(newLines, ignoredSectionsArrayList, oldConfig, yaml);
            write(newConfig, oldConfig, comments, ignoredSectionsArrayList, writer, yaml);
    }

    //Write method doing the work.
    //It checks if key has a comment associated with it and writes comment then the key and value
    private static void write(FileConfiguration newConfig, FileConfiguration oldConfig, Map<String, String> comments, List<String> ignoredSections, BufferedWriter writer, Yaml yaml) throws IOException
    {
        outer: for (String key : newConfig.getKeys(true))
        {
            String[] keys = key.split("\\.");
            String actualKey = keys[keys.length - 1];
            String comment = comments.remove(key);

            StringBuilder prefixBuilder = new StringBuilder();
            int indents = keys.length - 1;
            appendPrefixSpaces(prefixBuilder, indents);
            String prefixSpaces = prefixBuilder.toString();

            if (comment != null) {
                writer.write(comment);//No \n character necessary, new line is automatically at end of comment
            }

            for (String ignoredSection : ignoredSections)
            {
                if (key.startsWith(ignoredSection))
                {
                    continue outer;
                }
            }

            Object newObj = newConfig.get(key);
            Object oldObj = oldConfig.get(key);

            if (newObj instanceof ConfigurationSection && oldObj instanceof ConfigurationSection)
            {
                //write the old section
                writeSection(writer, actualKey, prefixSpaces, (ConfigurationSection) oldObj);
            }
            else if (newObj instanceof ConfigurationSection)
            {
                //write the new section, old value is no more
                writeSection(writer, actualKey, prefixSpaces, (ConfigurationSection) newObj);
            }
            else if (oldObj != null)
            {
                //write the old object
                write(oldObj, actualKey, prefixSpaces, yaml, writer);
            }
            else
            {
                //write new object
                write(newObj, actualKey, prefixSpaces, yaml, writer);
            }
        }

        String danglingComments = comments.get(null);

        if (danglingComments != null)
        {
            writer.write(danglingComments);
        }

        writer.close();
    }

    //Doesn't work with configuration sections, must be an actual object
    //Auto checks if it is serializable and writes to file
    @SuppressWarnings("rawtypes")
    private static void write(Object obj, String actualKey, String prefixSpaces, Yaml yaml, BufferedWriter writer) throws IOException {
        if (obj instanceof ConfigurationSerializable)
        {
            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(((ConfigurationSerializable) obj).serialize()));
        }
        else if (obj instanceof String || obj instanceof Character)
        {
            if (obj instanceof String)
            {
                String s = (String) obj;
                obj = s.replace("\n", "\\n");
            }

            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(obj));
        }
        else if (obj instanceof List)
        {
            writeList((List) obj, actualKey, prefixSpaces, yaml, writer);
        }
        else
        {
            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(obj));
        }
    }

    //Writes a configuration section
    private static void writeSection(BufferedWriter writer, String actualKey, String prefixSpaces, ConfigurationSection section) throws IOException
    {
        if (section.getKeys(false).isEmpty())
        {
            writer.write(prefixSpaces + actualKey + ": {}");
        }
        else
        {
            writer.write(prefixSpaces + actualKey + ":");
        }

        writer.write("\n");
    }

    //Writes a list of any object
    private static void writeList(@SuppressWarnings("rawtypes") List list, String actualKey, String prefixSpaces, Yaml yaml, BufferedWriter writer) throws IOException
    {
        writer.write(getListAsString(list, actualKey, prefixSpaces, yaml));
    }

    private static String getListAsString(@SuppressWarnings("rawtypes") List list, String actualKey, String prefixSpaces, Yaml yaml)
    {
        StringBuilder builder = new StringBuilder(prefixSpaces).append(actualKey).append(":");

        if (list.isEmpty())
        {
            builder.append(" []\n");
            return builder.toString();
        }

        builder.append("\n");

        for (int i = 0; i < list.size(); i++)
        {
            Object o = list.get(i);

            if (o instanceof String || o instanceof Character)
            {
                builder.append(prefixSpaces).append("- '").append(o).append("'");
            }
            else if (o instanceof List)
            {
                builder.append(prefixSpaces).append("- ").append(yaml.dump(o));
            }
            else
            {
                builder.append(prefixSpaces).append("- ").append(o);
            }

            if (i != list.size())
            {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    //Key is the config key, value = comment and/or ignored sections
    //Parses comments, blank lines, and ignored sections
    private static Map<String, String> parseComments(List<String> lines, List<String> ignoredSections, FileConfiguration oldConfig, Yaml yaml)
    {
        Map<String, String> comments = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        int lastLineIndentCount = 0;

        outer: for (String line : lines)
        {
            if (line != null && line.trim().startsWith("-"))
                continue;

            if (line == null || line.trim().equals("") || line.trim().startsWith("#"))
            {
                builder.append(line).append("\n");
            }
            else
            {
                lastLineIndentCount = setFullKey(keyBuilder, line, lastLineIndentCount);

                for (String ignoredSection : ignoredSections)
                {
                    if (keyBuilder.toString().equals(ignoredSection))
                    {
                        Object value = oldConfig.get(keyBuilder.toString());

                        if (value instanceof ConfigurationSection)
                            appendSection(builder, (ConfigurationSection) value, new StringBuilder(getPrefixSpaces(lastLineIndentCount)), yaml);

                        continue outer;
                    }
                }

                if (keyBuilder.length() > 0)
                {
                    comments.put(keyBuilder.toString(), builder.toString());
                    builder.setLength(0);
                }
            }
        }

        if (builder.length() > 0)
        {
            comments.put(null, builder.toString());
        }

        return comments;
    }

    @SuppressWarnings("rawtypes")
    private static void appendSection(StringBuilder builder, ConfigurationSection section, StringBuilder prefixSpaces, Yaml yaml)
    {
        builder.append(prefixSpaces).append(getKeyFromFullKey(section.getCurrentPath())).append(":");
        Set<String> keys = section.getKeys(false);

        if (keys.isEmpty())
        {
            builder.append(" {}\n");
            return;
        }

        builder.append("\n");
        prefixSpaces.append("  ");

        for (String key : keys)
        {
            Object value = section.get(key);
            String actualKey = getKeyFromFullKey(key);

            if (value instanceof ConfigurationSection)
            {
                appendSection(builder, (ConfigurationSection) value, prefixSpaces, yaml);
                prefixSpaces.setLength(prefixSpaces.length() - 2);
            }
            else if (value instanceof List)
            {
                builder.append(getListAsString((List) value, actualKey, prefixSpaces.toString(), yaml));
            } else
            {
                builder.append(prefixSpaces.toString()).append(actualKey).append(": ").append(yaml.dump(value));
            }
        }
    }

    //Counts spaces in front of key and divides by 2 since 1 indent = 2 spaces
    private static int countIndents(String s)
    {
        int spaces = 0;

        for (char c : s.toCharArray())
        {
            if (c == ' ')
            {
                spaces += 1;
            }
            else
            {
                break;
            }
        }

        return spaces / 2;
    }

    //Ex. keyBuilder = key1.key2.key3 --> key1.key2
    private static void removeLastKey(StringBuilder keyBuilder)
    {
        String temp = keyBuilder.toString();
        String[] keys = temp.split("\\.");

        if (keys.length == 1)
        {
            keyBuilder.setLength(0);
            return;
        }

        temp = temp.substring(0, temp.length() - keys[keys.length - 1].length() - 1);
        keyBuilder.setLength(temp.length());
    }

    private static String getKeyFromFullKey(String fullKey)
    {
        String[] keys = fullKey.split("\\.");
        return keys[keys.length - 1];
    }

    //Updates the keyBuilder and returns configLines number of indents
    private static int setFullKey(StringBuilder keyBuilder, String configLine, int lastLineIndentCount)
    {
        int currentIndents = countIndents(configLine);
        String key = configLine.trim().split(":")[0];

        if (keyBuilder.length() == 0)
        {
            keyBuilder.append(key);
        }
        else if (currentIndents == lastLineIndentCount)
        {
            //Replace the last part of the key with current key
            removeLastKey(keyBuilder);

            if (keyBuilder.length() > 0)
            {
                keyBuilder.append(".");
            }

            keyBuilder.append(key);
        }
        else if (currentIndents > lastLineIndentCount)
        {
            //Append current key to the keyBuilder
            keyBuilder.append(".").append(key);
        }
        else
        {
            int difference = lastLineIndentCount - currentIndents;

            for (int i = 0; i < difference + 1; i++)
            {
                removeLastKey(keyBuilder);
            }

            if (keyBuilder.length() > 0)
            {
                keyBuilder.append(".");
            }

            keyBuilder.append(key);
        }

        return currentIndents;
    }

    private static String getPrefixSpaces(int indents)
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < indents; i++)
        {
            builder.append("  ");
        }

        return builder.toString();
    }

    private static void appendPrefixSpaces(StringBuilder builder, int indents)
    {
        builder.append(getPrefixSpaces(indents));
    }
}