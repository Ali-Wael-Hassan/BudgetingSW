package com.duck.model.accountOps;

import java.util.ArrayList;
import com.duck.model.type.ReportConfig;
import com.duck.model.type.TransactionConfig;

public interface ReportGenerator {
    ArrayList<ReportConfig> generate(TransactionConfig config);
}