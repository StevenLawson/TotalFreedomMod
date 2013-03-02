package me.StevenLawson.TotalFreedomMod.Commands;

import java.io.File;
import me.StevenLawson.TotalFreedomMod.Commands.CommandPermissions.ADMIN_LEVEL;
import me.StevenLawson.TotalFreedomMod.Commands.CommandPermissions.SOURCE_TYPE_ALLOWED;
import me.StevenLawson.TotalFreedomMod.TFM_Log;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = ADMIN_LEVEL.SENIOR, source = SOURCE_TYPE_ALLOWED.ONLY_CONSOLE, block_host_console = true, ignore_permissions = false)
public class Command_listsync extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        TFM_Util.adminAction(sender.getName(), "Downloading superadmin and permban lists from http://www.thecjgcjg.com.", false);

        try
        {
            TFM_Util.downloadFile("http://www.thecjgcjg.com/cjfreedom/scripts/backup/superadmin.yml", new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.SUPERADMIN_FILE));
            TotalFreedomMod.loadSuperadminConfig();
            TFM_Util.adminAction(sender.getName(), TotalFreedomMod.SUPERADMIN_FILE + " downloaded.", false);
        }
        catch (Exception ex)
        {
            TFM_Log.severe(ex);
        }

        try
        {
            TFM_Util.downloadFile("http://www.thecjgcjg.com/cjfreedom/scripts/backup/permban.yml", new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.PERMBAN_FILE));
            TotalFreedomMod.loadPermbanConfig();
            TFM_Util.adminAction(sender.getName(), TotalFreedomMod.PERMBAN_FILE + " downloaded.", false);
        }
        catch (Exception ex)
        {
            TFM_Log.severe(ex);
        }

        return true;
    }
}
