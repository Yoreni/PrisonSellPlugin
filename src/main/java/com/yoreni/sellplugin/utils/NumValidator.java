package com.yoreni.sellplugin.utils;

public class NumValidator<T extends Number>
{
    //TODO make it work for int, float, short, long, byte

    private T max;
    private T min;

    public NumValidator()
    {

    }

    public NumValidator setMax(T number)
    {
        max = number;
        return this;
    }

    public NumValidator setMin(T number)
    {
        min = number;
        return this;
    }

    public boolean validate(String number)
    {
        try
        {
            if (max instanceof Double)
            {
                double max = this.max == null ? (Double) getDefaultMax() : (Double) this.max;
                double min = this.min == null ? (Double) getDefaultMax() : (Double) this.min;
                double value = Double.parseDouble(number);

                return value > min && value < max;
            }
        }
        catch(NumberFormatException exception)
        {
            return false;
        }
        return false;
    }

    private Object getDefaultMax()
    {
        if(max instanceof Double)
        {
            return Double.MAX_VALUE;
        }
        return null;
    }

    private Object getDefaultMin()
    {
        if(max instanceof Double)
        {
            return Double.MIN_VALUE;
        }
        return null;
    }
}
