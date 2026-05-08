package com.duck.model.accountOps;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import com.duck.model.type.AppSettings;
import com.duck.model.type.ReportConfig;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.Transaction;
import com.duck.model.dataAccessors.LocalStorage;

// PDF Classes
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Generates a PDF financial report.  Filters transactions by the
 * given TransactionConfig, calculates category percentages, and
 * writes the result to TransactionReport.pdf.
 */
public class PDFReport implements ReportGenerator {
    /**
     * Generates report data, exports to PDF, and returns the raw
     * ReportConfig list.
     * @param config the filter criteria for transactions
     * @return list of ReportConfig entries with category percentages
     */
    @Override
    public ArrayList<ReportConfig> generate(TransactionConfig config) {
        List<String> categories = LocalStorage.getInstance().getCategories();

        Map<String, Float> map = new HashMap<>();

        for (var item : categories) {
            map.put(item, 0.0f);
        }
        
        List<Transaction> incomeList = LocalStorage.getInstance().getIncome();
        List<Transaction> expenseList = LocalStorage.getInstance().getExpenses();

        // 1. Check if the config allows INCOME transactions
        if (config.getType() == null || config.getType() == AppSettings.TransactionType.INCOME) {
            for (var income : incomeList) {
                if (passesFilters(income, config)) {
                    float currentAmount = map.getOrDefault(income.getCategory(), 0.0f);
                    map.put(income.getCategory(), currentAmount + income.getAmount());
                }
            }
        }

        // 2. Check if the config allows EXPENSE transactions
        if (config.getType() == null || config.getType() == AppSettings.TransactionType.EXPENSE) {
            for (var expense : expenseList) {
                if (passesFilters(expense, config)) {
                    float currentAmount = map.getOrDefault(expense.getCategory(), 0.0f);
                    map.put(expense.getCategory(), currentAmount - expense.getAmount());
                }
            }
        }

        float totalAmount = 0;
        for (float amount : map.values()) {
            totalAmount += Math.abs(amount);
        }

        ArrayList<ReportConfig> reportData = new ArrayList<>();

        if (totalAmount > 0) {
            for (Map.Entry<String, Float> entry : map.entrySet()) {
                String category = entry.getKey();
                float amount = entry.getValue();

                float percent = (Math.abs(amount) / totalAmount) * 100f;
                reportData.add(new ReportConfig(category, percent));
            }
        } else {
            for (String category : categories) {
                reportData.add(new ReportConfig(category, 0f));
            }
        }

        exportToPdfFile(reportData);

        return reportData;
    }

    /**
     * Checks whether a transaction passes all active filters in the
     * config (category, date period, amount range, and account).
     * @param transaction the transaction to test
     * @param config      the filter criteria
     * @return true if the transaction passes all filters
     */
    private boolean passesFilters(Transaction transaction, TransactionConfig config) {
            
        // 1. Filter by Category
        if (config.getCategory() != null && !config.getCategory().isEmpty()) {
            if (!config.getCategory().contains(transaction.getCategory())) {
                return false;
            }
        }

        // 2. Filter by Period
        if (config.getPeriod() != null) {
            LocalDate txDate = transaction.getDate(); 
            LocalDate startDate = config.getPeriod().getStartDate();
            LocalDate endDate = config.getPeriod().getEndDate();

            if (txDate.isBefore(startDate) || txDate.isAfter(endDate)) {
                return false; 
            }
        }

        // 3. Filter by Range (Amount)
        if (config.getRange() != null) {
            if (!config.getRange().contains(transaction.getAmount())) {
                return false;
            }
        }

        // 4. Filter by Account
        if (config.getAccount() != null) {
            if (!config.getAccount().equals(transaction.getAccount())) {
                return false;
            }
        }

        return true; // Passed all active filters
    }

    /**
     * Writes the report data to a PDF file at
     * Documents/Reports/TransactionReport.pdf.
     * @param data the category-percentage pairs to render
     */
    private void exportToPdfFile(ArrayList<ReportConfig> data) {
        // Create PFD Document
        try (PDDocument document = new PDDocument()) {
            // Add a blank page to the document
            PDPage page = new PDPage();
            document.addPage(page);

            // Prepare to write text onto the page
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Write the Title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Financial Category Report");
                contentStream.endText();

                // Write Data Rows
                int yPosition = 670;
                for (ReportConfig item : data) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(50, yPosition);

                    // Format the text (Category: Percent%")
                    String lineText = String.format("- %s: %.2f%%", item.getCategory(), item.getPercent());

                    contentStream.showText(lineText);
                    contentStream.endText();

                    yPosition -= 20;
                }
            }

            String userHome = System.getProperty("user.home");

            File reportDir = new File(userHome, "Documents" + File.separator + "Reports");
            
            if (!reportDir.exists()) {
                boolean created = reportDir.mkdirs(); 
                if (created) {
                    System.out.println("Created new directory at: " + reportDir.getAbsolutePath());
                } else {
                    System.err.println("Failed to create directory. Saving might fail.");
                }
            }

            File pdfFile = new File(reportDir, "TransactionReport.pdf");

            document.save(pdfFile);
            System.out.println("PDF Successfully Created at: " + pdfFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating the PDF file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}