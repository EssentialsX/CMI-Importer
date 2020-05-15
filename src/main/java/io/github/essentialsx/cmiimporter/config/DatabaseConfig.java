/*
 * Imports data from a CMI SQLite database into EssentialsX.
 * Copyright (C) 2020 md678685
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.essentialsx.cmiimporter.config;

import co.aikar.idb.DatabaseOptions;
import io.github.essentialsx.cmiimporter.CMIImporter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DatabaseConfig {

    private final CMIImporter plugin;
    private final String hostname;
    private final String username;
    private final String password;
    private final String database;
    private final String backend;
    private final String prefix;

    public DatabaseConfig(CMIImporter plugin) {
        this.plugin = plugin;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "../CMI/dataBaseInfo.yml"));
        hostname = config.getString("mysql.hostname", "localhost:3306");
        username = config.getString("mysql.username", "root");
        password = config.getString("mysql.password", "");
        database = config.getString("mysql.database", "minecraft");
        backend = config.getString("storage.method");
        prefix = config.getString("mysql.tablePrefix", "CMI_");
    }

    public DatabaseOptions getDbOptions() {
        DatabaseOptions.DatabaseOptionsBuilder builder = DatabaseOptions.builder()
                .poolName(plugin.getDescription().getName() + " DB")
                .logger(plugin.getLogger());

        if (backend != null && backend.equalsIgnoreCase("mysql")) {
            return getMySQLOptions(builder);
        } else {
            return getSQLiteOptions(builder);
        }
    }

    private DatabaseOptions getSQLiteOptions(DatabaseOptions.DatabaseOptionsBuilder builder) {
        File sqliteFile = new File(plugin.getDataFolder(), "../CMI/cmi.sqlite.db");
        return builder.sqlite(sqliteFile.getPath())
                .dataSourceClassName("org.sqlite.SQLiteDataSource")
                .driverClassName("org.sqlite.JDBC")
                .build();
    }

    private DatabaseOptions getMySQLOptions(DatabaseOptions.DatabaseOptionsBuilder builder) {
        return builder.mysql(username, password, database, hostname).build();
    }

    public String getTablePrefix() {
        if (backend.equalsIgnoreCase("mysql")) {
            return prefix;
        }

        return "";
    }

}
