package com.akuma.protection;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

public class ProtectionPlus extends JavaPlugin implements Listener {

    private final HashSet<UUID> needLogin = new HashSet<>();
    private File userDataFile;
    private FileConfiguration userData;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        createUserDataFile();
        getLogger().info("ProtectionPlus is protecting Java players!");
    }

    private void createUserDataFile() {
        userDataFile = new File(getDataFolder(), "users.yml");
        if (!userDataFile.exists()) {
            userDataFile.getParentFile().mkdirs();
            saveResource("users.yml", false);
        }
        userData = YamlConfiguration.loadConfiguration(userDataFile);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Skip Bedrock players entirely
        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        needLogin.add(player.getUniqueId());
        if (userData.contains("users." + player.getName())) {
            player.sendMessage("§6[AkumaPlay] §ePlease login: §f/login <password>");
        } else {
            player.sendMessage("§6[AkumaPlay] §bWelcome! Please register: §f/register <password>");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (!needLogin.contains(player.getUniqueId())) return;

        if (args[0].equalsIgnoreCase("/register") && args.length > 1) {
            userData.set("users." + player.getName(), args[1]);
            saveUserData();
            needLogin.remove(player.getUniqueId());
            player.sendMessage("§aRegistered successfully!");
            event.setCancelled(true);
        } else if (args[0].equalsIgnoreCase("/login") && args.length > 1) {
            String savedPass = userData.getString("users." + player.getName());
            if (args[1].equals(savedPass)) {
                needLogin.remove(player.getUniqueId());
                player.sendMessage("§aWelcome back!");
            } else {
                player.sendMessage("§cWrong password!");
            }
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
            player.sendMessage("§cYou must login first!");
        }
    }

    private void saveUserData() {
        try { userData.save(userDataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // Freeze players
    @EventHandler public void onMove(PlayerMoveEvent e) { if(needLogin.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onChat(AsyncPlayerChatEvent e) { if(needLogin.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
}
