package org.minecraftevent.race.commands;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.command.CommandSender;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.minecraftevent.race.EventPlayer;
import org.minecraftevent.race.Race;
import org.minecraftevent.race.RaceCycle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;



public class RaceCommand extends AbstractCommand {

    public RaceCommand() {
        super("race");
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) throws IOException {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "add args");
            return;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You don't have permission");
                return;
            }
            Race.getInstance().reloadConfig();

            String region_name = Race.saveRegions();
            if (region_name == null){
                sender.sendMessage(ChatColor.GREEN + "Конфиг успешно обновлен");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Не найден регион - " + region_name);
            }
            Race.savePlayers();
            return;
        }

        if (args[0].equalsIgnoreCase("ready")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You don't have permission");
                return;
            }
            for (Player onlinePlayer : Race.getInstance().getServer().getOnlinePlayers()) {
                for (EventPlayer eventPlayer : Race.ready_players) {
                    if (onlinePlayer.getName().equals(eventPlayer.name)) {

                        String player_stage;
                        player_stage = eventPlayer.stage_name;

                        World world = Bukkit.getWorld("world");

                        ProtectedRegion region = Race.regions.get(player_stage + "start");
                        assert region != null;
                        BlockVector3 max_block = region.getMaximumPoint();
                        BlockVector3 min_block = region.getMinimumPoint();
                        int x = (max_block.getX() + min_block.getX()) / 2;
                        int z = (max_block.getZ() + min_block.getZ()) / 2;
                        assert world != null;
                        int y = world.getHighestBlockYAt(x, z);

                        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
                        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
                        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
                        meta.setColor(Color.fromRGB(Integer.parseInt(Objects.requireNonNull(Race.getInstance().getConfig().getString("team_colors." + eventPlayer.team_name)), 16)));

                        helmet.setItemMeta(meta);
                        Objects.requireNonNull(onlinePlayer).getInventory().clear();
                        Objects.requireNonNull(onlinePlayer).getInventory().setHelmet(helmet);


                        Objects.requireNonNull(Bukkit.getPlayer(eventPlayer.name)).teleport(new Location(world, x, y + 1, z));
                        eventPlayer.free = false;
                    }
                }
            }
            return;
        }
        if (args[0].equalsIgnoreCase("start")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You don't have permission");
                return;
            }

            if (!RaceCycle.started) {
                File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Race")).getDataFolder(), "result.csv");
                file.delete();
                if (!file.createNewFile()) {
                    sender.sendMessage(ChatColor.YELLOW + "Не удалось открыть файл для записи");
                    return;
                }
                Race.writer = new PrintWriter(file);
                Race.writer.println("team, stage, name, time");
                new RaceCycle();


            }
            else {
                sender.sendMessage(ChatColor.YELLOW + "Гонка уже началась");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("end")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You don't have permission");
                return;
            }
            if (!RaceCycle.started) {
                sender.sendMessage(ChatColor.YELLOW + "Невозможно закончить игру, которая не начиналась(");
                return;
            }
            for (EventPlayer player : Race.ready_players) {
                if (!player.free) {
                    player.free = true;
                    Objects.requireNonNull(Bukkit.getPlayer(player.name)).getInventory().clear();
                }

            }
            Race.writer.close();
            RaceCycle.started = false;
            sender.sendMessage(ChatColor.GREEN + "Файл записан, запертые игроки отпущены");
            Race.getInstance().getServer().getScheduler().cancelTasks(Race.getInstance());
            return;
        }
        sender.sendMessage(ChatColor.RED + "Unknown command: " + args[0]);
    }
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? Lists.newArrayList("ready", "start", "reload", "end") : Lists.newArrayList();
    }

}


