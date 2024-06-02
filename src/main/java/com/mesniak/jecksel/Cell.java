package com.mesniak.jecksel;


import java.util.LinkedList;
import java.util.List;

public class Cell {
    private int x;
    private int y;
    private Value value;
    private List<Cell> dependencies = new LinkedList<>();

    public Cell(int x, int y, Value value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }

    public String getLocation() {
        return "" + (char)(getX() + 'a') + (char)(getY() + 1 + '0');
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public List<Cell> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Cell> dependencies) {
        this.dependencies = dependencies;
    }
}
