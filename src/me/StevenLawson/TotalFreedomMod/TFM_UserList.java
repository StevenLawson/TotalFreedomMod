package me.StevenLawson.TotalFreedomMod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TFM_UserList
{
    private static final String USERLIST_FILENAME = "userlist.yml";

    private static final Logger log = Logger.getLogger("Minecraft");

    private static TFM_UserList instance = null;

    private Map<String, TFM_UserListEntry> _userlist = new HashMap<String, TFM_UserListEntry>();
    private final Plugin _plugin;

    protected TFM_UserList(Plugin plugin)
    {
        _plugin = plugin;

        primeList();
    }

    private void primeList()
    {
        _userlist.clear();

        FileConfiguration saved_userlist = YamlConfiguration.loadConfiguration(new File(_plugin.getDataFolder(), USERLIST_FILENAME));

        for (String username : saved_userlist.getKeys(false))
        {
            TFM_UserListEntry entry = new TFM_UserListEntry(username, saved_userlist.getStringList(username));
            _userlist.put(username, entry);
        }

        for (Player p : _plugin.getServer().getOnlinePlayers())
        {
            addUser(p);
        }

        exportList();
    }

    private void exportList()
    {
        FileConfiguration new_userlist = new YamlConfiguration();

        for (TFM_UserListEntry entry : _userlist.values())
        {
            new_userlist.set(entry.getUsername(), entry.getIpAddresses());
        }

        try
        {
            new_userlist.save(new File(_plugin.getDataFolder(), USERLIST_FILENAME));
        }
        catch (IOException ex)
        {
            log.log(Level.SEVERE, null, ex);
        }
    }

    public static TFM_UserList getInstance(Plugin plugin)
    {
        if (instance == null)
        {
            instance = new TFM_UserList(plugin);
        }
        return instance;
    }

    public void addUser(Player p)
    {
        addUser(p.getName(), p.getAddress().getAddress().getHostAddress());
    }

    public void addUser(String username, String ip_address)
    {
        username = username.toLowerCase();
        
        TFM_UserListEntry entry = _userlist.get(username);
        if (entry == null)
        {
            entry = new TFM_UserListEntry(username);
        }

        _userlist.put(username, entry);

        if (entry.addIpAddress(ip_address))
        {
            exportList();
        }
    }

    public TFM_UserListEntry getEntry(Player p)
    {
        return getEntry(p.getName());
    }

    public TFM_UserListEntry getEntry(String username)
    {
        return _userlist.get(username.toLowerCase());
    }

    public void purge()
    {
        _userlist.clear();

        for (Player p : _plugin.getServer().getOnlinePlayers())
        {
            addUser(p);
        }
        
        exportList();
    }

    public class TFM_UserListEntry
    {
        private String _username;
        private List<String> _ip_addresses = new ArrayList<String>();

        public TFM_UserListEntry(String username, List<String>ip_addresses)
        {
           _username = username;
           _ip_addresses = ip_addresses;
        }

        public TFM_UserListEntry(String username)
        {
           _username = username;
        }

        public List<String> getIpAddresses()
        {
            return _ip_addresses;
        }

        public String getUsername()
        {
            return _username;
        }

        public boolean addIpAddress(String ip_address)
        {
            if (!_ip_addresses.contains(ip_address))
            {
                _ip_addresses.add(ip_address);
                return true;
            }
            return false;
        }
    }
}
