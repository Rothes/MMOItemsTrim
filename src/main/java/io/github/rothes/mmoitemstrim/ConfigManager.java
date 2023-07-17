package io.github.rothes.mmoitemstrim;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.HashMap;

public class ConfigManager {

    public final HashMap<TrimPattern, ItemFeature> patternMap = new HashMap<>();
    public final HashMap<TrimPattern, HashMap<TrimMaterial, ItemFeature>> extraMap = new HashMap<>();

    public ItemFeature getExtra(TrimPattern pattern, TrimMaterial material) {
        HashMap<TrimMaterial, ItemFeature> map = extraMap.get(pattern);
        if (map == null) return null;
        return map.get(material);
    }

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
            patternMap.put(pattern, new ItemFeature(section.getString(key + ".type"), section.getString(key + ".id")));
            ConfigurationSection subs = section.getConfigurationSection(key + ".trim-material");
            if (subs != null) {
                HashMap<TrimMaterial, ItemFeature> mm = new HashMap<>();
                extraMap.put(pattern, mm);
                for (String k : subs.getKeys(false)) {
                    TrimMaterial material = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(k));
                    if (material == null) {
                        plugin.getLogger().warning("未知材料: " + k);
                        continue;
                    }
                    mm.put(material, new ItemFeature(subs.getString(k + ".type"), subs.getString(k + ".id")));
                }

            }
        }

    }

    public class ItemFeature {
        public final String type;
        public final String id;

        public ItemFeature(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public MMOItem getItem() {
            return MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(type), id);
        }
    }

}
