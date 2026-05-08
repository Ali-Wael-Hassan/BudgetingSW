package com.duck;

import com.duck.model.type.AppSettings;

/**
 * Utility class with static methods for currency formatting, symbol
 * lookup, and conversion between supported currencies (USD, EUR, EGP).
 */
public class CurrencyUtil {

    // =========================================================================
    // Conversion Rates
    // =========================================================================

    private static final double USD_TO_EUR = 1.0 / 1.18;
    private static final double USD_TO_EGP = 1.0 / 0.019;

    // =========================================================================
    // Symbol Lookup
    // =========================================================================

    /**
     * Returns the currency symbol for the given currency.
     * @param currency the currency enum, or null
     * @return the symbol string (defaults to $)
     */
    public static String getSymbol(AppSettings.Currency currency) {
        if (currency == null) return "$";
        switch (currency) {
            case USD: return "$";
            case EUR: return "\u20AC";
            case EGP: return "EGP ";
            default: return "$";
        }
    }

    // =========================================================================
    // Conversion
    // =========================================================================

    /**
     * Converts an amount from USD to the target currency.
     * @param amountUsd the amount in USD
     * @param target    the target currency
     * @return the converted amount
     */
    public static float convertFromUsd(float amountUsd, AppSettings.Currency target) {
        if (target == null || target == AppSettings.Currency.USD) return amountUsd;
        switch (target) {
            case EUR: return (float) (amountUsd * USD_TO_EUR);
            case EGP: return (float) (amountUsd * USD_TO_EGP);
            default: return amountUsd;
        }
    }

    /**
     * Converts an amount from a source currency to USD.
     * @param amountLocal the amount in the source currency
     * @param source      the source currency
     * @return the converted amount in USD
     */
    public static float convertToUsd(float amountLocal, AppSettings.Currency source) {
        if (source == null || source == AppSettings.Currency.USD) return amountLocal;
        switch (source) {
            case EUR: return (float) (amountLocal / USD_TO_EUR);
            case EGP: return (float) (amountLocal / USD_TO_EGP);
            default: return amountLocal;
        }
    }

    // =========================================================================
    // Formatting
    // =========================================================================

    /**
     * Formats an amount with the currency symbol and two decimal places.
     * @param amountUsd the amount in USD
     * @param currency  the target display currency
     * @return a formatted string like "$1,234.56"
     */
    public static String format(float amountUsd, AppSettings.Currency currency) {
        float converted = convertFromUsd(amountUsd, currency);
        return getSymbol(currency) + String.format("%,.2f", converted);
    }

    /**
     * Formats an amount with the currency symbol and no decimal places.
     * @param amountUsd the amount in USD
     * @param currency  the target display currency
     * @return a formatted string like "$1,235"
     */
    public static String formatInt(float amountUsd, AppSettings.Currency currency) {
        float converted = convertFromUsd(amountUsd, currency);
        return getSymbol(currency) + String.format("%,.0f", converted);
    }
}
