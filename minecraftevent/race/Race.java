package org.minecraftevent.race;


import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecraftevent.race.commands.RaceCommand;

import java.io.PrintWriter;
import java.util.*;

public final class Race extends JavaPlugin {
    public static HashMap<String, ProtectedRegion> regions;
    public static List<Map<?, ?>> teams = new ArrayList<>();
    public static List<EventPlayer> ready_players;
    private static Race instance;

    public static PrintWriter writer;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        new RaceCommand();
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        saveRegions();
        savePlayers();
    }

    @Override
    public void onDisable() {
        if (!RaceCycle.started) {
            return;
        }
        for (EventPlayer player : Race.ready_players) {
            player.free = true;
        }
        Race.writer.close();
        RaceCycle.started = false;
        Race.getInstance().getServer().getScheduler().cancelTasks(Race.getInstance());
    }

    public static Race getInstance() {
        return instance;
    }

    public static String saveRegions() {
        regions = new HashMap<>();
        World world = Bukkit.getWorld("world");
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world));
        assert regionManager != null;

        List<Map<?, ?>> config_regions = Race.getInstance().getConfig().getMapList("regions");

        for (Map<?, ?> config_region : config_regions) {
            String region_name = (String) config_region.values().iterator().next();
            ProtectedRegion region = regionManager.getRegion(region_name);
            if (region == null) {
                return region_name;
            }
            regions.put((String) config_region.keySet().iterator().next(), region);
        }
        return null;
    }

    public static void savePlayers() {
        Race.ready_players = new ArrayList<>();
        teams = Race.getInstance().getConfig().getMapList("teams");
        String sender_team;
        String sender_stage;
        String sender_name;

        List<Map<?, ?>> teams = Race.teams;

        for (Map<?, ?> team : teams) {
            Object team_name = team.keySet().iterator().next();

            Map<?, ?> players = (Map<?, ?>) team.get(team_name);

            Set<?> stages = players.keySet();

            for (Object stage : stages) {
                sender_name = players.get(stage).toString();
                sender_team = team_name.toString();
                sender_stage = stage.toString();
                Race.ready_players.add(new EventPlayer(sender_name, sender_team, sender_stage, true));
            }
        }
    }
}

