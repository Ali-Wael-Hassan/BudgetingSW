package com.duck.model.type;

import java.util.List;
import com.duck.model.type.AppSettings.TransactionType;

/**
 * Configuration object used to define the parameters for querying or
 * creating a Transaction.  Includes transaction type, date Period,
 * category list, amount Range, and the owning Account.
 */
public class TransactionConfig {
    private AppSettings.TransactionType type;
    private Period period;
    private List<String> category;
    private Range range;
    private Account account;

    /** Constructs an empty TransactionConfig. */
    public TransactionConfig() {}

    /**
     * Constructs a TransactionConfig with the given values.
     * @param type     the transaction type (INCOME or EXPENSE)
     * @param period   the date period filter
     * @param category the list of categories
     * @param range    the amount range filter
     * @param account  the owning account
     */
    public TransactionConfig(AppSettings.TransactionType type, Period period, List<String> category, Range range, Account account) {
        // 1. set type
        this.type = type;
        // 2. set time
        this.period = period;
        // 3. set filters
        this.category = category;
        this.range = range;
        // 4. bind account
        this.account = account;
    }

    /** @return the owning account */
    public Account getAccount() {
        return this.account;
    }

    /** @return the transaction type */
    public TransactionType getType() {
        return this.type;
    }

    /** @return the date period */
    public Period getPeriod() {
        return this.period;
    }

    /** @return the category list */
    public List<String> getCategory() {
        return this.category;
    }

    /** @return the amount range */
    public Range getRange() {
        return this.range;
    }

    /** @param type the new transaction type */
    public void setType(AppSettings.TransactionType type) {
        this.type = type;
    }

    /** @param period the new date period */
    public void setPeriod(Period period) {
        this.period = period;
    }

    /** @param category the new category list */
    public void setCategory(List<String> category) {
        this.category = category;
    }

    /** @param range the new amount range */
    public void setRange(Range range) {
        this.range = range;
    }

    /** @param account the new owning account */
    public void setAccount(Account account) {
        this.account = account;
    }
}