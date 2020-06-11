package com.domain;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;

@DatabaseTable(tableName = "settings")
public class Settings {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(defaultValue = "16")
    private int sheetTitleFontSize;

    @DatabaseField(defaultValue = "15")
    private int labelsFontSize;

    @DatabaseField(defaultValue = "16")
    private int meetingSectionTitleFontSize;

    @DatabaseField(defaultValue = "16")
    private int partFontSize;

    @DatabaseField(defaultValue = "16")
    private int presenterNameFontSize;

    @DatabaseField(defaultValue = "600")
    private int rowHeight;

    @DatabaseField(defaultValue = "false")
    private boolean addHallDividers;

    @DatabaseField(defaultValue = "false")
    private boolean askToAssignPresenters;

    public static Dao<Settings, Integer> settingsDao;

    static {
        try {
            settingsDao = DaoManager.createDao(DBConnection.connectionSource, Settings.class);
            if (settingsDao.queryForId(1) == null) {
                settingsDao.create(getDefaultSettings());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Settings() {}

    public Settings(
            int sheetTitleFontSize,
            int labelsFontSize,
            int meetingSectionTitleFontSize,
            int partFontSize,
            int presenterNameFontSize,
            int rowHeight,
            boolean addHallDividers,
            boolean askToAssignPresenters)
    {
        this.sheetTitleFontSize = sheetTitleFontSize;
        this.labelsFontSize = labelsFontSize;
        this.meetingSectionTitleFontSize = meetingSectionTitleFontSize;
        this.partFontSize = partFontSize;
        this.presenterNameFontSize = presenterNameFontSize;
        this.rowHeight = rowHeight;
        this.addHallDividers = addHallDividers;
        this.askToAssignPresenters = askToAssignPresenters;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSheetTitleFontSize() {
        return sheetTitleFontSize;
    }

    public void setSheetTitleFontSize(int sheetTitleFontSize) {
        this.sheetTitleFontSize = sheetTitleFontSize;
    }

    public int getLabelsFontSize() {
        return labelsFontSize;
    }

    public void setLabelsFontSize(int labelsFontSize) {
        this.labelsFontSize = labelsFontSize;
    }

    public int getMeetingSectionTitleFontSize() {
        return meetingSectionTitleFontSize;
    }

    public void setMeetingSectionTitleFontSize(int meetingSectionTitleFontSize) {
        this.meetingSectionTitleFontSize = meetingSectionTitleFontSize;
    }

    public int getPartFontSize() {
        return partFontSize;
    }

    public void setPartFontSize(int partFontSize) {
        this.partFontSize = partFontSize;
    }

    public int getPresenterNameFontSize() {
        return presenterNameFontSize;
    }

    public void setPresenterNameFontSize(int presenterNameFontSize) {
        this.presenterNameFontSize = presenterNameFontSize;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    public boolean hasHallDividers() {
        return addHallDividers;
    }

    public void setHasHallDividers(boolean addHallDividers) {
        this.addHallDividers = addHallDividers;
    }

    public boolean askToAssignPresenters() {
        return askToAssignPresenters;
    }

    public void setAskToAssignPresenters(boolean askToAssignPresenters) {
        this.askToAssignPresenters = askToAssignPresenters;
    }

    public static Settings getDefaultSettings() {
        return new Settings(16, 15, 16, 16, 16, 600, false, false);
    }

    public static Settings getLastSavedSettings() {
        try {
            return settingsDao.queryForId(1);
        } catch (SQLException e) {
            return getDefaultSettings();
        }
    }
}
