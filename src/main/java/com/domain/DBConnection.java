package com.domain;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DBConnection {

    static JdbcConnectionSource connectionSource = null;

    public static void initializeDBTables() {
        try {
            String PATH_TO_DB = "jdbc:sqlite:wrex.wdb";
            connectionSource = new JdbcConnectionSource(PATH_TO_DB);
            TableUtils.createTableIfNotExists(connectionSource, Presenter.class);
            TableUtils.createTableIfNotExists(connectionSource, Settings.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
