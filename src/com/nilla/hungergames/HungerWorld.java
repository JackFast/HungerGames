/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nilla.hungergames;


import com.wimbli.WorldBorder.BorderData;

import com.nilla.hungergames.managers.SettingsManager;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import com.wimbli.WorldBorder.WBCommand;
import com.wimbli.WorldBorder.WorldBorder;
import java.util.Date;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
/**
 *
 * @author telaeris
 */
public class HungerWorld {
    
    private static HungerGames plugin;
    private World world;
    private String world_name;
    private List<HungerPlayer> playerList;
    
    private Date start_time;
    private Date last_entry_time;
    private Date end_time;
    private Date shrink_start;
    private int radius;
    private int original_radius;
    private int center_x;
    private int center_z;
    private int spectator_x;
    private int spectator_y;
    private int spectator_z;
    private int spectator_x2;
    private int spectator_y2;
    private int spectator_z2;
    private int minimum_radius;
    private int shrink_rate;
    private String shrink_rate_interval;    
    private int max_players;
    private int tp_player_distance;
    private int spectator_tp_limit;
    private int player_move_distance;

    
    
    private static final DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");  
    private static WorldBorder wb_plugin;
    private static WorldEditPlugin we_plugin;
    private static WorldGuardPlugin wg_plugin;
    private static BorderData border;
    private SettingsManager sm; 
    
    public HungerWorld(World w, String name) {
        playerList = new ArrayList<HungerPlayer>();   
        
        plugin = HungerGames.getInstance();
        
        world = w;
        world_name = name;
        
        sm = plugin.getSettingsManager();
        
        //Load up the world with the defaults
        
        radius = sm.getDefaultRadius();
        original_radius = radius;
        minimum_radius = sm.getDefaultMinimumRadius();
        center_x = sm.getDefaultCenterX();
        center_z = sm.getDefaultCenterZ();
        shrink_rate = sm.getDefaultShrinkRate();
        shrink_rate_interval = sm.getDefaultShrinkRateInterval();
        max_players = sm.getDefaultMaxPlayers();
        tp_player_distance = sm.getDefaultTpPlayerDistance();
        spectator_tp_limit = sm.getDefaultSpectatorTpLimit();
        wb_plugin = plugin.getWorldBorderPlugin();
        we_plugin = plugin.getWorldEditPlugin();
        wg_plugin = plugin.getWorldGuardPlugin();
        player_move_distance = 0;
        //create Calendar instance
        Calendar now = Calendar.getInstance();
 

        //add minutes to current date using Calendar.add method
        now.add(Calendar.MINUTE,-1);
        //start_time = now.getTime();
        now.add(Calendar.MINUTE,2);
        //shrink_start = now.getTime();
        //last_entry_time = now.getTime();
        now.add(Calendar.YEAR,1);
        end_time = now.getTime();
      
        setBorders();

    
    }
    
    public void SetNewRadius(int iRadius){
        radius = iRadius;
        setBorders();
        plugin.getStorageManager().updateHungerWorld(this);
    }
    public void setBorders(){
        if(wb_plugin != null){
            border = wb_plugin.GetWorldBorder(world_name);
            
            if(border == null){
                CommandSender cs = plugin.getServer().getConsoleSender();
                
                //Send a command as the console to CREATE the border
                //Command.broadcastCommandMessage(cs, "wb " + getName() + " set " + radius + " " + center_x + " " + center_z);
                WBCommand cmd = new WBCommand(wb_plugin);
                
                //WBCommand cmd2
                String[] args = new String[5];
                args[0] = getName();
                args[1] = "set";
                args[2] = Integer.toString(radius);
                args[3] = Integer.toString(center_x);
                args[4] = Integer.toString(center_z);
                
                boolean ret = cmd.onCommand(cs,null,"", args);
                if(ret){
                    HungerGames.log("WorldBorder Command To create new border Successful");
                }
                else{
                    HungerGames.log("WorldBorder Command To create new border failed");
                }
                    
//                wb_plugin.onCommand(, cmd, shrink_rate_interval, args);
            }
            else{
             border.setData(center_x, center_z, radius, false);   
            }
                
            
            
       //     wb_plugin.saveConfig();
        }
    }
           
    public World getWorld(){
        return world;
    }
    public List<HungerPlayer> getPlayers(){
        return playerList;
    }
    public void setPlayers(List<HungerPlayer> pl){
        playerList = pl;
    }

    public String getDisplayName(){
        return sm.CCWorldName + world_name + sm.CCDefaultText;
    }
    public String getName(){
        return world_name;
    }
    
    public int getEnrolledPlayerCount(){
        int iCount = 0;
        for(int i = 0; i < playerList.size(); i++){
            if(playerList.get(i).Enrolled()){
                iCount++;           
            } 
        }
        return iCount;
    }
    public int getEnteredPlayerCount(){
        int iCount = 0;
        for(int i = 0; i < playerList.size(); i++){
            if(playerList.get(i).Entered()){
                iCount++;           
            }
        }
        return iCount;
    }
    public int getSpectatingPlayerCount(){
        int iCount = 0;
        for(int i = 0; i < playerList.size(); i++){
            if(playerList.get(i).Spectating()){
                iCount++;           
            }
        }
        return iCount;
    }
    

    
    public void loadFromResultSet(ResultSet res){
        //Set the values for all the fields from the DB here
        //The exception would be the world, that's needed ahead of time
        try{
            start_time = Helper.ParseDate(res.getString("start_time"));
            last_entry_time = Helper.ParseDate(res.getString("last_entry_time"));
            end_time = Helper.ParseDate(res.getString("end_time"));
            shrink_start = Helper.ParseDate(res.getString("shrink_start"));
            radius  = Helper.ParseInt(res.getString("radius"));
            original_radius = Helper.ParseInt(res.getString("original_radius"));
            center_x = Helper.ParseInt(res.getString("center_x"));
            center_z = Helper.ParseInt(res.getString("center_z"));
            spectator_x = Helper.ParseInt(res.getString("spectator_x"));
            spectator_y = Helper.ParseInt(res.getString("spectator_y"));
            spectator_z = Helper.ParseInt(res.getString("spectator_z"));            
            spectator_x2 = Helper.ParseInt(res.getString("spectator_x2"));
            spectator_y2 = Helper.ParseInt(res.getString("spectator_y2"));
            spectator_z2 = Helper.ParseInt(res.getString("spectator_z2"));
            minimum_radius = Helper.ParseInt(res.getString("minimum_radius"));
            shrink_rate = Helper.ParseInt(res.getString("shrink_rate"));
            shrink_rate_interval = res.getString("shrink_rate_interval");
            max_players = Helper.ParseInt(res.getString("max_players"));
            tp_player_distance = Helper.ParseInt(res.getString("tp_player_distance"));
            spectator_tp_limit = Helper.ParseInt(res.getString("spectator_tp_limit"));
    }
        catch(Exception ex){
            
        }
    }
    /*
     START - start time
     * END - end time
     * RADIUS - world radius
     * CENTER - world center
     * MINIMUM_RADIUS - shrink min size
     * SHRINK_RATE - shrink rate
     * SHRINK_START - shrink start time
     * MAX_PLAYERS - max # players
     * TP_MIN_DISTANCE - TP player distance
     * SPECTATOR_TP_LIMIT - # TPs allowed per games per spectator
     */
    
    public String GetPrintableDateDifference(Date input_time, String sBefore, String sAfter){
        if(input_time == null){
            return "not set";
        }
        long diffInSeconds = 0;
        String suffix = "";
        String ret = "";
        if(input_time.before(new Date())){
            ret = sBefore;
            diffInSeconds = (new Date().getTime() - input_time.getTime()) / 1000;
            suffix = " ago";
        }
        else{
            ret = sAfter;
            diffInSeconds = (input_time.getTime() - new Date().getTime()) / 1000;
            suffix = " from now";
        }
        
        
        long minutes = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hours = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24));
        
        if(days > 0){
            ret += days + " days, ";
        }
        return ret + hours + " hours, " + minutes + " minutes" + suffix;
    }
    public void SendWorldInfoToUser(CommandSender sender){
        

        
        ChatBlock cb = new ChatBlock();
        cb.addRow(sm.CCDefaultText + "World: " +  getDisplayName());
        cb.addRow(sm.CCParamName + "  Center: " + sm.CCParamValue + center_x + " " + center_z);
        cb.addRow(sm.CCParamName + "  Radius: " + sm.CCParamValue + radius);
        if(radius != original_radius){
            cb.addRow(sm.CCParamName + "  Original Radius: " + sm.CCParamValue + original_radius);
        }
        cb.addRow(sm.CCParamName + "  " + GetPrintableDateDifference(getStartTime(), "Started: " + ChatColor.RED, "Starts: " + sm.CCParamValue ));
        cb.addRow(sm.CCParamName + "  " + sm.CCParamValue + GetPrintableDateDifference(getLastEntryTime(),"Last Entry Time: ", "Last Entry Time: "));
        cb.addRow(sm.CCParamName + "  " + GetPrintableDateDifference(getEndTime(), "Ended: " + ChatColor.RED, "Ends: " + sm.CCParamValue));      
        cb.addRow(sm.CCParamName + "  " + GetPrintableDateDifference(getShrinkStart(), "Shrink Starts: " + sm.CCParamValue, "Shrink Started: " + ChatColor.YELLOW));
        cb.addRow(sm.CCParamName + "  Shrink Rate: " + sm.CCParamValue + shrink_rate + " per " + shrink_rate_interval);
        cb.addRow(sm.CCParamName + "  Minimum Radius: " + sm.CCParamValue + minimum_radius);
        
        if(max_players > 0){
            cb.addRow(sm.CCParamName + "  Max # Players: " + sm.CCParamValue + max_players);    
        }
        
        cb.addRow(sm.CCParamName + "  TP Insertion Distance: " + sm.CCParamValue + tp_player_distance);
        
        if(spectator_tp_limit > 0){
            cb.addRow(sm.CCParamName + "  Max # Spectator TPs: " + sm.CCParamValue + spectator_tp_limit);    
        }
        cb.sendBlock(sender);
    }
    
    //Get a list of players who are in the game or spectating the game
    public void PlayerList(CommandSender sender){
        int line_size = 90;
        ChatBlock cb = new ChatBlock();
        List<HungerPlayer> enrolled = new ArrayList<HungerPlayer>();
        List<HungerPlayer> dead = new ArrayList<HungerPlayer>();
        List<HungerPlayer> entered = new ArrayList<HungerPlayer>();
        for(int i = 0; i < playerList.size();i++){
            if(playerList.get(i).HasDied()){
                dead.add(playerList.get(i));
            }
            else if(playerList.get(i).Entered()){
                entered.add(playerList.get(i));
            }
            else if(playerList.get(i).Enrolled()){
                enrolled.add(playerList.get(i));
            }
        }
        
        cb.addRow(getDisplayName() + " Player List");
        if(entered.size() > 0){
            cb.addRow(sm.CCDefaultText + "Active Players: (" + entered.size() + ")");
            String sHunger = "   ";
            for(int i = 0; i < entered.size(); i++){
                HungerPlayer hp = entered.get(i);
                if(sHunger.length() + hp.getName().length() > line_size)
                {
                    cb.addRow(sHunger);
                    sHunger = "";
                }
                
                //Add the block if it's too long as its own row
                sHunger += ChatColor.GREEN + hp.getName();
                if(i < entered.size() - 1){
                    sHunger += sm.CCDefaultText + ", ";
                }                
            }
            if(sHunger.length() > 0)
            {
                cb.addRow(sHunger);
            }
        }
        if(dead.size() > 0){
            cb.addRow(sm.CCDefaultText + "Dead Players: (" + dead.size() + ")");
            String sHunger = "   ";
            for(int i = 0; i < dead.size(); i++){
                HungerPlayer hp = dead.get(i);
                if(sHunger.length() + hp.getName().length() > line_size)
                {
                    cb.addRow(sHunger);
                    sHunger = "";
                }
                sHunger += ChatColor.RED + hp.getName();
                if(i < dead.size() - 1){
                    sHunger += sm.CCDefaultText + ", ";
                }
            }
            if(sHunger.length() > 0)
            {
                cb.addRow(sHunger);
            }
        }
        if(enrolled.size() > 0){
            cb.addRow(sm.CCDefaultText + "Enrolled Players: (" + enrolled.size() + ")");
            String sHunger = "   ";
            for(int i = 0; i < enrolled.size(); i++){
                HungerPlayer hp = enrolled.get(i);
                if(sHunger.length() + hp.getName().length() > line_size)
                {
                    cb.addRow(sHunger);
                    sHunger = "";
                }
                
                sHunger += ChatColor.GRAY + hp.getName();
                if(i < enrolled.size() - 1){
                    sHunger += sm.CCDefaultText + ", ";
                }
            }
            if(sHunger.length() > 0)
            {
                cb.addRow(sHunger);
            }
        }
        if((enrolled.isEmpty()) && (entered.isEmpty()) && (dead.isEmpty()))
        {
            cb.addRow(ChatColor.RED + "0 Enrolled Players");
        }
        
        cb.sendBlock(sender);
//        ChatBlock.sendMessage(sender, sm.CCWorldName + getName() + sm.CCDefaultText + " Players: " + sRes.toString());
    }
    
    public String OpenForMessage(boolean enrolling){
        if(!enrolling && (start_time == null))
        {
            return "No Start Time Set.";
        }
        
        
        if(end_time == null){
            return "No End Time Set.";
        }
        
        if(end_time.before(new Date())){
            return "Games have already Ended.";
        }
        if(!enrolling && start_time.after(new Date())){
            return "Games have not started yet.";
        }
        
            
        //An empty response is a good response
        return "";
    }
    
    public boolean IsRunning(){
        if(start_time == null){
            return false;
        }
        
        if(end_time == null){
            return false;
        }
        
        if(end_time.before(new Date())){
            return false;
        }
        if(start_time.after(new Date())){
            return false;
        }
        
            
        return true;
    }
            

        //Toggles Enrollment of the player in the games
    public void UnEnroll(Player player){
        HungerPlayer hp = findHungerPlayer(player.getName());
        if((hp == null) || (!hp.Enrolled())){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You are not Enrolled.");
            return;
        }
        if((end_time != null) && (end_time.before(new Date()))){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " Games are already ended.");
            return;            
        }
        if(hp.HasDied()){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You already played and died.  Bad player!. ");
            //Damage the player just for good measure
            player.damage(2);
            return;
        }
        if(hp.Entered()){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " Can't unenroll after Entering Games! ");
            return;
        }
        hp.SetEnrolled(false);
        
        plugin.getStorageManager().updateHungerPlayer(hp);
        ChatBlock.sendMessage(player, getDisplayName() + " You are now UnEnrolled from these games.");
        HungerGames.log(getDisplayName() + " Player ia UnEnrolled from games: " + player.getName());
    }
    //Toggles Enrollment of the player in the games
    public void Enroll(Player player){
        
        //Make sure they player is enrolled
        HungerPlayer hp = findHungerPlayer(player.getName());
        if(hp == null){
            
            //This is a good thing.
            hp = new HungerPlayer(this, player);
            //Do actual mysql insert here
            plugin.getStorageManager().insertHungerPlayer(hp);
            playerList.add(hp);
        }
        String sOpenMessage = OpenForMessage(true);
        //Check that the games are open for entering
        if(!sOpenMessage.equals((""))){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You may not Enroll. " + sOpenMessage);
            return;
        }
        

        //Punish the player for trying to re-enroll
        if(hp.HasDied()){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You already played and died.  Bad player!. ");
            //Damage the player just for good measure
            player.damage(2);
            return;
        }
        
        if(hp.Entered()){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You are already in the games! ");
            return;
        }

        if(hp.Enrolled()){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You are already Enrolled. ");
            return;
        }
        if((last_entry_time != null)&&(last_entry_time.before(new Date()))){
            ChatBlock.sendMessage(player, getDisplayName() + " Last Entry Time has already passed.  You may spectate though!");
            return;
        }
        hp.SetEnrolled(true);
        
        plugin.getStorageManager().updateHungerPlayer(hp);
        ChatBlock.sendMessage(player, getDisplayName() + " You are now Enrolled in these games.  You should '/hg enter' once the games have started.");
        
        //Announce that the place has enrolled in the games
        ChatColor defaultC = plugin.getSettingsManager().CCDefaultText;
        plugin.AnnounceAll(defaultC + player.getDisplayName() + " has " + ChatColor.GOLD + "enrolled " + defaultC + "in the " + getDisplayName() + " games.");
        
    }
    //Enter a player into the games after the games have started
    public void Enter(Player player){
        //Make sure they player is enrolled
        HungerPlayer hp = findHungerPlayer(player.getName());
        if((hp == null)|| (!hp.Enrolled())){
            ChatBlock.sendMessage(player, getDisplayName() +  " Must Be Enrolled in the Games to Enter");
            return;
        }
        //Punish the player for trying to re-enroll
        if(hp.HasDied()){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You already played and died.  Bad player!");
            //Damage the player just for good measure
            player.damage(2);
            return;
        }

        String sOpenMessage = OpenForMessage(false);
        //Check that the games are open for entering
        if(!sOpenMessage.equals((""))){
            ChatBlock.sendMessage(player, getDisplayName() + " You may not enter. " + sOpenMessage);
            return;
        }
        
        if(hp.Entered()){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You are already in the games.  Bad player!");
            //Damage the player just for good measure
            player.damage(2);
            return;
        }
        
        if(getWorld() == null){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " World Does Not Exist!  Please see Administrator.");
            return;
        }

        
        if((last_entry_time != null)&&(last_entry_time.before(new Date()))){
            hp.SetEnrolled(false);
            plugin.getStorageManager().updateHungerPlayer(hp);
            ChatBlock.sendMessage(player, getDisplayName() + " Last Entry Time Expired.  You are now unenrolled and may spectate.");
            return;
        }
        
        if(!hp.EnterConfirmed()){
            hp.SetEnterConfirmed();
            ChatBlock.sendMessage(player, getDisplayName() + " WARNING: Your inventory will be CLEARED if you continue.  Type '" + ChatColor.YELLOW + "/hg enter " + world_name  + plugin.getSettingsManager().CCDefaultText + "' to confirm you understand.");
            return;
        }
        
        //Set the players starting location to where they are now.
        Location playerStartingLocation = player.getLocation();
        if(TeleportEnteringPlayer(hp,player)){
            
            hp.SetStartingLocation(playerStartingLocation);
            hp.SetEntered(true);
            
            //Clear their inventory
            PlayerInventory pi = player.getInventory();
            
            pi.setBoots(null);
            pi.setChestplate(null);
            pi.setHelmet(null);
            pi.setLeggings(null);
            
            pi.clear();
            
            ChatBlock.sendMessage(player, getDisplayName() + " Welcome to the Games!  Good luck!  You will be returned to your last position when you die.");
        }
        else{
            ChatBlock.sendMessage(player, ChatColor.RED + "ERROR: " + getDisplayName() + " Could not Enter you into the games. There might not be any safe space available to teleport you.");
        }
        plugin.getStorageManager().updateHungerPlayer(hp);
        
        
        ChatColor defaultC = plugin.getSettingsManager().CCDefaultText;
        plugin.AnnounceEnrolled(defaultC + player.getDisplayName() + " has " + ChatColor.GOLD + "entered " + defaultC + "the " + getDisplayName() + " games.");

        
    }
    
    private static final int limTop = 120, limBot = 1;
    // find closest safe Y position from the starting position
    private double getSafeY(int X, int Y, int Z)
    {
            // Expanding Y search method adapted from Acru's code in the Nether plugin

            for(int y1 = Y, y2 = Y; (y1 > limBot) || (y2 < limTop); y1--, y2++){
                    // Look below.
                    if(y1 > limBot){
                            if (isSafeSpot(world, X, y1, Z))
                                    return (double)y1;
                    }

                    // Look above.
                    if(y2 < limTop && y2 != y1){
                            if (isSafeSpot(world, X, y2, Z))
                                    return (double)y2;
                    }
            }

            return -1.0;	// no safe Y location?!?!? Must be a rare spot in a Nether world or something
    }
    //these material IDs are acceptable for places to teleport player; breathable blocks and water
    private static final LinkedHashSet<Integer> safeOpenBlocks = new LinkedHashSet<Integer>(Arrays.asList(
             new Integer[] {0, 6, 8, 9, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 55, 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 83, 90, 93, 94, 96, 104, 105, 106}
    ));

    //these material IDs are ones we don't want to drop the player onto, like cactus or lava or fire
    private static final LinkedHashSet<Integer> painfulBlocks = new LinkedHashSet<Integer>(Arrays.asList(
             new Integer[] {10, 11, 51, 81}
    ));
    // check if a particular spot consists of 2 breathable blocks over something relatively solid
    private boolean isSafeSpot(World world, int X, int Y, int Z)
    {
            Integer below = (Integer)world.getBlockTypeIdAt(X, Y - 1, Z);
            return (safeOpenBlocks.contains((Integer)world.getBlockTypeIdAt(X, Y, Z))		// target block open and safe
                     && safeOpenBlocks.contains((Integer)world.getBlockTypeIdAt(X, Y + 1, Z))	// above target block open and safe
                     && (!safeOpenBlocks.contains(below) || below == 8 || below == 9)			// below target block not open and safe (probably solid), or is water
                     && !painfulBlocks.contains(below)											// below target block not painful
                    );
    }
    public boolean TeleportPlayer(HungerPlayer hp, Player p, Location l, String reason)
    {
        Player player = plugin.getServer().getPlayer(p.getName());
        //we need to save the players 
        if(player == null){
            return false;
        }
        hp.setTeleporting(2);
        player.teleport(l);
        
        HungerGames.log(reason + " Player Teleported:" + l.getWorld().getName() + " ("+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")");
        return true;
    }
    
    private boolean TeleportEnteringPlayer(HungerPlayer hp, Player p){
        //Start at the center(center_x, center_z)
        //see how close the closest player in the world is
        
        //Start at center
        int x_loc = center_x;
        int z_loc = center_z;
        //Move in 5 block increments
        int iMoveTestDistance = 5;
        int iMoveDirection = 0;
        int iMoveMultiplier = 1;
        
        while(true)
        {
            //Look for the first safe location starting at the ground
            double y_loc = getSafeY(x_loc, 63, z_loc);
            boolean is_wg_spectator_region = isWorldGuardSpectatorLocation(x_loc, y_loc, z_loc);
            
            if(!PlayerWithinRadiusOfPoint(x_loc, z_loc) && (y_loc > 0) && !is_wg_spectator_region)
            {
                //Teleport the player to x_loc, z_loc
                Location new_loc = new Location(world, x_loc, y_loc, z_loc);
                //This can return false when the player doesn't exist
                return TeleportPlayer(hp, p, new_loc, "ENTERING");
            }

            if(iMoveDirection > 8)
            {
                iMoveDirection = 0;
                iMoveMultiplier += 1;
            }
            //Plan our next move since that one didn't work out
            int iDistance = iMoveTestDistance * iMoveMultiplier;
            
            //Start at top(0) and move clockwise
            switch(iMoveDirection){
                case 0:                        
                    //Top Middle
                    //+, 0
                    x_loc = center_x + iDistance;
                    z_loc = center_z;
                    break;
                case 1:
                    //Top Right
                    //+, +
                    x_loc = center_x + iDistance;
                    z_loc = center_z + iDistance;
                    break;
                case 2:
                    //Middle Right
                    //0, +
                    x_loc = center_x;
                    z_loc = center_z + iDistance;
                    break;
                case 3:
                    //Bottom, right
                    //-, +
                    x_loc = center_x - iDistance;
                    z_loc = center_z + iDistance;
                    break;
                case 4:
                    //Bottom, middle
                    //-, 0
                    x_loc = center_x - iDistance;
                    z_loc = center_z;
                    break;
                case 5:
                    //Bottom, left
                    //-,-
                    x_loc = center_x - iDistance;
                    z_loc = center_z - iDistance;
                    break;                        
                case 6:
                    //middle, left
                    //0,-
                    x_loc = center_x;
                    z_loc = center_z - iDistance;
                    break;
                case 7:
                    //top, left
                    //+,-
                    x_loc = center_x + iDistance;
                    z_loc = center_z - iDistance;
                    break;                        
            }
            iMoveDirection += 1;
                        
                
            
            //Check if we've moved out of bounds
            if (iDistance >= radius){
                break;
            }
        
        }
        return false;
    }
    
    private boolean PlayerWithinRadiusOfPoint(int x_loc, int z_loc){
        for(Player p : world.getPlayers()){
            //
            double dDist = Helper.getDistance(p.getLocation(), x_loc, z_loc);
            if(dDist < tp_player_distance)
            {
                plugin.log(" Player " + p.getName() + "was within radius: " + tp_player_distance + " of location " + x_loc + " " + z_loc);
                return true;
            }
            
        }
        return false;
    }

    
    public int getPlayerMoveDistance(){
        return player_move_distance;
    }
            
    public boolean isWorldGuardSpectatorLocation(int x, double y, int z){
        boolean ret = false;
        if((spectator_y > 0) && (spectator_y2 > 0)){
            return ((x >= spectator_x) && (x <= spectator_x2) && (y >= spectator_y) && (y <= spectator_y2) && (z >= spectator_z) && (z <= spectator_z2));
        }
        return ret;
    }
    
    
    //Set a player as a spectator for the games.  
    //They can't enter/enroll
    public void Spectate(Player player){
        //Make sure they player is enrolled
        HungerPlayer hp = findHungerPlayer(player.getName());
        if(hp == null){
            hp = new HungerPlayer(this, player);
            //Do actual mysql insert here
            plugin.getStorageManager().insertHungerPlayer(hp);
            playerList.add(hp);
        }
        
        String sOpenMessage = OpenForMessage(false);
        //Check that the games are open for entering
        if(!sOpenMessage.equals((""))){
            ChatBlock.sendMessage(player, getDisplayName() + sm.CCDefaultText + " You may not spectate. " + sOpenMessage);
            return;
        }
        
        if(hp.Enrolled() && !hp.HasDied()){
            ChatBlock.sendMessage(player, getDisplayName() + " You may not spectate.  You are enrolled");
            return;
        }
        
        if(spectator_y <= 0){
            ChatBlock.sendMessage(player, getDisplayName() + " No Spectator Location Set!");
            return;
        }
        
        if(getWorld() == null){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " World Does Not Exist!  Please see Administrator.");
            return;
        }
        
        Location loc = null;
        //If they're already spectating, move them back
        if(hp.Spectating()){
            hp.SetSpectating(false);
            loc = hp.getStartingLocation();
            String sMsg = " Thank you for spectating the hunger games.  You have used: ";
            sMsg += sm.CCParamValue + Integer.toString(hp.getSpectateTPCount());
            
            if(spectator_tp_limit > 0){
                sMsg += "/" + Integer.toString(spectator_tp_limit);
            }
            sMsg += sm.CCDefaultText + " spectator teleports";
            ChatBlock.sendMessage(player, getDisplayName() + sMsg);
        }
        else if((spectator_tp_limit > 0) && (hp.getSpectateTPCount() >= spectator_tp_limit)){
            ChatBlock.sendMessage(player, getDisplayName() + ChatColor.RED + " You have reached your spectator TP limit of " + spectator_tp_limit);
            return;
        }
        else{            
            //Set the players starting location to where they are now.
            hp.incrementSpectateTPCount();
            hp.SetStartingLocation(player.getLocation());
            hp.SetSpectating(true);
            
            //If we're using two spots, use the bottom block and 
            loc = getSpectatorLocation();
            
            String sMsg = " You are now spectating the hunger games.  You have used: ";
            sMsg += sm.CCParamValue + Integer.toString(hp.getSpectateTPCount());
            if(spectator_tp_limit > 0){
                sMsg += "/" + Integer.toString(spectator_tp_limit);
            }
            sMsg += sm.CCDefaultText + " spectator teleports";
            ChatBlock.sendMessage(player, getDisplayName() + sMsg);

        }
        TeleportPlayer(hp, player, loc, "SPECTATE");
        plugin.getStorageManager().updateHungerPlayer(hp);
        HungerGames.log(getDisplayName() + " Player is Spectating Games: " + player.getName());
    }
    
    
    private Location getSpectatorLocation(){
        if(spectator_y2 > 0){
            return new Location(world, (spectator_x + spectator_x2) / 2, spectator_y + 0.5, (spectator_z + spectator_z2)/2);
        }
        else{
            return new Location(world, spectator_x ,spectator_y  + 0.5, spectator_z );
        }
    }
    
    //Send coords for all players in the game.
    //Get them from HungerPlayer
    public void Coords(CommandSender sender){
        ChatBlock.sendMessage(sender, getDisplayName() + sm.CCDefaultText + " No Coords Available Yet");
    }
    
    /*
     * Admin Commands
     */
    
    public void ClearPlayers(CommandSender sender){
        for(HungerPlayer hp : playerList){
            plugin.getStorageManager().deleteHungerPlayer(hp.getName());
        }
        playerList.clear();
        ChatBlock.sendMessage(sender, getDisplayName() + ChatColor.AQUA + " All Players Cleared");
        HungerGames.log(getDisplayName() + " All Players Cleared by: " + sender.getName());
    }
    //Administrate a spectator
    public void AdminPlayerSpectate(CommandSender sender, String[] args){
        if(args.length ==0){
            ChatBlock.sendMessage(sender, getDisplayName() + " You must Enter a player name!" );
            return;
        }
        String pName = args[0];
        HungerPlayer hp = findHungerPlayer(pName);
        if(hp == null){
            ChatBlock.sendMessage(sender, getDisplayName() + " Can not find player: " + pName );
            return;
        }

        //Make sure the player exists
        Player player = plugin.getServer().getPlayer(pName);
        if(player == null){
            ChatBlock.sendMessage(sender, getDisplayName() + "Player " + pName + " Is not online");
            return;
        }
        
        Location loc = null;
        //If they're already spectating, move them back
        if(hp.Spectating()){
            hp.SetSpectating(false);
            loc = hp.getStartingLocation();
            String sMsg = " Thank you for spectating the hunger games.  You have used: ";
            sMsg += sm.CCParamValue + Integer.toString(hp.getSpectateTPCount());
            
            if(spectator_tp_limit > 0){
                sMsg += "/" + Integer.toString(spectator_tp_limit);
            }
            sMsg += sm.CCDefaultText + " spectator teleports";
            ChatBlock.sendMessage(player, getDisplayName() + sMsg);
        }
        else{            
            //Set the players starting location to where they are now.
            hp.incrementSpectateTPCount();
            hp.SetStartingLocation(player.getLocation());
            hp.SetSpectating(true);
            
            loc = getSpectatorLocation();
            String sMsg = " You are now spectating the hunger games.  You have used: ";
            sMsg += sm.CCParamValue + Integer.toString(hp.getSpectateTPCount());
            if(spectator_tp_limit > 0){
                sMsg += "/" + Integer.toString(spectator_tp_limit);
            }
            sMsg += sm.CCDefaultText + " spectator teleports";
            ChatBlock.sendMessage(player, getDisplayName() + sMsg);

        }
        TeleportPlayer(hp, player, loc, "ADMIN SPECTATE BY " + sender.getName());
        HungerGames.log(getDisplayName() + " Player " + player.getName() + " set to spectate by: " + sender.getName());
    }

    
    //Administrate a player who is in the games
    public void AdminPlayerEnroll(CommandSender sender, String[] args){
        if(args.length ==0){
            ChatBlock.sendMessage(sender, getDisplayName() + " You must Enter a player name!" );
            return;
        }
        String pName = args[0];
        HungerPlayer hp = findHungerPlayer(pName);
        if(hp == null){
            ChatBlock.sendMessage(sender, getDisplayName() + " Can not find player: " + pName );
            return;
        }
        //Make sure the player exists
        Player player = plugin.getServer().getPlayer(pName);

        //make sure they player isn't already in the games
        if((player != null)&&(player.getLocation().getWorld().getName().equals(world_name))){
            ChatBlock.sendMessage(sender, getDisplayName() + "Player " + pName + " Already in the selected world!");
            return;
        }
        
        hp.SetDied(false);
        hp.SetEnrolled(true);
        
        plugin.getStorageManager().updateHungerPlayer(hp);
        ChatBlock.sendMessage(player, getDisplayName() + " You are now Enrolled in these games.  You should '/hg enter' once the games have started.");
        
        //Announce that the place has enrolled in the games
        ChatColor defaultC = plugin.getSettingsManager().CCDefaultText;
        plugin.AnnounceAll(player.getDisplayName() + " has " + ChatColor.GOLD + "enrolled " + defaultC + "in the " + getDisplayName() + " games.");
        HungerGames.log(getDisplayName() + " Player " + player.getName() + " enrolled by: " + sender.getName());
    }
    
    //Administrate a player who is in the games
    public void AdminPlayerEnter(CommandSender sender, String[] args){
        if(args.length ==0){
            ChatBlock.sendMessage(sender, getDisplayName() + " You must Enter a player name!" );
            return;
        }
        String pName = args[0];
        HungerPlayer hp = findHungerPlayer(pName);
        if(hp == null){
            ChatBlock.sendMessage(sender, getDisplayName() + " Can not find player: " + pName );
            return;
        }
        //Make sure the player exists
        Player player = plugin.getServer().getPlayer(pName);

        //make sure they player isn't already in the games
        if((player == null)){
            ChatBlock.sendMessage(sender, getDisplayName() + "Player Not Online!");
            return;
        }
        
        //Tell the admin that the player already was in and died
        if(hp.HasDied()){
            ChatBlock.sendMessage(sender, getDisplayName() + ChatColor.RED + " Player: " + pName + " already played and died. Ignoring this error!");
        }

        String sOpenMessage = OpenForMessage(false);
        //Check that the games are open for entering
        if(!sOpenMessage.equals((""))){
            ChatBlock.sendMessage(sender, getDisplayName() + " Player may not enter. " + sOpenMessage);
            return;
        }
        //We're just notifying the sender of the errors
        if(hp.Entered()){
            ChatBlock.sendMessage(sender, getDisplayName() + ChatColor.RED + " Player: " + pName + " already entered. Ignoring this error!");
        }
        
        if(getWorld() == null){
            ChatBlock.sendMessage(sender, getDisplayName() + ChatColor.RED + " World Does Not Exist!  Please see Administrator.");
            return;
        }

        //We're just notifying the sender of the errors
        if((last_entry_time != null)&&(last_entry_time.before(new Date()))){
            ChatBlock.sendMessage(sender, getDisplayName() + " Last Entry Time Expired.  Ignoring this error.");
        }
        //Set the players starting location to where they are now.
        Location playerStartingLocation = player.getLocation();
        if(TeleportEnteringPlayer(hp,player)){
            if(hp.Spectating() && (hp.getStartingLocation() != null)){
                //If they were a spectator, transfer their starting location to their original
                playerStartingLocation = hp.getStartingLocation();
            }
            
            hp.SetStartingLocation(playerStartingLocation);
            hp.SetEntered(true);
            hp.SetSpectating(false);
            hp.SetDied(false);
            
            //Clear their inventory
            PlayerInventory pi = player.getInventory();            
            pi.setBoots(null);
            pi.setChestplate(null);
            pi.setHelmet(null);
            pi.setLeggings(null);
            
            pi.clear();
            
            ChatBlock.sendMessage(player, getDisplayName() + " Welcome to the Games!  Good luck!  You will be returned to your last position when you die.");
        }
        else{
            ChatBlock.sendMessage(player, ChatColor.RED + "ERROR: " + getDisplayName() + " Could not Enter you into the games. There might not be any safe space available to teleport you.");
        }
        plugin.getStorageManager().updateHungerPlayer(hp);
        ChatColor defaultC = plugin.getSettingsManager().CCDefaultText;
        plugin.AnnounceEnrolled(player.getDisplayName() + " has " + ChatColor.GOLD + "entered " + defaultC + "the " + getDisplayName() + defaultC + " games.");
        
        HungerGames.log(getDisplayName() + " Player " + player.getName() + " Admin entered by: " + sender.getName());
    }
    
    //Administrate a player who is in the games
    public void AdminPlayerRemove(CommandSender sender, String[] args){
        if(args.length ==0){
            ChatBlock.sendMessage(sender, getDisplayName() + " You must Enter a player name!" );
            return;
        }
        String pName = args[0];
        HungerPlayer hp = findHungerPlayer(pName);
        if(hp == null){
            ChatBlock.sendMessage(sender, getDisplayName() + " Can not find player: " + pName );
            return;
        }
        plugin.getStorageManager().deleteHungerPlayer(pName);
        playerList.remove(hp);
        ChatBlock.sendMessage(sender, getDisplayName() + " Admin Removed Player: " + pName );
        HungerGames.log(getDisplayName() + " Admin Removed Player: " + pName );
    }
    
    //Force the start of a game right now.
    public void Start(CommandSender sender){
        start_time = new Date();
        plugin.AnnounceAll(getDisplayName() + " Hunger Games have started!");
        plugin.getStorageManager().updateHungerWorld(this);
    }
    //Force the a game to stop.
    //TP all players back to world
    public void Stop(CommandSender sender){
        plugin.StopGame(this);
        end_time = new Date();
        plugin.AnnounceAll(getDisplayName() + " Hunger Games have ended!");
        plugin.getStorageManager().updateHungerWorld(this);
    }
    
    
    public void SetParameter(CommandSender sender, String[] args){

        //Check our inputs
        if(args.length == 0){
            plugin.getCommandManager().SendHelpMessage(sender, "set");
            return;
        }
        else{
            String value = "";
            String param = args[0].toUpperCase();
            if((args.length == 1) && !(param.contains("SPECTATOR"))){
                //Not enough parameters.  Get help for just this one
                plugin.getCommandManager().SendHelpMessage(sender, "set " + param);
                return;
            }
            else if(!(param.contains("SPECTATOR"))){
                value = args[1];
            }
                try
                {
                    
                    if(param.equals("RADIUS")){
                        radius = Integer.parseInt(value);
                        original_radius = radius;
                        ChatBlock.sendMessage(sender, getDisplayName() + sm.CCDefaultText + " Radius Set to " + value);
                        if(wb_plugin != null){
                            setBorders();
                       }
                        else
                        {
                            ChatBlock.sendMessage(sender, getDisplayName() + ChatColor.RED + " ERROR!  NO WORLDBORDER PLUGIN!");
                        }
                        
                    }
                    else if(param.equals("PLAYER_MOVE_DISTANCE")){
                        player_move_distance = Integer.parseInt(value);
                        ChatBlock.sendMessage(sender, getDisplayName() + sm.CCDefaultText + " Player Force Move Distance Set to " + value);                        
                    }
                            
                    else if(param.equals("CENTER")){
                        //Must have another value parameter
                        if(args.length != 3){
                            plugin.getCommandManager().SendHelpMessage(sender, "set center");
                            return;
                        }
                        else{
                            center_x = Integer.parseInt(value);
                            center_z = Integer.parseInt(args[2]);
                            
                            setBorders();
                            ChatBlock.sendMessage(sender, getDisplayName() + " Center Set to " + sm.CCDefaultText +  value + " " + args[2]);    
                        }
                    }
                    else if(param.equals("WG_SPECTATOR")){
                        Player player = null;
                        if(!(sender instanceof Player) ){
                            
                            ChatBlock.sendMessage(sender, getDisplayName() + " Must be a player for this command.");    
                        }
                        else if(we_plugin == null){
                            ChatBlock.sendMessage(sender, getDisplayName() + " Can't find WorldEdit Plugin.");    
                        }
                        else{
                            player = (Player)sender;
                            
                            Selection se = we_plugin.getSelection(player);
                            if(se.getArea() < 1){
                                ChatBlock.sendMessage(sender, getDisplayName() + " Area of region selected is < 1.");    
                            }
                            else{
                                //We have an area we can set
                                Location l = se.getMinimumPoint();
                                Location l2 = se.getMaximumPoint();
                                spectator_x = l.getBlockX();
                                spectator_y = l.getBlockY();
                                spectator_z = l.getBlockZ();
                                
                                spectator_x2 = l2.getBlockX();
                                spectator_y2 = l2.getBlockY();
                                spectator_z2 = l2.getBlockZ();
                                if(wg_plugin != null){
                                    RegionManager rm = wg_plugin.getRegionManager(world);
                                    ProtectedRegion r = null;
                                    if(rm.hasRegion(world_name + "_spectator")){
                                        r = rm.getRegion(world_name + "_spectator");
                                        BlockVector min = r.getMinimumPoint();
                                        min.setX(spectator_x);
                                        min.setY(spectator_y);
                                        min.setZ(spectator_z);
                                        
                                        BlockVector max = r.getMaximumPoint();
                                        max.setX(spectator_x2);
                                        max.setY(spectator_y2);
                                        max.setZ(spectator_z2);
                                        
                                        ChatBlock.sendMessage(sender, getDisplayName() + " Redefined WorldGuard Region: " + world_name + "_spectator");
                                    }
                                    else{
                                        
                                        r = new ProtectedCuboidRegion(world_name + "_spectator", new BlockVector(spectator_x, spectator_y, spectator_z), new BlockVector(spectator_x2, spectator_y2, spectator_z2));
                                        rm.addRegion(r);
                                        ChatBlock.sendMessage(sender, getDisplayName() + " Defined WorldGuard Region: " + world_name + "_spectator");
                                    }
 /*                                   StateFlag sf = DefaultFlag.PVP;
                                    
                                    r.setFlag((T)sf, (V)false);*/
                                    //do we need this?
                                    rm.save();
                                    
                                }
                                
                                ChatBlock.sendMessage(sender, getDisplayName() + " Spectator Region set to your selection.");
                            }
                        }
                    }        
                    else if(param.equals("SPECTATOR")){
                        //Must have another value parameter
                        if((args.length != 4)&&(args.length != 1)){
                            plugin.getCommandManager().SendHelpMessage(sender, "set set_spectator");
                            return;
                        }
                        else{
                            if(args.length == 4){
                                spectator_x = Integer.parseInt(value);
                                spectator_y = Integer.parseInt(args[2]);
                                spectator_z = Integer.parseInt(args[3]);
                            }
                            else if(!(sender instanceof Player))
                            {
                                ChatBlock.sendMessage(sender, getDisplayName() + " Must be player to use this option.");
                            }
                            else{
                                Location l = ((Player) sender).getLocation();
                                spectator_x = l.getBlockX();
                                spectator_y = l.getBlockY();
                                spectator_z = l.getBlockZ();
                                
                            }
                            ChatBlock.sendMessage(sender, getDisplayName() + " Spectator Location Set to " + sm.CCDefaultText +  spectator_x + " " + spectator_y + " " + spectator_z );
                        }
                    }
                    else if(param.equals("SPECTATOR_2")){
                        //Must have another value parameter
                        if((args.length != 4)&&(args.length != 1)){
                            plugin.getCommandManager().SendHelpMessage(sender, "set set_spectator");
                            return;
                        }
                        else{
                            if(args.length == 4){
                                spectator_x2 = Integer.parseInt(value);
                                spectator_y2 = Integer.parseInt(args[2]);
                                spectator_z2 = Integer.parseInt(args[3]);
                            }
                            else if(!(sender instanceof Player))
                            {
                                ChatBlock.sendMessage(sender, getDisplayName() + " Must be player to use this option.");
                            }
                            else{
                                Location l = ((Player) sender).getLocation();
                                spectator_x2 = l.getBlockX();
                                spectator_y2 = l.getBlockY();
                                spectator_z2 = l.getBlockZ();
                                
                            }
                            ChatBlock.sendMessage(sender, getDisplayName() + " Spectator Location 2 Set to " + sm.CCDefaultText +  spectator_x2 + " " + spectator_y2 + " " + spectator_z2);
                        }
                    }
                    else if(param.equals("MINIMUM_RADIUS")){
                        
                        if(Integer.parseInt(value) <= 0){
                            ChatBlock.sendMessage(sender, getDisplayName() + " Minimum Radius Must be Greater than Zero");
                        }
                        else if(Integer.parseInt(value) >= radius){
                            ChatBlock.sendMessage(sender,  getDisplayName() + " Minimum Radius Must be Less than Current Radius (" + sm.CCParamValue + radius + sm.CCDefaultText + ")");
                        }
                        else{
                            minimum_radius = Integer.parseInt(value);
                            ChatBlock.sendMessage(sender, getDisplayName() + " Minimum Radius Set to " + sm.CCDefaultText + value);
                        }
                    }
                    else if(param.equals("SHRINK_RATE")){
                        String errorMsg;
                        //NEED TO PARSE THIS VALUE TO MAKE SURE IT'LL WORK
                        errorMsg = ParseShrinkRate(args);
                        if(errorMsg.equals("")){
                            ChatBlock.sendMessage(sender, getDisplayName() + " Shrink Rate Set to " + getShrinkRateString());    
                        }
                        else{
                            ChatBlock.sendMessage(sender, getDisplayName() + " Error Setting Shrink Rate: " + errorMsg);
                        }
                        
                    }
                    else if(param.equals("END_TIME")){
                        
                        try{
                            Date temp_time = (Date) formatter.parse(value + " " + args[2]);
                            if((start_time != null) && ((temp_time.before(start_time)) || (temp_time.equals(start_time)))){
                                ChatBlock.sendMessage(sender, getDisplayName() + " Games End Must be set after the start of " + getStartTimeString());
                            }
                            else{
                                end_time = temp_time;
                                ChatBlock.sendMessage(sender,  getDisplayName() + " Games End Set to " + getEndTimeString());
                            }
                            //If we bumped the end of the games down before the shrink_start time.
                            //Then set the shrink_start time to our end_time
                            if((shrink_start != null) && end_time.before(shrink_start)){
                                shrink_start = end_time;
                                ChatBlock.sendMessage(sender, "Notice:" +  getDisplayName() + " Shrink Start ALSO Set to " + getShrinkStartString());
                            }
                            
                        }
                        catch(Exception ex){
                            ChatBlock.sendMessage(sender,  getDisplayName() + " Invalid Format. Use: MM-dd-yyyy HH:mm");
                        }
                    }
                    else if(param.equals("START_TIME")){
                        
                        try{
                            Date temp_time = (Date) formatter.parse(value + " " + args[2]);
                            if((end_time != null) && ((temp_time.after(end_time)) || (temp_time.equals(end_time)))){
                                ChatBlock.sendMessage(sender, getDisplayName() + " Games Must have Start time BEFORE End Time of " + getEndTimeString());
                            }
                            else{
                                start_time = temp_time;
                                ChatBlock.sendMessage(sender, getDisplayName() + " Games Start Set to " + getStartTimeString());
                            }
                            if((shrink_start != null) && start_time.after(shrink_start)){
                                shrink_start = start_time;
                                ChatBlock.sendMessage(sender, "Notice:" +  getDisplayName() + " Shrink Start ALSO Set to " + getShrinkStartString());
                            }
                            
                        }
                        catch(Exception ex){
                            ChatBlock.sendMessage(sender,  getDisplayName() + " Invalid Format. Use: MM-dd-yyyy HH:mm" + ex.getMessage());
                        }
                    }
                    else if(param.equals("LAST_ENTRY_TIME")){
                        
                        try{
                            Date temp_time = (Date) formatter.parse(value + " " + args[2]);
                            if((start_time != null) && ((temp_time.before(start_time)) || (temp_time.equals(start_time)))){
                                ChatBlock.sendMessage(sender,  getDisplayName() + " Last Entry Time must be after the start at " + getStartTimeString());
                            }
                            else{
                                last_entry_time = temp_time;
                                ChatBlock.sendMessage(sender,  getDisplayName() + " Games Last Entry Time Set to " + getLastEntryTimeString());
                            }
                            
                            
                        }
                        catch(Exception ex){
                            ChatBlock.sendMessage(sender, getDisplayName() + " Invalid Format. Use: MM-dd-yyyy HH:mm");
                        }
                    }
                    else if(param.equals("SHRINK_START")){
                      
                        try{
                            Date temp_time = (Date) formatter.parse(value + " " + args[2]);
                            
                            //Shrink is allowed to be equals
                            if((end_time != null) && (temp_time.after(end_time))) {
                                ChatBlock.sendMessage(sender, getDisplayName() + " Shrink must begin BEFORE End Time of " + getEndTimeString());
                            }
                            else if((start_time != null) && (temp_time.before(start_time))){
                                ChatBlock.sendMessage(sender, getDisplayName() + " Shrink must be AFTER Start Time of " + getShrinkStartString());
                            }
                            else
                            {
                                shrink_start = temp_time;
                                ChatBlock.sendMessage(sender, getDisplayName() + " Games Shrink Start Set to " + getShrinkStartString());
                            }
                        }
                        catch(Exception ex){
                            ChatBlock.sendMessage(sender, getDisplayName() + " Invalid Format. Use: MM-dd-yyyy HH:mm");
                        }
                    }
                    else if(param.equals("MAX_PLAYERS")){
                        max_players = Integer.parseInt(value);
                        ChatBlock.sendMessage(sender, getDisplayName() + " Max # Players Set to " + value);    
                    }
                    else if(param.equals("TP_MIN_DISTANCE")){
                        tp_player_distance = Integer.parseInt(value);
                        ChatBlock.sendMessage(sender, getDisplayName() + " TP Minimum Distance Set to " + value);    
                    }
                    else if(param.equals("SPECTATOR_TP_LIMIT")){
                        spectator_tp_limit = Integer.parseInt(value);
                        ChatBlock.sendMessage(sender, getDisplayName() + " Spectator TP Limit Set to " + value);    
                    }
                    else{
                        //Wrong parameter, send the help message for SET
                        plugin.getCommandManager().SendHelpMessage(sender, "set");
                        return;
                    }
                   
                }
                catch(Exception e){
                    ChatBlock.sendMessage(sender, getDisplayName() + " Error Setting Parameter: " + sm.CCParamName + param + " : " + sm.CCDefaultText + e.toString());
                    HungerGames.log(getDisplayName() + " Error Setting Parameter: " + sm.CCParamName + param + " : " + e.toString());
                    return;
                }

            }
            //If we made it this far, a setting for the world changed.
            //Let's do an SQL Update
            plugin.getStorageManager().updateHungerWorld(this);
            
        
        
            
        }
    
    //Return the HungerPlayer from our list with the given name
    public HungerPlayer findHungerPlayer(String name){
        for(HungerPlayer p : playerList){
            if(p.getName().equals(name)){
                return p;
            }
        }
        return null;
    }
    
    public int getCenterX() {
        return center_x;
    }

    public int getCenterZ() {
        return center_z;
    }

    public String getStartTimeString() {
        if(start_time == null){return "";}
        return sm.CCParamValue + formatter.format(start_time);
    }
    public Date getStartTime() {
        return start_time;
    }
    
    public String getLastEntryTimeString(){
        if(last_entry_time == null){return "";}
        return sm.CCParamValue + formatter.format(last_entry_time);
    }
    public Date getLastEntryTime(){
        return last_entry_time;
    }
    
    
    public Date getEndTime() {
        return end_time;
    }
    public String getEndTimeString() {
        if(end_time == null){return "";}
        return sm.CCParamValue + formatter.format(end_time);
    }
    public Date getShrinkStart() {
        
        return shrink_start;
    }
    public String getShrinkStartString() {
        if(shrink_start == null){return "";}
        return sm.CCParamValue + formatter.format(shrink_start);
    }

    public int getSpectatorX() {
        return spectator_x;
    }
    public int getSpectatorY() {
        return spectator_y;
    }
    public int getSpectatorZ() {
        return spectator_z;
    }
    public int getSpectatorX2() {
        return spectator_x2;
    }
    public int getSpectatorY2() {
        return spectator_y2;
    }
    public int getSpectatorZ2() {
        return spectator_z2;
    }
    public int getMaxPlayers() {
        return max_players;
    }

    public int getMinimumRadius() {
        return minimum_radius;
    }


    public int getRadius() {
        return radius;
    }
    
    public int getOriginalRadius() {
        return original_radius;
    }

    public String getShrinkRateString() {
        return shrink_rate + " per " + shrink_rate_interval;
    }
    
    public int getShrinkRate(){
        return shrink_rate;
    }

    public String getShrinkRateInterval(){
        return shrink_rate_interval;
    }
    
    
    /*
     * Shrink Rate is in the form of:
     * RADIUS per TIME
     * Radius is a number
     * Time is either h(our) m(inute) d(ay)
     */
    public String ParseShrinkRate(String[] args){
        String ret = "";
        String err = "Shrink Rate is in the form of: RADIUS per TIME.  RADIUS = #, Time = (m/h/d)";
        int iTempRate;
        String sTempInterval;
        //Args start at 1
        if(args.length < 4){
            //Just set the message to 
            ret = err;
        }
        else {
            try
            {
                iTempRate = Integer.parseInt(args[1]);
                if(iTempRate <= 0){
                    return "Rate must be a number > 0";    
                }
            }
            catch(Exception ex){
                return "Rate must be a number > 0";
            }
            if(!args[2].toLowerCase().equals("per")){
                return "Second word MUST be per(100 per hour)";
            }
            //If it starts with 
            //h for hour
            //m for minute
            //d for day
            if(args[3].toLowerCase().startsWith("h")){
                sTempInterval = "hour";
            }
            else if(args[3].toLowerCase().startsWith("m")){
                sTempInterval = "minute";
            }
            else if(args[3].toLowerCase().startsWith("d")){
                sTempInterval = "day";
            }
            else
            {
                return "Interval Must be Hour/Minute/Day";
            }
            shrink_rate = iTempRate;
            shrink_rate_interval = sTempInterval;
        }
        
        return ret;
    }

    public int getSpectatorTpLimit() {
        return spectator_tp_limit;
    }


    public int getTpPlayerDistance() {
        return tp_player_distance;
    }

}
