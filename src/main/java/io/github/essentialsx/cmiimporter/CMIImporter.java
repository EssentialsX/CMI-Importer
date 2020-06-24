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

package io.github.essentialsx.cmiimporter;

import co.aikar.idb.DB;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.HikariPooledDatabase;
import co.aikar.idb.PooledDatabaseOptions;
import io.github.essentialsx.cmiimporter.config.DatabaseConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class CMIImporter extends JavaPlugin implements Listener {

    private DatabaseConfig dbConfig;
    private File migrationFile;

    private FileConfiguration migrationConfig;

    @Override
    public void onEnable() {
        loadMigrationFile();
        getServer().getPluginManager().registerEvents(this, this);

        dbConfig = new DatabaseConfig(this);
        DatabaseOptions options = dbConfig.getDbOptions();
        HikariPooledDatabase db = PooledDatabaseOptions.builder()
                .options(options)
                .createHikariDatabase();

        DB.setGlobalDatabase(db);
    }

    @Override
    public void onDisable() {
        DB.close();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("Essentials")) {
            Migrations.migrateAll(this, event.getPlugin());
        }
    }

    private void loadMigrationFile() {
        this.migrationFile = new File(getDataFolder(), "migrations.yml");
        if (!this.migrationFile.exists()) {
            saveResource("migrations.yml", false);
        }
        migrationConfig = YamlConfiguration.loadConfiguration(this.migrationFile);
    }

    public void saveMigrationFile() {
        try {
            migrationConfig.save(migrationFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save migrations to " + migrationFile, ex);
        }
    }

    DatabaseConfig getDbConfig() {
        return dbConfig;
    }

    public FileConfiguration getMigrationConfig() {
        return migrationConfig;
    }
}
