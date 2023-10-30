package org.minecraftevent.race;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RaceCycle {

    public static boolean started = false;
    public static long startTime;

    public RaceCycle(){
        int sec = 10;
        startCountDown(sec);
        Race.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Race.getInstance(), () -> {
            for (EventPlayer player : Race.ready_players) {
                if (player.stage_name.equals("stage1")) {
                    player.free = true;
                }
            }
            startTime = System.currentTimeMillis();
            RaceCycle.started = true;
        }, sec * 20L);


    }

    private void startCountDown(int sec) {
        Race.getInstance().getServer().getScheduler().runTask(Race.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.BLUE + "Старт гонки через:");
            }
            for (int i = sec; i > 0; i--) {
                int finalI = i;
                Race.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Race.getInstance(), () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(ChatColor.BLUE + Integer.toString(finalI));
                    }
                }, (sec - i) * 20L);
            }
            Race.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Race.getInstance(), () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.BLUE + "Гонка началась!");
                }
            }, sec * 20L);
        });

    }
}
