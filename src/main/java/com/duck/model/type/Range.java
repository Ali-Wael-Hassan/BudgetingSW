package com.duck.model.type;

public class Range {
    private float minValue; 
    private float maxValue;

    public Range() {}

    public Range(float minValue, float maxValue) {
        if (minValue <= maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        } else {
            this.minValue = maxValue;
            this.maxValue = minValue;
        }
    }

    public float getMinValue() {
        return this.minValue;
    }

    public float getMaxValue() {
        return this.maxValue;
    }

    public void setMinValue(float minValue) {
        if (minValue <= this.maxValue) {
            this.minValue = minValue;
        }
    }

    public void setMaxValue(float maxValue) {
        if (maxValue >= this.minValue) {
            this.maxValue = maxValue;
        }
    }

    public boolean contains(float value) {
        return value >= minValue && value <= maxValue;
    }
}