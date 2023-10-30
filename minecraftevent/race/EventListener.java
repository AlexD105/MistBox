package org.minecraftevent.race;


import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.netzkronehd.wgregionevents.events.RegionEnterEvent;
import de.netzkronehd.wgregionevents.events.RegionLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EventListener implements Listener {

    public EventListener() {
    }


    @EventHandler
    public void onRegionLeave(RegionLeaveEvent e) {
        //if (e.getPlayer().isOp()) {
        //    return;
        //}
        if (Race.regions.get("lobby").equals(e.getRegion())){
            return;
        }
        for (String region_key : Race.regions.keySet()) {
            if (Race.regions.get(region_key).equals(e.getRegion())){
                for (EventPlayer player : Race.ready_players) {
                    if (player.name.equals(e.getPlayer().getName())){
                        if (!player.free) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.YELLOW + "Ты пока не можешь выйти из региона");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRegionEntered(RegionEnterEvent e){
        if (!RaceCycle.started) {
            return;
        }
        if (Race.regions.get("lobby").equals(e.getRegion())){
            return;
        }
        HashMap<String, ProtectedRegion> race_regions = Race.regions;
        for (String region_key : race_regions.keySet()) {
            if (race_regions.get(region_key).equals(e.getRegion())){

                for (EventPlayer ready_player : Race.ready_players){
                    if (ready_player.name.equals(e.getPlayer().getName())) {

                        if (region_key.equals("finish")) {
                            for (EventPlayer player : Race.ready_players){
                                if (player.team_name.equals(ready_player.team_name)){
                                    player.free = true;

                                    tpToLobby(player);

                                }
                            }
                            Race.writer.printf("%s, %s, %s, %s\n", ready_player.team_name, ready_player.stage_name, ready_player.name, ((double) (System.currentTimeMillis() - RaceCycle.startTime)) / 1000);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.sendMessage(ChatColor.GREEN + e.getPlayer().getName() + " финишировал!");
                            }
                            return;
                        }

                        Integer number_region = Integer.parseInt(String.valueOf(region_key.charAt(5)));
                        Integer number_player_nextstage = Integer.parseInt(String.valueOf(ready_player.stage_name.charAt(5))) + 1;
                        if (number_region.equals(number_player_nextstage)) {
                            for (EventPlayer another_player : Race.ready_players) {
                                Integer another_player_stage = Integer.parseInt(String.valueOf(another_player.stage_name.charAt(5)));
                                if (another_player_stage.equals(number_player_nextstage) && another_player.team_name.equals(ready_player.team_name)) {
                                    if (ready_player.free){
                                        Race.writer.printf("%s, %s, %s, %s\n", ready_player.team_name, ready_player.stage_name, ready_player.name, ((double) (System.currentTimeMillis() - RaceCycle.startTime)) / 1000);
                                    }
                                    ready_player.free = false;
                                    another_player.free = true;
                                }
                            }
                        }


                    }
                }
            }
        }
    }

    private void tpToLobby (EventPlayer player) {
        ProtectedRegion region = Race.regions.get("lobby");
        assert region != null;
        BlockVector3 max_block = region.getMaximumPoint();
        BlockVector3 min_block = region.getMinimumPoint();
        int x = (max_block.getX() + min_block.getX()) / 2;
        int z = (max_block.getZ() + min_block.getZ()) / 2;
        World world = Bukkit.getWorld("world");
        assert world != null;
        int y  = world.getHighestBlockYAt(x, z);

        for (Player onlinePlayer : Race.getInstance().getServer().getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(player.name)) {
                Race.getInstance().getServer().getScheduler().runTask(Race.getInstance(), () -> {
                    Objects.requireNonNull(Bukkit.getPlayer(player.name)).getInventory().clear();
                    Objects.requireNonNull(Bukkit.getPlayer(player.name)).teleport(new Location(world, x, y + 1, z));
                });
            }
        }


    }

}
