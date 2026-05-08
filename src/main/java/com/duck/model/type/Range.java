package com.duck.model.type;

/**
 * Represents a numeric range between a minimum and maximum float value.
 * Ensures that minValue is always less than or equal to maxValue.
 */
public class Range {
    private float minValue; 
    private float maxValue;

    /** Constructs an empty Range (0.0 - 0.0). */
    public Range() {}

    /**
     * Constructs a Range with the given values, swapping if necessary
     * to ensure minValue <= maxValue.
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public Range(float minValue, float maxValue) {
        if (minValue <= maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        } else {
            this.minValue = maxValue;
            this.maxValue = minValue;
        }
    }

    /** @return the minimum value */
    public float getMinValue() {
        return this.minValue;
    }

    /** @return the maximum value */
    public float getMaxValue() {
        return this.maxValue;
    }

    /**
     * Sets the minimum value only if it does not exceed the current maximum.
     * @param minValue the new minimum value
     */
    public void setMinValue(float minValue) {
        if (minValue <= this.maxValue) {
            this.minValue = minValue;
        }
    }

    /**
     * Sets the maximum value only if it is not below the current minimum.
     * @param maxValue the new maximum value
     */
    public void setMaxValue(float maxValue) {
        if (maxValue >= this.minValue) {
            this.maxValue = maxValue;
        }
    }

    /**
     * Checks whether a value falls within this range.
     * @param value the value to check
     * @return true if value is between min and max inclusive
     */
    public boolean contains(float value) {
        return value >= minValue && value <= maxValue;
    }
}