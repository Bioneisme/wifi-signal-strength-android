package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.location.Location;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalGrid {
    GridInfo gridInfo;

    private Map<CellPosition, SignalInfo> cells = new HashMap<>();

    public SignalGrid(GridInfo gridInfo) {
        this.gridInfo = gridInfo;

        cells.put(null, new SignalInfo());
    }

    public void addMeasurement(Location location, ScanResult scanResult) {
        addSignalLevel(gridInfo.computeCellPosition(location), scanResult.level);
    }

    private void addSignalLevel(CellPosition cellPosition, double signalLevel) {
        if (cellPosition == null)
            return;

        if (!gridInfo.containsCellPosition(cellPosition))
            return;

        if (!cells.containsKey(cellPosition)) {
            cells.put(cellPosition, new SignalInfo());
        }

        cells.get(cellPosition).addSignalLevel(signalLevel);
    }

    public SignalInfo getSignalInfo(CellPosition cellPosition) {
        if (!gridInfo.containsCellPosition(cellPosition) ||
                !cells.containsKey(cellPosition))
            cellPosition = null;

        return cells.get(cellPosition);
    }

    public Map<CellPosition, SignalInfo> getCells() {
        return cells;
    }

    public GridInfo getGridInfo() {
        return gridInfo;
    }

    public class SignalInfo {
        private final static double DEFAULT_SIGNAL_LEVEL = -100.0;

        private List<Double> signalLevels = new ArrayList<>();
        private double averageSignalLevel = DEFAULT_SIGNAL_LEVEL;

        public SignalInfo() {
        }

        public void addSignalLevel(double signalLevel) {
            signalLevels.add(signalLevel);
            updateAverageSignalLevel();
        }

        public double getAverageSignalLevel() {
            return averageSignalLevel;
        }

        private void updateAverageSignalLevel() {
            if (signalLevels.isEmpty()) {
                averageSignalLevel = DEFAULT_SIGNAL_LEVEL;
                return;
            }

            double average;
            double sum = 0;

            for (Double signalLevel : signalLevels)
                sum += signalLevel;

            this.averageSignalLevel = sum / signalLevels.size();
        }
    }
}
