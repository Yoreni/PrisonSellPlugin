package com.yoreni.sellplugin.utils;

import com.yoreni.sellplugin.SellPlugin;
import net.md_5.bungee.chat.TranslationRegistry;
import org.bukkit.Material;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils
{
    //private static final String[] NUMBER_SUFFIXES = {"","k"," mil"," bil"," tril"," quad"," quint"," sext"," sept"," oct"," non"," dec"," und"," dud"," tred"};
    //private static final String[] NUMBER_SUFFIXES = {"","K","M","B","T","Q"};
    private static final DecimalFormat FORMAT = new DecimalFormat("#,###.###############");
    private static List<String> listOfItemNames = new ArrayList<String>();

    /**
     * converts a material to a human readable (english) name
     *
     * @param material the material you want to convert
     * @return the human frendly name
     */
    public static String materialToEnglish(Material material)
    {
        String name = TranslationRegistry.INSTANCE.translate(material.getTranslationKey());
        return name;
    }

    /**
     * rounds a number with a rounding rule telling how it should be one
     *
     *  Syntax of Rounding Rule
     *  DP means decimal places and SF means sifnificant figures
     *  eg.
     *  2 DP means round the number to 2 decimal places
     *  5 SF means round the number t0 5 significant figures
     *
     * @param number
     * @param roundingRule
     * @return the rounded number
     */
    public static String round(double number, String roundingRule)
    {
        //if they provided an invalid rule we will default to 2 decimal places
        if(!roundingRule.split(" ")[1].equals("DP") && !roundingRule.split(" ")[1].equals("SF"))
        {
            roundingRule = "2 DP";
        }

        //gets how to round the number
        double factor = 0;
        if(roundingRule.split(" ")[1].equals("DP"))
        {
            factor = Math.pow(10,Double.parseDouble(roundingRule.split(" ")[0]));
        }
        if(roundingRule.split(" ")[1].equals("SF"))
        {
            factor = Math.floor(Math.log10(number)) - (Double.parseDouble(roundingRule.split(" ")[0]) - 1);
            factor = Math.pow(10,factor * -1);
        }

        //rounds the number
        number = Math.round(number * factor);
        number = number / factor;

        //removes the reduntent ".0" if there is one
        String output = toCommas(number);

        return output;
    }

    /**
     * formats a number into scientific notation
     *
     * @param number
     * @return a String in scientifific notation
     */
    public static String toScientificNotation(double number)
    {
        if(number < 0)
        {
            number = Math.abs(number);
            return "-" + toScientificNotation(number);
        }
        else if (number == 0)
        {
            return "0";
        }
        else
        {
            int exponent = (int) Math.floor(Math.log10(number));
            double mentessia = number / Math.pow(10,exponent);
            return round(mentessia, SellPlugin.config().getString("number.rounding")) + "e" + exponent;
        }
    }

    /**
     * Takes a big number and only shows the most important digits
     * eg. 32,432,672 -> 23.4M
     *
     * @param number
     * @return
     */
    public static String abbreviateNumber(double number)
    {
        final List<String> NUMBER_SUFFIXES = SellPlugin.getMessageHandler().getAsList("number.large-number-suffixes");

        int exponent = (int) Math.log10(number);
        int powerOf1000 = exponent / 3;

        if(number < 0)
        {
            number = Math.abs(number);
            return "-" + abbreviateNumber(number);
        }
        else if(number < SellPlugin.config().getLong("number.abbreviate-from"))
        {
            //we cast it to an int cos people will only want the intager portion cos so there wont be any
            //redundent .0's
            return String.valueOf((int) number);
        }
        else if (powerOf1000 < NUMBER_SUFFIXES.size())
        {
            number = number / Math.pow(10, powerOf1000 * 3);
            String roundedNumber = round(number, SellPlugin.config().getString("number.rounding"));
            return roundedNumber + NUMBER_SUFFIXES.get(powerOf1000);
        }
        //if the number is so big that we dont have a suffix for it then we will display it in scienfic notation
        else
        {
            return toScientificNotation(number);
        }
    }

    /**
     * sperates a number with commas to make it more readible eg. 23432345 = 23,432,345
     *
     * @param number
     * @return a String
     */
    public static String toCommas(double number)
    {
        return toCommas(number, "#,###.###############");
    }

    public static String toCommas(double number, String format)
    {
        String commaNumber = new DecimalFormat(format).format(number);

        /*
            other languages use different symbols for decimal points and thousand separators
            so tis accounts for this
         */
        final String thousandsSeperator = SellPlugin.getMessageHandler().get("number.thousand-separator");
        final String decimalPoint = SellPlugin.getMessageHandler().get("number.decimal-point");

        commaNumber = commaNumber.replace(",", "a").replace(".", "b");
        commaNumber = commaNumber.replace("a", thousandsSeperator).replace("b", decimalPoint);

        return commaNumber;
    }

    public static String formatMoney(double money)
    {
        return formatMoney(money, SellPlugin.config().getBoolean("number.show-cents"));
    }

    /**
     * displays money in a more human readable way
     *
     * @param money the number you want to format
     * @param showCents wether you want to show cents at the end of the number
     * @return
     */
    public static String formatMoney(double money, boolean showCents)
    {
        if(money < SellPlugin.config().getInt("number.abbreviate-from"))
        {
            //round it to 2 decimal places
            money = Math.floor(money * 100);
            money /= 100;

            if(money % 1 == 0 || !showCents)
            {
                return toCommas(money, "#,###");
            }
            else
            {
                return toCommas(money, "#,##0.00");
            }
        }
        else
        {
            return abbreviateNumber(money);
        }
    }

    /**
     * gets a list of all items and blocks in the game
     *
     * @return a list of all items and blocks in the game
     */
    public static List<String> getListOfItems()
    {
        if(listOfItemNames.size() == 0)
        {
            initListOfItems();
        }

        return listOfItemNames;
    }

    private static void initListOfItems()
    {
        if(listOfItemNames.size() == 0)
        {
            Material[] items = Arrays.stream(Material.values())
                    .toArray(Material[]::new);

            //turning all the item materials into strings
            String[] itemNames = Arrays.stream(items)
                    .map((item) -> item.getKey().asString().split(":")[1])
                    .toArray(String[]::new);

            listOfItemNames = Arrays.asList(itemNames);
        }
    }

    public static boolean isValidDouble(String string)
    {
        try
        {
            double number = Double.parseDouble(string);
            return true;
        }
        catch(NumberFormatException exception)
        {
            return false;
        }
    }
}
