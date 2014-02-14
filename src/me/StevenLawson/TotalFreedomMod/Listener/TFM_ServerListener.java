package me.StevenLawson.TotalFreedomMod.Listener;

import me.StevenLawson.TotalFreedomMod.TFM_CommandBlocker;
import me.StevenLawson.TotalFreedomMod.TFM_ConfigEntry;
import me.StevenLawson.TotalFreedomMod.TFM_ServerInterface;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class TFM_ServerListener implements Listener
{
    // CommandBlockSetEvent does not exist in "vanilla" Bukkit/CraftBukkit.
    // Comment this method out if you want to compile this without a custom CraftBukkit.
    // Just make sure that enable-command-block=false in server.properties.
    // -Madgeek
    /* Temporary: Until we get a custom CB build out
     @EventHandler(priority = EventPriority.NORMAL)
     public void onCommandBlockSet(org.bukkit.event.server.CommandBlockSetEvent event)
     {
     Player player = event.getPlayer();
     String newCommandRaw = event.getNewCommand();

     if (!TFM_SuperadminList.isSeniorAdmin(player, true))
     {
     player.sendMessage(ChatColor.GRAY + "Only senior admins may set command block commands.");
     event.setCancelled(true);
     return;
     }

     Matcher matcher = Pattern.compile("^/?(\\S+)").matcher(newCommandRaw);
     if (matcher.find())
     {
     String topLevelCommand = matcher.group(1);
     if (topLevelCommand != null)
     {
     topLevelCommand = topLevelCommand.toLowerCase().trim();

     // We need to make it look like the command is coming from the console, so keep the player's name without the Player instance via dummy:
     if (TFM_CommandBlocker.getInstance().isCommandBlocked(topLevelCommand, new TFM_ServerListener_DummyCommandSender(player.getName()), false))
     {
     player.sendMessage(ChatColor.GRAY + "That command is blocked.");
     event.setCancelled(true);
     }
     }
     }
     }*/
    @Deprecated // Moved to TFM_TelnetListener
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRemoteServerCommand(RemoteServerCommandEvent event)
    {
        if (TFM_CommandBlocker.getInstance().isCommandBlocked(event.getCommand(), event.getSender()))
        {
            event.setCommand("");
        }
    }

    @Deprecated // Moved to TFM_TelnetListener
    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerCommand(ServerCommandEvent event)
    {
        if (TFM_CommandBlocker.getInstance().isCommandBlocked(event.getCommand(), event.getSender()))
        {
            event.setCommand("");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(ServerListPingEvent event)
    {
        event.setMotd(TFM_Util.randomChatColor() + "Total" + TFM_Util.randomChatColor() + "Freedom " + ChatColor.DARK_GRAY + "-" + TFM_Util.randomChatColor() + " Bukkit v" + TFM_ServerInterface.getVersion());

        if (TFM_ServerInterface.isIPBanned(event.getAddress().getHostAddress()))
        {
            event.setMotd(ChatColor.RED + "You are banned.");
        }
        else if (TFM_ConfigEntry.ADMIN_ONLY_MODE.getBoolean())
        {
            event.setMotd(ChatColor.RED + "Server is closed.");
        }
        else if (Bukkit.hasWhitelist())
        {
            event.setMotd(ChatColor.RED + "Whitelist enabled.");
        }
        else if (Bukkit.getOnlinePlayers().length >= Bukkit.getMaxPlayers())
        {
            event.setMotd(ChatColor.RED + "Server is full.");
        }
    }
}
