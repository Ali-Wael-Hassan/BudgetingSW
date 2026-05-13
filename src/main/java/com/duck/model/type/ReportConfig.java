package com.duck.model.type;

/**
 * Holds configuration for a single report entry associating a category
 * name with a calculated percentage value.
 */
public class ReportConfig {
    private String category;
    private float percent;

    /** Constructs an empty ReportConfig. */
    public ReportConfig() {}

    /**
     * Constructs a ReportConfig with the given values.
     * @param category the category name
     * @param percent  the percentage value
     */
    public ReportConfig(String category, float percent) {
        // initialize values
        this.category = category;
        this.percent = percent;
    }

    /** @return the category name */
    public String getCategory() {
        return this.category;
    }

    /** @return the percentage value */
    public float getPercent() {
        return this.percent;
    }

    /** @param category the new category name */
    public void setCategory(String category) {
        // update category
        this.category = category;
    }

    /** @param percent the new percentage value */
    public void setPercent(float percent) {
        // update precentage
        this.percent = percent;
    }
}