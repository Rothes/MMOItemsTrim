package io.github.rothes.mmoitemstrim;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MMOItemsTrim extends JavaPlugin {

    public static ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);
    }

}
