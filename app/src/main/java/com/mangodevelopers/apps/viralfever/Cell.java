package com.mangodevelopers.apps.viralfever;

import android.util.Log;

/**
 * Created by Aman Tugnawat on 3/12/2018.
 */

public class Cell {

    private static final String TAG = "CeLL";
    private int cellOwner;
    private int numberOfViruses;
    private int cellType;

    public Cell(int cellOwner, int numberOfViruses, int cellType) {
        this.cellOwner = cellOwner;
        this.numberOfViruses = numberOfViruses;
        this.cellType = cellType;
    }
    public Cell() {
        this.cellOwner = -1;
        this.numberOfViruses = 0;
        this.cellType = -1;
    }

    public int getNumberOfViruses() {
        return numberOfViruses;
    }

    public void setNumberOfViruses(int numberOfViruses) {
        this.numberOfViruses = numberOfViruses;
    }

    public int getCellType() {
        return cellType;
    }

    public void setCellType(int cellType) {
        this.cellType = cellType;
    }

    public void increaseVirus(){
        if(numberOfViruses<3)
            numberOfViruses++;
        else
            numberOfViruses=0;
        Log.d(TAG, "cellowner:"+cellOwner+" cellnumviruses:"+numberOfViruses);
    }

    public int getCellOwner() {
        return cellOwner;
    }

    public void setCellOwner(int cellOwner) {
        this.cellOwner = cellOwner;
    }
}
