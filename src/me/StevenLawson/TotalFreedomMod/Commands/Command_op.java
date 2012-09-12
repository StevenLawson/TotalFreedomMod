package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command_op extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
    	if(!sender.isOp())
    	{
    		sender.sendMessage(TotalFreedomMod.MSG_NO_PERMS);
    		return true;
    	}
    	
    	if(args.length != 1)
    	{
    		return false;
    	}
    	
    	if(args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("everyone"))
    	{
    		TFM_Util.playerMsg(sender, "Correct usage: /opall");
    		return true;
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
    		if(TFM_Util.isUserSuperadmin(sender) || senderIsConsole)
    		{
    			p = Bukkit.getOfflinePlayer(args[0]);
    		}
    		else
    		{
    			TFM_Util.playerMsg(sender, "That player is not online.");
    			TFM_Util.playerMsg(sender, "You don't have permissions to OP offline players.", ChatColor.YELLOW);
    			return true;
    			
    		}
    	}
    	
    	TFM_Util.adminAction(sender.getName(), "Opping " + p.getName(), false);
    	p.setOp(true);
    	
    	
    	
    	return true;
    }
}
