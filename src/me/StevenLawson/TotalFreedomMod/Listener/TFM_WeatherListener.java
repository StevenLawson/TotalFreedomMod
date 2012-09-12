package me.StevenLawson.TotalFreedomMod.Listener;

import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.Plugin;

public class TFM_WeatherListener implements Listener
{
    private final Plugin plugin;
    @SuppressWarnings("unused")
	private final Server server;

    public TFM_WeatherListener()
    {
        this.plugin = TotalFreedomMod.plugin;
        this.server = plugin.getServer();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onThunderChange(ThunderChangeEvent event)
    {
        if (event.toThunderState() && TotalFreedomMod.disableWeather)
        {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWeatherChange(WeatherChangeEvent event)
    {
        if (event.toWeatherState() && TotalFreedomMod.disableWeather)
        {
            event.setCancelled(true);
            return;
        }
    }
}
