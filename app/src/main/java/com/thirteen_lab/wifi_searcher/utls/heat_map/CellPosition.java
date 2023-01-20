package com.thirteen_lab.wifi_searcher.utls.heat_map;

public class CellPosition {
    private int row;
    private int column;

    public CellPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (getClass() != o.getClass())
            return false;

        CellPosition other = (CellPosition) o;

        return getColumn() == other.getColumn() && getRow() == other.getRow();
    }
}
