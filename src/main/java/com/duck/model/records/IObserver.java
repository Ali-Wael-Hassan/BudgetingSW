package com.duck.model.records;

import com.duck.model.type.Transaction;

public interface IObserver {
    void update(Transaction transaction);
}
