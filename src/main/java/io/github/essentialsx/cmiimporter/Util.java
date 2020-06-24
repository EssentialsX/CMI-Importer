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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    public static Map<String, String> parseMap(String input) {
        HashMap<String, String> result = new HashMap<>();

        for (String entry : input.split(";")) {
            String[] split = entry.split("%%");
            result.put(split[0], split[1]);
        }

        return result;
    }

    public static List<List<String>> parseLists(String input, String listSeparator, String elementSeparator) {
        List<List<String>> parent = new ArrayList<>();
        for (String list : input.split(listSeparator)) {
            parent.add(Arrays.asList(list.split(elementSeparator)));
        }
        return parent;
    }

    public static Location parseLocation(String input, String separator, boolean reverseYawPitch) {
        String[] split = input.split(separator);
        World world = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        float yaw = Float.parseFloat((reverseYawPitch ? split[5] : split[4]));
        float pitch = Float.parseFloat((reverseYawPitch ? split[4] : split[5]));
        return new Location(world, x, y, z, yaw, pitch);
    }

}
