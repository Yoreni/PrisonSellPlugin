package com.yoreni.sellplugin.utils;

public class Placeholder
{
    private String placeholder;
    private String value;

    public Placeholder(String placeholder, String value)
    {
        this.placeholder = placeholder;
        this.value = value;
    }

    public String apply(String text)
    {
        return text.replace(placeholder, value);
    }
}
