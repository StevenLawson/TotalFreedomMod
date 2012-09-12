package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command_qdeop extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length != 1)
        {
            return false;
        }

        if (senderIsConsole || TFM_Util.isUserSuperadmin(sender))
        {
            boolean matched_player = false;
            for (Player p : server.matchPlayer(args[0]))
            {
                matched_player = true;

                TFM_Util.adminAction(sender.getName(), "De-opping " + p.getName(), false);
                p.setOp(false);
                p.sendMessage(TotalFreedomMod.YOU_ARE_NOT_OP);
            }
            if (!matched_player)
            {
            	for(Player p : server.getOnlinePlayers())
            	{
            		if(args[0].toLowerCase().startsWith(p.getDisplayName().toLowerCase()))
            		TFM_Util.adminAction(sender.getName(), "De-opping " + p.getName(), false);
                    p.setOp(false);
                    p.sendMessage(TotalFreedomMod.YOU_ARE_NOT_OP);
                    return true;
            	}
                sender.sendMessage("No targets matched.");
            }
        }
        else
        {
            sender.sendMessage(TotalFreedomMod.MSG_NO_PERMS);
        }

        return true;
    }
}
