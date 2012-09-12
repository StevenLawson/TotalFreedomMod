package me.StevenLawson.TotalFreedomMod;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.StevenLawson.TotalFreedomMod.Commands.TFM_Command;
import me.StevenLawson.TotalFreedomMod.Listener.TFM_BlockListener;
import me.StevenLawson.TotalFreedomMod.Listener.TFM_EntityListener;
import me.StevenLawson.TotalFreedomMod.Listener.TFM_PlayerListener;
import me.StevenLawson.TotalFreedomMod.Listener.TFM_WeatherListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TotalFreedomMod extends JavaPlugin
{
	// logger
    public static Logger logger = Bukkit.getLogger();
    // server
    public static Server server = Bukkit.getServer();
    // plugin
    public static Plugin plugin;
    
    public static String pluginVersion;
    public static String buildNumber;
    public static String buildDate;
    public static String pluginName;
    public static String pluginAuthor;
    
    public static String mod = "0.6 SE";
    
    public static final long HEARTBEAT_RATE = 5L; //Seconds
    public static final String CONFIG_FILE = "config.yml";
    public static final String SUPERADMIN_FILE = "superadmin.yml";
    public static final String COMMAND_PATH = "me.StevenLawson.TotalFreedomMod.Commands";
    public static final String COMMAND_PREFIX = "Command_";
    public static final String MSG_NO_PERMS = ChatColor.YELLOW + "You do not have permission to use this command.";
    public static final String YOU_ARE_OP = ChatColor.YELLOW + "You are now op!";
    public static final String YOU_ARE_NOT_OP = ChatColor.YELLOW + "You are no longer op!";
    public static final String CAKE_LYRICS = "But there's no sense crying over every mistake. You just keep on trying till you run out of cake.";
    public static final String NOT_FROM_CONSOLE = "This command may not be used from the console.";
    
    public static boolean allPlayersFrozen = false;
    public static Map<Player, Double> fuckoffEnabledFor = new HashMap<Player, Double>();

    @Override
    public void onEnable()
    {
        setAppProperties(this);
        
        loadMainConfig();
        loadSuperadminConfig();

        TFM_UserList.getInstance(this);

        registerEventHandlers();

        server.getScheduler().scheduleAsyncRepeatingTask(this, new TFM_Heartbeat(this), HEARTBEAT_RATE * 20L, HEARTBEAT_RATE * 20L);

        log("Enabled! - v" + pluginVersion + "." + buildNumber + " by Madgeek1450");
        log("Modified by DarthSalamon - v" + mod);
        TFM_Util.deleteFolder(new File("./_deleteme"));

        if (generateFlatlands)
        {
            TFM_Util.generateFlatlands(flatlandsGenerationParams);
        }
    }

    @Override
    public void onDisable()
    {
        server.getScheduler().cancelTasks(plugin);
        log(pluginName + " is disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        try
        {
            Player sender_p = null;
            boolean senderIsConsole = false;
            if (sender instanceof Player)
            {
                sender_p = (Player) sender;
                logger.info(String.format("[PLAYER_COMMAND] %s(%s): /%s %s",
                        sender_p.getName(),
                        ChatColor.stripColor(sender_p.getDisplayName()),
                        commandLabel,
                        TFM_Util.implodeStringList(" ", Arrays.asList(args))));
            }
            else
            {
                senderIsConsole = true;
                logger.info(String.format("[CONSOLE_COMMAND] %s: /%s %s",
                        sender.getName(),
                        commandLabel,
                        TFM_Util.implodeStringList(" ", Arrays.asList(args))));
            }

            TFM_Command dispatcher;
            try
            {
                ClassLoader classLoader = TotalFreedomMod.class.getClassLoader();
                dispatcher = (TFM_Command) classLoader.loadClass(String.format("%s.%s%s", COMMAND_PATH, COMMAND_PREFIX, cmd.getName().toLowerCase())).newInstance();
                dispatcher.setPlugin(this);
            }
            catch (Throwable ex)
            {
                log("[" + getDescription().getName() + "] Command not loaded: " + cmd.getName() + ": " + ex.getMessage(), Level.SEVERE);
                sender.sendMessage(ChatColor.RED + "Command Error: Command not loaded: " + cmd.getName());
                return true;
            }

            try
            {
                return dispatcher.run(sender, sender_p, cmd, commandLabel, args, senderIsConsole);
            }
            catch (Throwable ex)
            {
                sender.sendMessage(ChatColor.RED + "Command Error: " + ex.getMessage());
            }

            dispatcher = null;
        }
        catch (Throwable ex)
        {
            log("Command Error: " + commandLabel + ex.getMessage());
            sender.sendMessage(ChatColor.RED + "Unknown Command Error.");
        }

        return true;
    }
    
    public static boolean allowFirePlace = false;
    public static Boolean allowFireSpread = false;
    public static Boolean allowLavaDamage = false;
    public static boolean allowLavaPlace = false;
    public static boolean allowWaterPlace = false;
    public static Boolean allowExplosions = false;
    public static double explosiveRadius = 4.0D;
    public static boolean autoEntityWipe = true;
    public static boolean nukeMonitor = true;
    public static int nukeMonitorCountBreak = 100;
    public static int nukeMonitorCountPlace = 25;
    public static double nukeMonitorRange = 10.0D;
    public static int freecamTriggerCount = 10;
    public static Boolean preprocessLogEnabled = true;
    public static Boolean disableNight = true;
    public static Boolean disableWeather = true;
    public static boolean landminesEnabled = false;
    public static boolean mp44Enabled = false;
    public static boolean mobLimiterEnabled = true;
    public static int mobLimiterMax = 50;
    public static boolean mobLimiterDisableDragon = true;
    public static boolean mobLimiterDisableGhast = true;
    public static boolean mobLimiterDisableSlime = true;
    public static boolean mobLimiterDisableGiant = true;
    public static boolean tossmobEnabled = false;
    public static boolean generateFlatlands = true;
    public static String flatlandsGenerationParams = "16,stone,32,dirt,1,grass";
    public static boolean allowFliudSpread = false;

    public void loadMainConfig()
    {
        TFM_Util.createDefaultConfiguration(CONFIG_FILE, this, getFile());
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), CONFIG_FILE));

        allowFirePlace = config.getBoolean("allow_fire_place", allowFirePlace);
        allowFireSpread = config.getBoolean("allow_fire_spread", allowFireSpread);
        allowLavaDamage = config.getBoolean("allow_lava_damage", allowLavaDamage);
        allowLavaPlace = config.getBoolean("allow_lava_place", allowLavaPlace);
        allowWaterPlace = config.getBoolean("allow_water_place", allowWaterPlace);
        allowExplosions = config.getBoolean("allow_explosions", allowExplosions);
        explosiveRadius = config.getDouble("explosiveRadius", explosiveRadius);
        autoEntityWipe = config.getBoolean("auto_wipe", autoEntityWipe);
        nukeMonitor = config.getBoolean("nuke_monitor", nukeMonitor);
        nukeMonitorCountBreak = config.getInt("nuke_monitor_count_break", nukeMonitorCountBreak);
        nukeMonitorCountPlace = config.getInt("nuke_monitor_count_place", nukeMonitorCountPlace);
        nukeMonitorRange = config.getDouble("nuke_monitor_range", nukeMonitorRange);
        freecamTriggerCount = config.getInt("freecam_trigger_count", freecamTriggerCount);
        preprocessLogEnabled = config.getBoolean("preprocess_log", preprocessLogEnabled);
        disableNight = config.getBoolean("disable_night", disableNight);
        disableWeather = config.getBoolean("disable_weather", disableWeather);
        landminesEnabled = config.getBoolean("landmines_enabled", landminesEnabled);
        mp44Enabled = config.getBoolean("mp44_enabled", mp44Enabled);
        mobLimiterEnabled = config.getBoolean("mob_limiter_enabled", mobLimiterEnabled);
        mobLimiterMax = config.getInt("mob_limiter_max", mobLimiterMax);
        mobLimiterDisableDragon = config.getBoolean("mob_limiter_disable_dragon", mobLimiterDisableDragon);
        mobLimiterDisableGhast = config.getBoolean("mob_limiter_disable_ghast", mobLimiterDisableGhast);
        mobLimiterDisableSlime = config.getBoolean("mob_limiter_disable_slime", mobLimiterDisableSlime);
        mobLimiterDisableGiant = config.getBoolean("mob_limiter_disable_giant", mobLimiterDisableGiant);
        tossmobEnabled = config.getBoolean("tossmob_enabled", tossmobEnabled);
        generateFlatlands = config.getBoolean("generate_flatlands", generateFlatlands);
        flatlandsGenerationParams = config.getString("flatlands_generation_params", flatlandsGenerationParams);
        allowFliudSpread = config.getBoolean("allow_fluid_spread", allowFliudSpread);
    }
    
    public static List<String> superadmins = new ArrayList<String>();
    public static List<String> superadmin_ips = new ArrayList<String>();
    
    public void loadSuperadminConfig()
    {
        TFM_Util.createDefaultConfiguration(SUPERADMIN_FILE, this, getFile());
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), SUPERADMIN_FILE));

        superadmins = new ArrayList<String>();
        superadmin_ips = new ArrayList<String>();

        for (String user : config.getKeys(false))
        {
            superadmins.add(user.toLowerCase().trim());

            List<String> user_ips = (List<String>) config.getStringList(user);
            for (String ip : user_ips)
            {
                ip = ip.toLowerCase().trim();
                if (!superadmin_ips.contains(ip))
                {
                    superadmin_ips.add(ip);
                }
            }
        }
    }

    private void registerEventHandlers()
    {
        PluginManager pm = server.getPluginManager();

        pm.registerEvents(new TFM_EntityListener(), this);
        pm.registerEvents(new TFM_BlockListener(), this);
        pm.registerEvents(new TFM_PlayerListener(), this);
        pm.registerEvents(new TFM_WeatherListener(), this);
    }

    private void setAppProperties(Plugin TFM_Plugin)
    {
        try
        {

        	
            InputStream in;
            Properties props = new Properties();

            in = getClass().getResourceAsStream("/appinfo.properties");
            props.load(in);
            in.close();
            
            plugin = TFM_Plugin;
            pluginName = this.getDescription().getName();
            pluginAuthor = this.getDescription().getAuthors().get(0);
            pluginVersion = this.getDescription().getVersion();
            buildNumber = props.getProperty("program.BUILDNUM");
            buildDate = props.getProperty("program.BUILDDATE");
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    public static void log(String message)
    {
    	logger.info("[" + pluginName + "] " + message);
    }
    public static void log(String message, Level level)
    {
    	logger.log(level, "[" + pluginName + "] " + message);
    }
}
