package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command_gtfo extends TFM_Command
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
            Player p;
            try
            {
                p = getPlayer(args[0]);
            }
            catch (CantFindPlayerException ex)
            {
                sender.sendMessage(ex.getMessage());
                return true;
            }

            TFM_Util.bcastMsg(p.getName() + " has been a VERY naughty, naughty boy.", ChatColor.RED);
            
            //Undo WorldEdits:
            if (senderIsConsole)
            {
            }
            else
            {
                server.dispatchCommand(sender, String.format("/undo %d %s", 15, p.getName()));
            }
            
            // deop
            p.setOp(false);

            // set gamemode to survival
            p.setGameMode(GameMode.SURVIVAL);

            // clear inventory
            p.getInventory().clear();

            // strike with lightning effect
            final Location target_pos = p.getLocation();
            for (int x = -1; x <= 1; x++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    final Location strike_pos = new Location(target_pos.getWorld(), target_pos.getBlockX() + x, target_pos.getBlockY(), target_pos.getBlockZ() + z);
                    target_pos.getWorld().strikeLightning(strike_pos);
                }
            }

            // ban IP Address
            String user_ip = p.getAddress().getAddress().getHostAddress();
            String[] ip_parts = user_ip.split("\\.");
            if (ip_parts.length == 4)
            {
                user_ip = String.format("%s.%s.*.*", ip_parts[0], ip_parts[1]);
            }
            TFM_Util.bcastMsg(String.format("Banning: %s, IP: %s.", p.getName(), user_ip), ChatColor.RED);
            server.banIP(user_ip);

            // ban username
            server.getOfflinePlayer(p.getName()).setBanned(true);

            // kick player
            p.kickPlayer("GTFO");
        }
        else
        {
            sender.sendMessage(TotalFreedomMod.MSG_NO_PERMS);
        }

        return true;

    }
}
