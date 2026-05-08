package com.duck.model.accountOps;

import java.util.ArrayList;
import com.duck.model.type.ReportConfig;
import com.duck.model.type.TransactionConfig;

/**
 * Interface for generating financial reports from transaction data.
 */
public interface ReportGenerator {

    /**
     * Generates report data based on the given filter configuration.
     * @param config the filter criteria (type, category, period, etc.)
     * @return list of ReportConfig entries with category and percentage
     */
    ArrayList<ReportConfig> generate(TransactionConfig config);
}