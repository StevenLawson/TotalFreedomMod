package me.StevenLawson.TotalFreedomMod.Listener;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import me.StevenLawson.TotalFreedomMod.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class TFM_PlayerListener implements Listener
{
    private final Plugin plugin;
    private static final Logger log = Logger.getLogger("Minecraft");
    @SuppressWarnings("unused")
    private final Server server;
    @SuppressWarnings("unused")
	private static final Random randomGenerator = new Random();

    public TFM_PlayerListener()
    {
        this.plugin = TotalFreedomMod.plugin;
        this.server = plugin.getServer();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        switch (event.getAction())
        {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
            {
                switch (event.getMaterial())
                {
                    case WATER_BUCKET:
                    {
                        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.COOKIE, 1));
                        player.sendMessage(ChatColor.GRAY + "Water buckets are currently disabled.");
                        event.setCancelled(true);
                        return;
                    }
                    case LAVA_BUCKET:
                    {
                        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.COOKIE, 1));
                        player.sendMessage(ChatColor.GRAY + "Lava buckets are currently disabled.");
                        event.setCancelled(true);
                        return;
                    }
                }
                break;
            }
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
            {
                switch (event.getMaterial())
                {
                    case STICK:
                    {
                        TFM_UserInfo playerdata = TFM_UserInfo.getPlayerData(player);
                        if (playerdata.mobThrowerEnabled())
                        {
                            Location player_pos = player.getLocation();
                            Vector direction = player_pos.getDirection().normalize();

                            LivingEntity rezzed_mob = (LivingEntity)player.getWorld().spawnEntity(player_pos.add(direction.multiply(2.0)), playerdata.mobThrowerCreature());
                            rezzed_mob.setVelocity(direction.multiply(playerdata.mobThrowerSpeed()));
                            playerdata.enqueueMob(rezzed_mob);

                            event.setCancelled(true);
                            return;
                        }
                        break;
                    }
                    case SULPHUR:
                    {
                        TFM_UserInfo playerdata = TFM_UserInfo.getPlayerData(player);
                        if (playerdata.isMP44Armed())
                        {
                            if (playerdata.toggleMP44Firing())
                            {
                                playerdata.startArrowShooter(plugin);
                            }
                            else
                            {
                                playerdata.stopArrowShooter();
                            }

                            event.setCancelled(true);
                            return;
                        }
                        break;
                    }
                    case BLAZE_ROD:
                    {
                        if (TotalFreedomMod.allowExplosions && (player.getName().equals("Madgeek1450") || player.getName().equals("markbyron")))
                        {
                            Block target_block = null;

                            if (event.getAction().equals(Action.LEFT_CLICK_AIR))
                            {
                                target_block = player.getTargetBlock(null, 120);
                            }
                            else
                            {
                                target_block = event.getClickedBlock();
                            }

                            if (target_block != null)
                            {
                                player.getWorld().createExplosion(target_block.getLocation(), 4F, true);
                                player.getWorld().strikeLightning(target_block.getLocation());
                            }
                            else
                            {
                                player.sendMessage("Can't resolve target block.");
                            }

                            event.setCancelled(true);
                            return;
                        }

                        break;
                    }
                }
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player p = event.getPlayer();
        TFM_UserInfo playerdata = TFM_UserInfo.getPlayerData(p);

        for (Entry<Player, Double> fuckoff : TotalFreedomMod.fuckoffEnabledFor.entrySet())
        {
            Player fuckoff_player = fuckoff.getKey();

            if (fuckoff_player.equals(p) || !fuckoff_player.isOnline())
            {
                continue;
            }

            double fuckoff_range = fuckoff.getValue().doubleValue();

            Location mover_pos = p.getLocation();
            Location fuckoff_pos = fuckoff_player.getLocation();

            double distance;
            try
            {
                distance = mover_pos.distance(fuckoff_pos);
            }
            catch (IllegalArgumentException ex)
            {
                continue;
            }

            if (distance < fuckoff_range)
            {
                event.setTo(fuckoff_pos.clone().add(mover_pos.subtract(fuckoff_pos).toVector().normalize().multiply(fuckoff_range * 1.1)));
                break;
            }
        }

        boolean do_freeze = false;
        if (TotalFreedomMod.allPlayersFrozen)
        {
            if (!TFM_Util.isUserSuperadmin(p))
            {
                do_freeze = true;
            }
        }
        else
        {
            if (playerdata.isFrozen())
            {
                do_freeze = true;
            }
        }

        if (do_freeze)
        {
            Location from = event.getFrom();
            Location to = event.getTo().clone();

            to.setX(from.getX());
            to.setY(from.getY());
            to.setZ(from.getZ());

            event.setTo(to);
        }

        if (playerdata.isCaged())
        {
            Location target_pos = p.getLocation().add(0, 1, 0);

            boolean out_of_cage = false;
            if (!target_pos.getWorld().equals(playerdata.getCagePos().getWorld()))
            {
                out_of_cage = true;
            }
            else
            {
                out_of_cage = target_pos.distance(playerdata.getCagePos()) > 2.5;
            }

            if (out_of_cage)
            {
                playerdata.setCaged(true, target_pos, playerdata.getCageMaterial(TFM_UserInfo.CageLayer.INNER), playerdata.getCageMaterial(TFM_UserInfo.CageLayer.OUTER));
                playerdata.regenerateHistory();
                playerdata.clearHistory();
                TFM_Util.buildHistory(target_pos, 2, playerdata);
                TFM_Util.generateCube(target_pos, 2, playerdata.getCageMaterial(TFM_UserInfo.CageLayer.OUTER));
                TFM_Util.generateCube(target_pos, 1, playerdata.getCageMaterial(TFM_UserInfo.CageLayer.INNER));
            }
        }

        if (playerdata.isOrbiting())
        {
            if (p.getVelocity().length() < playerdata.orbitStrength() * (2.0 / 3.0))
            {
                p.setVelocity(new Vector(0, playerdata.orbitStrength(), 0));
            }
        }

//        if (p.getItemInHand().getType() == Material.SPECKLED_MELON)
//        {
//            if (TFM_Util.isUserSuperadmin(p))
//            {
//                Location from_pos = event.getFrom().clone();
//                Location to_pos = event.getTo().clone();
//
//                /*
//                Block block_pos = user_pos.clone().add(user_pos.getDirection().multiply(-2.0)).getBlock();
//
//                if ((block_pos.isEmpty() || block_pos.isLiquid()))
//                {
//                    block_pos.setTypeIdAndData(Material.WOOL.getId(), DyeColor.values()[randomGenerator.nextInt(DyeColor.values().length)].getData(), false);
//                }
//                */
//            }
//        }

        if (TotalFreedomMod.landminesEnabled && TotalFreedomMod.allowExplosions)
        {
            Iterator<TFM_LandmineData> landmines = TFM_LandmineData.landmines.iterator();
            while (landmines.hasNext())
            {
                TFM_LandmineData landmine = landmines.next();

                Location landmine_pos = landmine.landmine_pos;
                if (landmine_pos.getBlock().getType() != Material.TNT)
                {
                    landmines.remove();
                    continue;
                }

                if (!landmine.player.equals(p))
                {
                    if (p.getWorld().equals(landmine_pos.getWorld()))
                    {
                        if (p.getLocation().distance(landmine_pos) <= landmine.radius)
                        {
                            landmine.landmine_pos.getBlock().setType(Material.AIR);

                            TNTPrimed tnt1 = landmine_pos.getWorld().spawn(landmine_pos, TNTPrimed.class);
                            tnt1.setFuseTicks(40);
                            tnt1.setPassenger(p);
                            tnt1.setVelocity(new Vector(0.0, 2.0, 0.0));

                            TNTPrimed tnt2 = landmine_pos.getWorld().spawn(p.getLocation(), TNTPrimed.class);
                            tnt2.setFuseTicks(1);

                            p.setGameMode(GameMode.SURVIVAL);
                            landmines.remove();
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeavesDecay(LeavesDecayEvent event){
    	event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        try
        {
    		String msg = event.getMessage();
            Player p = event.getPlayer();

            TFM_UserInfo playerdata = TFM_UserInfo.getPlayerData(p);
            playerdata.incrementMsgCount();
            
            // check for spam
            if (playerdata.getMsgCount() > 10)
            {
                TFM_Util.bcastMsg(p.getName() + " was automatically kicked for spamming chat.", ChatColor.RED);
                TFM_Util.autoEject(p, "Kicked for spamming chat.");

                playerdata.resetMsgCount();

                event.setCancelled(true);
                return;
            }
            
            // check for muted
            if(playerdata.isMuted()){
            	if(!TFM_Util.isUserSuperadmin(p))
            	{
            		p.sendMessage(ChatColor.RED + "You're muted, STFU!");
            		event.setCancelled(true);
            		return;
            	} else {
            		playerdata.setMuted(false);
            		return;
            	}
            }
            
            //check for caps
    		int caps = 0;
    		for (int i=0; i<msg.length(); i++)
    		{
    			//count caps
    			if (Character.isUpperCase(msg.charAt(i)))
    				caps++;
    		}
    		if(caps > 4)
    		{
    			event.setMessage(msg.toLowerCase());
    		}
    		
    		// check if message is too long
    		if(msg.toCharArray().length > 85)
    		{
    			p.sendMessage(ChatColor.GRAY + "Message is too long to send.");
    			event.setCancelled(true);
    		}
            
            // Old code :D
            /*
            if (Pattern.compile("^mad(?:geek)?(?:1450)?[\\?\\.\\!]?$").matcher(event.getMessage().toLowerCase()).find())
            {
                if (server.getPlayerExact("Madgeek1450") != null)
                {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.setFoodLevel(0);
                    p.setHealth(1);

                    TNTPrimed tnt1 = p.getWorld().spawn(p.getLocation(), TNTPrimed.class);
                    tnt1.setFuseTicks(40);
                    tnt1.setPassenger(p);
                    tnt1.setVelocity(new Vector(0.0, 2.0, 0.0));
                }
            }
			*/
            event.setMessage(ChatColor.stripColor(event.getMessage()));
        }
        catch (Exception ex)
        {
            log.log(Level.SEVERE, null, ex);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage();
        Player p = event.getPlayer();

        TFM_UserInfo playerdata = TFM_UserInfo.getPlayerData(p);
        playerdata.incrementMsgCount();

        if (playerdata.getMsgCount() > 10)
        {
            TFM_Util.bcastMsg(p.getName() + " was automatically kicked for spamming commands.", ChatColor.RED);
            TFM_Util.autoEject(p, "Kicked for spamming commands.");

            playerdata.resetMsgCount();

            TFM_Util.wipeEntities(true);

            event.setCancelled(true);
            return;
        }

        if (TotalFreedomMod.preprocessLogEnabled)
        {
            log.info(String.format("[PREPROCESS_COMMAND] %s(%s): %s", p.getName(), ChatColor.stripColor(p.getDisplayName()), command));
        }

        command = command.toLowerCase().trim();

        boolean block_command = false;

        //Commands that will auto-kick the user:
        if (Pattern.compile("^/stop").matcher(command).find())
        {
            if (!TFM_Util.isUserSuperadmin(p))
            {
                block_command = true;
            }
        }
        else if (Pattern.compile("^/reload").matcher(command).find())
        {
            if (!TFM_Util.isUserSuperadmin(p))
            {
                block_command = true;
            }
        }
        else if (Pattern.compile("^/save-").matcher(command).find())
        {
            if (!TFM_Util.isUserSuperadmin(p))
            {
                block_command = true;
            }
        }

        if (block_command)
        {
            TFM_Util.autoEject(p, "That command is prohibited.");
            TFM_Util.bcastMsg(p.getName() + " was automatically kicked for using harmful commands.", ChatColor.RED);
        }
        else
        {
            //Commands that will not auto-kick the user, but still deny:
            if (Pattern.compile("^/time").matcher(command).find())
            {
                p.sendMessage(ChatColor.GRAY + "Server-side time changing is disabled. Please use /ptime to set your own personal time.");
                block_command = true;
            }
            else if (Pattern.compile("^/md").matcher(command).find())
            {
                p.sendMessage(ChatColor.GRAY + "This server now uses DisguiseCraft instead of MobDisguise. Type /d to disguise and /u to undisguise.");
                block_command = true;
            }
        }

        if (block_command)
        {
            p.sendMessage(ChatColor.RED + "That command is prohibited.");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        if (TotalFreedomMod.autoEntityWipe)
        {
            if (event.getPlayer().getWorld().getEntities().size() > 750)
            {
                event.setCancelled(true);
            }
            else
            {
                event.getItemDrop().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event)
    {
        Player p = event.getPlayer();
        if (TotalFreedomMod.fuckoffEnabledFor.containsKey(p))
        {
            TotalFreedomMod.fuckoffEnabledFor.remove(p);
        }
        TFM_UserInfo playerdata = TFM_UserInfo.getPlayerData(p);
        playerdata.disarmMP44();
        if (playerdata.isCaged())
        {
            playerdata.regenerateHistory();
            playerdata.clearHistory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player p = event.getPlayer();
        if (TotalFreedomMod.fuckoffEnabledFor.containsKey(p))
        {
            TotalFreedomMod.fuckoffEnabledFor.remove(p);
        }
        TFM_UserInfo playerdata = TFM_UserInfo.getPlayerData(p);
        playerdata.disarmMP44();
        if (playerdata.isCaged())
        {
            playerdata.regenerateHistory();
            playerdata.clearHistory();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(PlayerPreLoginEvent event)
    {
    	String n = event.getName();
    	for(Player p : Bukkit.getOnlinePlayers())
    	{
    		if(p.getName().equalsIgnoreCase(n))
    		{
    			if(TFM_Util.isUserSuperadmin(p) || TotalFreedomMod.superadmins.contains(n.toLowerCase()))
    			{
    				// prevent the player from logging in
        			event.disallow(Result.KICK_OTHER, "You may not login as a superadmin.");
    			}
    			else
    			// prevent the player from logging in
    			event.disallow(Result.KICK_OTHER, "You are logged in from another location.");
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player p = event.getPlayer();
        String n = p.getName();
        try
        {
            TFM_UserList.getInstance(plugin).addUser(p);
            
            if(TFM_Util.isUserSuperadmin(p))
            {
            	TFM_Util.bcastMsg(ChatColor.AQUA + n + " is " + TFM_Util.getRank(p));
            	return;
            }
            
            if(TotalFreedomMod.superadmins.contains(n.toLowerCase()))
            {
            	if(!TFM_Util.isUserSuperadmin(p))
            	{
            		TFM_Util.bcastMsg("Warning: player " + n + " might be an impostor!", ChatColor.RED);
            		
            		p.getInventory().clear();
            		p.setOp(false);
            		p.setGameMode(GameMode.SURVIVAL);
            	}
            	else
            	{
            		p.setHealth(20);
            		p.setWhitelisted(true);
            		p.setGameMode(GameMode.CREATIVE);
            		p.setOp(true);
            	}
            }
        }
        catch (Throwable ex)
        {
        }
    }
}
