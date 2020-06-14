package com.domain;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "presenter")
public class Presenter {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "first_name", defaultValue = "", canBeNull = false)
    private String firstName;

    @DatabaseField(columnName = "middle_name", defaultValue = "")
    private String middleName;

    @DatabaseField(columnName = "last_name", defaultValue = "", canBeNull = false)
    private String lastName;

    @DatabaseField(columnName = "privilege", canBeNull = false)
    private Privilege privilege;

    public static Dao<Presenter, Integer> presenterDao;

    static {
        try {
            presenterDao = DaoManager.createDao(DBConnection.connectionSource, Presenter.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Presenter() {}

    public Presenter(String firstName, String middleName, String lastName, Privilege privilege) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.privilege = privilege;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.middleName + " " + this.lastName;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }

    public static void save(Presenter presenter) throws SQLException {
        presenterDao.createIfNotExists(presenter);
    }

    public static List<Presenter> getAllPresenters() {
        try {
            return presenterDao.queryForAll();
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }
}
