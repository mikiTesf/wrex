package com.domain;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DBConnection {

    static JdbcConnectionSource connectionSource = null;

    static  {
        // `wdb` - A fancy acronym for "WREX Database"
        String PATH_TO_DB = "jdbc:sqlite:wrex.wdb";

        try {
            connectionSource = new JdbcConnectionSource(PATH_TO_DB);
            TableUtils.createTableIfNotExists(connectionSource, Presenter.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
