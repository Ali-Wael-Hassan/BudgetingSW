package com.duck.model.type;

public class ReportConfig {
    private String category;
    private float percent;

    public ReportConfig(String category, float percent) {
        this.category = category;
        this.percent = percent;
    }

    public String getCategory() {
        return this.category;
    }

    public float getPercent() {
        return this.percent;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
}