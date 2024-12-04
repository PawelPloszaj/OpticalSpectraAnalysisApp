package com.pbs.edu.opticalspectraanalysis;

public class Data {
    private int index;
    private double x;
    private double y;

    public Data(int index, double x, double y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "Data [x=" + x + ", y=" + y + "]";
    }
}
