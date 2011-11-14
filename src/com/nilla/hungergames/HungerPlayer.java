/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nilla.hungergames;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author telaeris
 */
public class HungerPlayer {

    
    private HungerWorld hw;
    
    //SQL Variables
    private String name;
    private boolean is_enrolled;
    private boolean is_entered;
    private boolean is_spectating;
    private boolean has_died;
    private int spectate_tp_count;
    private Location starting_location;
    
    //Local variables
    private boolean enter_confirmed;
    private int teleporting_for_games;
    
    //hmmmmmm
    private LinkedList<Location> locations_queue;

    
    public HungerPlayer(HungerWorld w, ResultSet res){
        //HungerGames.log("New HungerPlayer Created");
        hw = w;
        
        loadFromResultSet(res);
        locations_queue = new LinkedList<Location>();
    }
    
    //This is the actual instantiation the first time
    public HungerPlayer(HungerWorld w, Player p){
        hw = w;
        name = p.getName();
        //HungerGames.log("New HungerPlayer Created " + name );
    }
    private void loadFromResultSet(ResultSet res){
        try {
            name = res.getString("name");
            is_enrolled = res.getBoolean("is_enrolled");
            is_entered = res.getBoolean("is_entered");
            is_spectating = res.getBoolean("is_spectating");
            has_died = res.getBoolean("has_died");
            spectate_tp_count = Helper.ParseInt(res.getString("spectate_tp_count"));
            String starting_world = res.getString("starting_world");
            if(starting_world != null){
                World w = HungerGames.getInstance().getServer().getWorld(starting_world);
                int starting_x = Helper.ParseInt(res.getString("starting_x"));
                int starting_y = Helper.ParseInt(res.getString("starting_y"));
                int starting_z = Helper.ParseInt(res.getString("starting_z"));
                starting_location = new Location(w, starting_x, starting_y, starting_z);
            }
        } catch (SQLException ex) {
            Logger.getLogger(HungerPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    //Add a location so we can calculate how far this user is travelled
    public void addLocation(Location l){
        locations_queue.add(l);
        if(locations_queue.size() > 10){
            locations_queue.removeFirst();
        }
    }
    
    //Get the total distance they've moved over the last 10 records
    public int CalculateTotalLocationDistance(){
        int distance = 0;
        //make sure we're okay if we don't have enough locations
        if(locations_queue.size() < 10){
            return -1;
        }
        Location last = null;
        for(Location this_l: locations_queue){
            if(last == null){
                last = this_l;
            }
            else{
                distance += Helper.getDistance(last, this_l.getBlockX(), this_l.getBlockZ());
                last = this_l;
            }
                
        }
        return distance;
    }
    
    public boolean EnterConfirmed(){
        return enter_confirmed;
    }
    public void SetEnterConfirmed(){
        enter_confirmed = true;
    }
    public int getTeleporting(){
        return teleporting_for_games;
    }
    public void setTeleporting(int val){
        teleporting_for_games = val;
    }
    
    public int getSpectateTPCount(){
        return spectate_tp_count;
    }
    
    public void incrementSpectateTPCount(){
        spectate_tp_count += 1;
    }
    
    public void SetStartingLocation(Location l){
        starting_location = l;
    }
    
    public void SetEntered(boolean bEntered){
        is_entered = bEntered;
    }
    public void SetEnrolled(boolean bEnrolled){
        is_enrolled = bEnrolled;
    }
    public void SetSpectating(boolean bSpec){
        is_spectating = bSpec;
    }
    public void SetDied(boolean bVal){
        has_died = bVal;
    }
    public boolean HasDied(){
        return has_died;
    }
    
    public boolean Enrolled(){
        return is_enrolled;
    }
    public boolean Entered(){
        return is_entered;
    }
    public boolean Spectating(){
        return is_spectating;
    }
    public String getName(){
        return name;
    }
    public String getWorldName(){
        return hw.getName();
    }
    public Location getStartingLocation(){
        return starting_location;
    }
}
