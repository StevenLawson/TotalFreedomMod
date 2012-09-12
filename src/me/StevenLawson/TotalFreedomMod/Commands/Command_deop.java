package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command_deop extends TFM_Command
{
	@Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
		if(!(TFM_Util.isUserSuperadmin(sender) || senderIsConsole))
		{
			sender.sendMessage(TotalFreedomMod.MSG_NO_PERMS);
			return true;
		}
		
		if(args.length != 1)
		{
			return false;
		}
		
		OfflinePlayer p = null;
    	for(Player onlinePlayer : Bukkit.getOnlinePlayers())
    	{
    		if(args[0].equalsIgnoreCase(onlinePlayer.getName()))
    		{
    			p = onlinePlayer;
    		}
    	}
    	
    	// if the player is not online
    	if(p == null)
    	{
    		p = Bukkit.getOfflinePlayer(args[0]);
    	}
		
		TFM_Util.adminAction(sender.getName(), "De-opping " + p.getName(), false);
		p.setOp(false);
		return true;
    }
}
