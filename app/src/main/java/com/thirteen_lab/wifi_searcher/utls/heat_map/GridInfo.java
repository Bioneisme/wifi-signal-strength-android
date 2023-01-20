package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.location.Location;

public class GridInfo {
    private double cellWidth; // as from east to west (in meters)
    private double cellHeight; // as from north to south (in meters)

    private int columnsCount;
    private int rowsCount;

    // center point of the grid, mapping it to the position on the earth
    private Location centerLocation;

    public Location getCenterLocation() {
        return centerLocation;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public int getColumnsCount() {
        return columnsCount;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public double getWidth() {
        return cellWidth * (double) columnsCount;
    }

    public double getHeight() {
        return cellHeight * (double) rowsCount;
    }

    public boolean containsCellPosition(CellPosition cellPosition) {
        return cellPosition.getRow() >= 0 && cellPosition.getRow() < rowsCount &&
                cellPosition.getColumn() >= 0 && cellPosition.getColumn() < columnsCount;
    }

    public CellPosition computeCellPosition(Location location) {
        Location centerLocation = getCenterLocation();

        double yOffset = GeographicalCalculator.InMeters.getNorthwardsDisplacement(centerLocation, location);
        double xOffset = GeographicalCalculator.InMeters.getEastwardsDisplacement(centerLocation, location);

        yOffset += getHeight() / 2;
        xOffset += getWidth() / 2;

        int row = (int) (yOffset / getRowsCount());
        int column = (int) (xOffset / getColumnsCount());

        return new CellPosition(row, column);
    }

    GridInfo(Location centerLocation) {
        // as for now, let the grid's cells be 10m x 10m
        cellWidth = 10.0;
        cellHeight = 10.0;

        // as for now, let it consist of 10 x 10 pieces
        columnsCount = 10;
        rowsCount = 10;

        this.centerLocation = centerLocation;
    }
}
