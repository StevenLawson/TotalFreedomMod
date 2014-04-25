package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.Bridge.TFM_DisguiseCraftBridge;
import me.StevenLawson.TotalFreedomMod.TFM_PlayerData;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Superadmin command - Purge everything! (except for bans).", usage = "/<command>")
public class Command_purgeall extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        TFM_Util.adminAction(sender.getName(), "Purging all player data", true);

        // Purge entities
        TFM_Util.TFM_EntityWiper.wipeEntities(true, true);

        // Undisguise all players
        TFM_DisguiseCraftBridge.undisguiseAllPlayers();

        for (Player player : server.getOnlinePlayers())
        {
            TFM_PlayerData playerdata = TFM_PlayerData.getPlayerData(player);

            // Unmute all players
            if (playerdata.isMuted())
            {
                playerdata.setMuted(false);
            }

            // Unblock all commands
            if (playerdata.allCommandsBlocked())
            {
                playerdata.setCommandsBlocked(false);
            }

            // Unhalt all players
            if (playerdata.isHalted())
            {
                playerdata.setHalted(false);
            }

            // Stop orbiting
            if (playerdata.isOrbiting())
            {
                playerdata.stopOrbiting();
            }

            // Unfreeze
            if (playerdata.isFrozen())
            {
                playerdata.setFrozen(false);
            }

            // Purge potion effects
            for (PotionEffect potion_effect : player.getActivePotionEffects())
            {
                player.removePotionEffect(potion_effect.getType());
            }

            // Uncage
            if (playerdata.isCaged())
            {
                playerdata.setCaged(false);
                playerdata.regenerateHistory();
                playerdata.clearHistory();
            }
        }

        // Clear auto-unmute and auto-unfreeze tasks
        if (TotalFreedomMod.mutePurgeTask != null)
        {
            TotalFreedomMod.mutePurgeTask.cancel();
        }

        TotalFreedomMod.allPlayersFrozen = false;
        if (TotalFreedomMod.freezePurgeTask != null)
        {
            TotalFreedomMod.freezePurgeTask.cancel();
        }

        // Remove all mobs
        Command_mp.purgeMobs();

        return true;
    }
}
