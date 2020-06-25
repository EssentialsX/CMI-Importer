package io.github.essentialsx.cmiimporter.commands;

import com.earth2me.essentials.Essentials;
import io.github.essentialsx.cmiimporter.CMIImporter;
import io.github.essentialsx.cmiimporter.Migrations;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MigrationCommand implements CommandExecutor {
    private CMIImporter cmiImporter;
    private Essentials essentials;

    public MigrationCommand(CMIImporter cmiImporter) {
        this.cmiImporter = cmiImporter;
        essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("essentials.migration")) {
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            Migrations.migrateAll(cmiImporter, essentials);
        }
        return false;
    }
}
