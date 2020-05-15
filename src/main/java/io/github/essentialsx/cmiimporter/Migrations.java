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
import co.aikar.idb.DbRow;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.OfflinePlayer;
import com.earth2me.essentials.User;
import net.ess3.api.MaxMoneyException;
import net.ess3.nms.refl.ReflUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Migrations {

    private static final Method SET_OFFLINE_PLAYER_NAME = ReflUtil.getMethodCached(OfflinePlayer.class, "setName", String.class);

    private static String prefix;

    static void migrateAll(CMIImporter importerPlugin, Plugin essPlugin) {
        if (!(essPlugin instanceof Essentials)) {
            throw new IllegalArgumentException("The currently installed \"Essentials\" plugin isn't actually EssentialsX!");
        }

        Essentials ess = (Essentials) essPlugin;
        prefix = importerPlugin.getDbConfig().getTablePrefix();
        migrateUsers(ess);
        migrateHomes(ess);
        migrateNicknames(ess);
        migrateWarps(ess);
        migrateEconomy(ess);
    }

    static void migrateUsers(Essentials ess) {
        try {
            SET_OFFLINE_PLAYER_NAME.setAccessible(true);
            List<DbRow> results = DB.getResults("SELECT player_uuid, username, FakeAccount FROM " + table("users") + " WHERE player_uuid NOT NULL AND username NOT NULL");
            for (DbRow row : results) {
                UUID uuid = UUID.fromString(row.getString("player_uuid"));
                String username = row.getString("username");
                boolean isNpc = row.getInt("FakeAccount") != 0;

                if (!ess.getUserMap().userExists(uuid)) {
                    OfflinePlayer player = new OfflinePlayer(uuid, Bukkit.getServer());
                    SET_OFFLINE_PLAYER_NAME.invoke(player, username);

                    User user = new User(player, ess);
                    user.setLastAccountName(username);
                    if (isNpc) {
                        user.setNPC(true);
                    }
                    user.save();
                }
            }
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    static void migrateHomes(Essentials ess) {
        final String homeLocSeparator = ":";
        try {
            List<DbRow> results = DB.getResults("SELECT player_uuid, Homes FROM " + table("users") + " WHERE player_uuid NOT NULL AND Homes NOT NULL");
            for (DbRow row : results) {
                UUID uuid = UUID.fromString(row.getString("player_uuid"));
                User user = ess.getUser(uuid);

                for (Map.Entry<String, String> entry : Util.parseMap(row.getString("Homes")).entrySet()) {
                    String name = entry.getKey();
                    String loc = entry.getValue();
                    try {
                        user.setHome(name, Util.parseLocation(loc, homeLocSeparator, true));
                    } catch (Exception ex) {
                        System.out.println("Couldn't set home: " + name + " for " + user.getLastAccountName());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void migrateNicknames(Essentials ess) {
        try {
            List<DbRow> results = DB.getResults("SELECT player_uuid, nickname FROM " + table("users") + " WHERE player_uuid NOT NULL AND nickname NOT NULL");
            for (DbRow row : results) {
                UUID uuid = UUID.fromString(row.getString("player_uuid"));
                User user = ess.getUser(uuid);
                String nickname = row.getString("nickname");
                if (nickname != null) {
                    user.setNickname(nickname);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    static void migrateWarps(Essentials ess) {
        final String warpLocSeparator = ";";
        try {
            File warpsFile = new File(ess.getDataFolder(), "../CMI/warps.yml");
            YamlConfiguration warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
            for (String key : warpsConfig.getKeys(false)) {
                String locString = warpsConfig.getString(key + ".Location");
                if (locString != null) {
                    Location loc = Util.parseLocation(locString, warpLocSeparator, false);
                    try {
                        ess.getWarps().setWarp(null, key, loc);
                    } catch (Exception ex) {
                        System.out.println("Couldn't migrate warp: " + key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void migrateEconomy(Essentials ess) {
        try {
            List<DbRow> results = DB.getResults("SELECT player_uuid, Balance FROM " + table("users") + " WHERE player_uuid NOT NULL AND Balance NOT NULL");
            for (DbRow row : results) {
                UUID uuid = UUID.fromString(row.getString("player_uuid"));
                User user = ess.getUser(uuid);
                // Not using valueOf() because it returns scientific notation and Glare isn't advanced enough to find a way to fix this
                BigDecimal bal = new BigDecimal(row.getDbl("Balance", 0.0));
                user.setMoney(bal);
            }
        } catch (SQLException | MaxMoneyException ex) {
            ex.printStackTrace();
        }
    }

    private static String table(String table) {
        return prefix + table;
    }

}
