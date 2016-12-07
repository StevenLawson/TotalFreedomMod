

import me.StevenLawson.TotalFreedomMod.TFM_RollbackManager;
import me.StevenLawson.TotalFreedomMod.TFM_ServerInterface;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TFM_WorldEditBridge;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "IP-Ban a impostor, or famous player impostor.", usage = "/<command> <partialname> [reason]")
public class Command_fame extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        Player p;
        try
        {
            p = getPlayer(args[0]);
        }
        catch (PlayerNotFoundException ex)
        {
            playerMsg(ex.getMessage(), ChatColor.RED);
            return true;
        }

        String ban_reason = null;
        if (args.length >= 2)
        {
            ban_reason = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");
        }

        playerMsg("IP was banned.");

        TFM_WorldEditBridge.getInstance().undo(p, 15);

        TFM_RollbackManager.rollback(p.getName());



        String user_ip = p.getAddress().getAddress().getHostAddress();
        String[] ip_parts = user_ip.split("\\.");
        if (ip_parts.length == 4)
        {
            user_ip = String.format("%s.%s.*.*", ip_parts[0], ip_parts[1]);
        }
        TFM_Util.bcastMsg(String.format("Banning: %s, IP: %s.", p.getName(), user_ip), ChatColor.RED);
        TFM_ServerInterface.banIP(user_ip, ban_reason, null, null);

        p.kickPlayer(ChatColor.RED + "You have been ip-banned." + (ban_reason != null ? ("\nReason: " + ChatColor.YELLOW + ban_reason) : ""));

        return true;
    }
}