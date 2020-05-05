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
import com.earth2me.essentials.Essentials;
import com.google.common.collect.ImmutableMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public final class CMIImporter extends JavaPlugin implements Listener {

    private static final Map<String, Consumer<Essentials>> MIGRATIONS = ImmutableMap.<String, Consumer<Essentials>>builder()
            .put("users", Migrations::migrateUsers)
            .put("homes", Migrations::migrateHomes)
            .put("nicknames", Migrations::migrateNicknames)
            .put("warps", Migrations::migrateWarps)
            .build();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        Path cmiDatabaseLocation = getDataFolder().toPath().resolve("../CMI/cmi.sqlite.db");

        DatabaseOptions options = DatabaseOptions.builder()
                .poolName(getDescription().getName() + " DB")
                .logger(getLogger())
                .dataSourceClassName("org.sqlite.SQLiteDataSource")
                .driverClassName("org.sqlite.JDBC")
                .dsn(cmiDatabaseLocation.toString())
                .build();
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
            runMigrations((Essentials) event.getPlugin());
        }
    }

    private void runMigrations(Essentials ess) {
        MIGRATIONS.forEach((name, migration) -> {
            getLogger().info("Running migration for " + name);
            migration.accept(ess);
        });
    }

}
