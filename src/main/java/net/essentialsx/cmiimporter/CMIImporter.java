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

package net.essentialsx.cmiimporter;

import co.aikar.idb.DB;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.HikariPooledDatabase;
import co.aikar.idb.PooledDatabaseOptions;
import net.essentialsx.cmiimporter.config.DatabaseConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CMIImporter extends JavaPlugin implements Listener {

    private DatabaseConfig dbConfig;

    @Override
    public void onEnable() {
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
        if (event.getPlugin().getName().equals("EssentialsX")) {
            Migrations.migrateAll(this, event.getPlugin());
        }
    }

    DatabaseConfig getDbConfig() {
        return dbConfig;
    }
}
