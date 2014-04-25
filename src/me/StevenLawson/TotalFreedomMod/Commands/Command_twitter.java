package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.Config.TFM_ConfigEntry;
import me.StevenLawson.TotalFreedomMod.TFM_TwitterHandler;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Manage your twitter.", usage = "/<command> <set [twitter] | info | enable | disable>")
public class Command_twitter extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!TFM_ConfigEntry.TWITTERBOT_ENABLED.getBoolean())
        {
            TFM_Util.playerMsg(sender, "TwitterBot has been disabled in config.", ChatColor.RED);
            return true;
        }

        if (args.length < 1)
        {
            return false;
        }

        TFM_TwitterHandler twitterbot = TFM_TwitterHandler.getInstance();

        if ("set".equals(args[0]))
        {
            if (args.length != 2)
            {
                return false;
            }

            if (args[1].startsWith("@"))
            {
                TFM_Util.playerMsg(sender, "Please do not prefix your twitter username with '@'");
                return true;
            }

            String reply = twitterbot.setTwitter(sender.getName(), args[1]);

            if ("ok".equals(reply))
            {
                TFM_Util.playerMsg(sender, "Your twitter handle has been set to: " + ChatColor.AQUA + "@" + args[1] + ChatColor.GRAY + ".");
            }
            else if ("disabled".equals(reply))
            {
                TFM_Util.playerMsg(sender, "TwitterBot has been temporarily disabled,, please wait until it get re-enabled", ChatColor.RED);
            }
            else if ("failed".equals(reply))
            {
                TFM_Util.playerMsg(sender, "There was a problem querying the database, please let a developer know.", ChatColor.RED);
            }
            else if ("false".equals(reply))
            {
                TFM_Util.playerMsg(sender, "There was a problem with the database, please let a developer know.", ChatColor.RED);
            }
            else if ("cannotauth".equals(reply))
            {
                TFM_Util.playerMsg(sender, "The database password is incorrect, please let a developer know.", ChatColor.RED);
            }
            else
            {
                TFM_Util.playerMsg(sender, "An unknown error occurred, please contact a developer", ChatColor.RED);
                TFM_Util.playerMsg(sender, "Response code: " + reply);
            }
            return true;
        }

        if (args.length != 1)
        {
            return false;
        }

        if ("info".equals(args[0]))
        {
            String reply = twitterbot.getTwitter(sender.getName());
            TFM_Util.playerMsg(sender, "-- Twitter Information --", ChatColor.BLUE);
            TFM_Util.playerMsg(sender, "Using this feature, you can re-super yourself using twitter.");
            TFM_Util.playerMsg(sender, "You can set your twitter handle using " + ChatColor.AQUA + "/twitter set [twittername]");
            TFM_Util.playerMsg(sender, "Then, you can verify yourself by tweeting " + ChatColor.AQUA + "@TFUpdates #superme");
            if ("notfound".equals(reply))
            {
                TFM_Util.playerMsg(sender, "You currently have " + ChatColor.RED + "no" + ChatColor.BLUE + " Twitter handle set.", ChatColor.BLUE);
            }
            else if ("disabled".equals(reply))
            {
                TFM_Util.playerMsg(sender, "TwitterBot has been temporarily disabled, please wait until re-enabled", ChatColor.RED);
            }
            else if ("failed".equals(reply))
            {
                TFM_Util.playerMsg(sender, "There was a problem querying the database, please let a developer know.", ChatColor.RED);
            }
            else if ("false".equals(reply))
            {
                TFM_Util.playerMsg(sender, "There was a problem with the database, please let a developer know.", ChatColor.RED);
            }
            else if ("cannotauth".equals(reply))
            {
                TFM_Util.playerMsg(sender, "The database password is incorrect, please let a developer know.", ChatColor.RED);
            }
            else
            {
                TFM_Util.playerMsg(sender, "Your current twitter handle: " + ChatColor.AQUA + "@" + reply + ChatColor.BLUE + ".", ChatColor.BLUE);
            }
            return true;
        }

        if ("enable".equals(args[0]) || "disable".equals(args[0]))
        {
            if (!sender.getName().equalsIgnoreCase("DarthSalamon"))
            {
                sender.sendMessage(TotalFreedomMod.MSG_NO_PERMS);
                return true;
            }

            TFM_Util.adminAction(sender.getName(), ("enable".equals(args[0]) ? "Ena" : "Disa") + "bling Twitterbot", true);
            String reply = twitterbot.setEnabled(args[0] + "d");
            TFM_Util.playerMsg(sender, "Reply: " + reply);
            return true;
        }

        // Command not recognised
        return false;
    }
}
