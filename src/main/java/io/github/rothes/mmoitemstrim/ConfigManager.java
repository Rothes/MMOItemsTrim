package io.github.rothes.mmoitemstrim;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.HashMap;

public class ConfigManager {

    public final HashMap<TrimPattern, String> idMap = new HashMap<>();
    public final HashMap<TrimPattern, String> typeMap = new HashMap<>();

    ConfigManager(MMOItemsTrim plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("options.trim-pattern");
        if (section == null)
            return;
        for (String key : section.getKeys(false)) {
            TrimPattern pattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(key));
            if (pattern == null) {
                plugin.getLogger().warning("未知模板: " + key);
                continue;
            }
            idMap.put(pattern, section.getString(key + ".id"));
            typeMap.put(pattern, section.getString(key + ".type"));
        }

    }

}
