using System;
using System.Numerics;

public static class StringTools
{
    public static string CompleteCero(string pString, short pMaxChar, bool pRight = false)
    {
        var vNewString = pString;

        if (pString.Length < pMaxChar)
        {
            for (int i = pString.Length; i < pMaxChar; i++)
            {
                if (pRight)
                    vNewString = string.Concat(vNewString, "0");
                else
                    vNewString = string.Concat("0", vNewString);
            }
        }
        return vNewString;
    }

    public static string Base16(string pString)
    {
        var vValor = BigInteger.Parse(pString);
        return vValor.ToString("X");
    }

    public static string Base10(string pString)
    {
        var vValor = BigInteger.Parse(pString, System.Globalization.NumberStyles.HexNumber);
        return vValor.ToString();
    }
}
