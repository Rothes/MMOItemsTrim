package io.github.rothes.mmoitemstrim;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class MMOItemsTrim extends JavaPlugin {

    public static ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);
        PluginCommand cmd = Bukkit.getPluginCommand("mmoitemstrim");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp() && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            configManager = new ConfigManager(this);
            sender.sendMessage("§a配置文件已重载");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.isOp()) {
            return Arrays.asList("reload");
        }
        return null;
    }

}
