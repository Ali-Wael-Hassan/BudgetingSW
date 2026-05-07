package com.duck;

import com.duck.model.type.AppSettings;

public class CurrencyUtil {

    private static final double USD_TO_EUR = 1.0 / 1.18;
    private static final double USD_TO_EGP = 1.0 / 0.019;

    public static String getSymbol(AppSettings.Currency currency) {
        if (currency == null) return "$";
        switch (currency) {
            case USD: return "$";
            case EUR: return "\u20AC";
            case EGP: return "\u062C.\u0645";
            default: return "$";
        }
    }

    public static float convertFromUsd(float amountUsd, AppSettings.Currency target) {
        if (target == null || target == AppSettings.Currency.USD) return amountUsd;
        switch (target) {
            case EUR: return (float) (amountUsd * USD_TO_EUR);
            case EGP: return (float) (amountUsd * USD_TO_EGP);
            default: return amountUsd;
        }
    }

    public static float convertToUsd(float amountLocal, AppSettings.Currency source) {
        if (source == null || source == AppSettings.Currency.USD) return amountLocal;
        switch (source) {
            case EUR: return (float) (amountLocal / USD_TO_EUR);
            case EGP: return (float) (amountLocal / USD_TO_EGP);
            default: return amountLocal;
        }
    }

    public static String format(float amountUsd, AppSettings.Currency currency) {
        float converted = convertFromUsd(amountUsd, currency);
        return getSymbol(currency) + String.format("%,.2f", converted);
    }

    public static String formatInt(float amountUsd, AppSettings.Currency currency) {
        float converted = convertFromUsd(amountUsd, currency);
        return getSymbol(currency) + String.format("%,.0f", converted);
    }
}
