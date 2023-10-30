package org.minecraftevent.race;

public class EventPlayer {

    public String name;
    public String team_name;
    public String stage_name;
    public Boolean free;

    public EventPlayer(String name, String team_name, String stage_name, Boolean free) {
        this.name = name;
        this.team_name = team_name;
        this.stage_name = stage_name;
        this.free = free;
    }
}
