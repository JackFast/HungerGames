/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nilla.hungergames.listeners;


import com.nilla.hungergames.ChatBlock;
import com.nilla.hungergames.HungerGames;
import com.nilla.hungergames.HungerPlayer;
import com.nilla.hungergames.HungerWorld;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;

import java.util.Date;
import org.bukkit.Location;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 *
 * @author telaeris
 */
public class HGPlayerListener extends PlayerListener
{
    private final HungerGames plugin;
    public HGPlayerListener()
    {
        plugin = HungerGames.getInstance();
    }
    
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(event.isCancelled()){
            return;
        }
        Player player = event.getPlayer();
        
        if(player == null){
            return;
        }
        for(HungerWorld hw : plugin.getHungerWorlds())
        {
            for(HungerPlayer hp : hw.getPlayers())
            {
                //If our player is spectating and trying to drop shit. 
                if(hp.Spectating() && player.getName().equals(hp.getName()))
                {
                    event.setCancelled(true);
                    ChatBlock.sendMessage(player, hw.getDisplayName() + ":" +  ChatColor.RED + " You may NOT Drop anything while spectating!");
                    HungerGames.log(player.getName() + " Tried to drop items while spectating! " + event.getItemDrop().toString());
                }
            }
        }
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        
        if(player == null){
            return;
        }

        //Check and see if the player was entrolled in the hunger games
        //If so, TP them back to their original location
        //Mark them as having died
        for(HungerWorld hw : plugin.getHungerWorlds())
        {
            for(HungerPlayer hp : hw.getPlayers())
            {
                if(player.getName().equals(hp.getName())){
                    if(hp.HasDied() || !hp.Entered()){
                        return;
                    }
                    hp.SetDied(true);
                    
                    event.setRespawnLocation(hp.getStartingLocation());
                    plugin.getStorageManager().updateHungerPlayer(hp);
                    ChatColor defaultC = plugin.getSettingsManager().CCDefaultText;
                    plugin.AnnounceAll(defaultC + player.getDisplayName() + " has " + ChatColor.RED + "died " + defaultC + "in the " + hw.getDisplayName() + defaultC + " games.");
                    ChatBlock.sendMessage(player, hw.getDisplayName() + ":" +  ChatColor.RED + " You have died.  Thank you for playing!");
                    return;
                }
                
            }
            
        }
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        if(event.isCancelled())
        {
            return;
        }
/*        
         * This is bugged for now :(
         * Player player = event.getPlayer();
        for(HungerWorld hw : plugin.getHungerWorlds())
        {
            boolean tp_flag = false;
            //this is so we can clear the flag.  not the most effecient code
            HungerPlayer hp = hw.findHungerPlayer(player.getName());
            if(hp != null){
                int iTPCT = hp.getTeleporting();
                tp_flag = (iTPCT > 0);
                hp.setTeleporting(iTPCT - 1);
            }
            if(event.getTo().getWorld().getName().equals(hw.getName())){
                if(!plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.teleport")){
                    
                    if(!tp_flag){
                        event.setCancelled(true);
                        ChatBlock.sendMessage(player, hw.getDisplayName() +  " You may NOT TP to a hunger games without the 'hungergames.admin.teleport' permissions node" );
                        HungerGames.log(player.getName() + " Tried to tp to hunger game" + hw.getName() + " and was rejected");
                    }
                }
            }
        }
         * 
         */
    }
    /**
     * @param event
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        //Show players who are in the games the game info and # remaining players
        Player player = event.getPlayer();
        
        //If a game is running and the player is NOT in the games
        //Tell them they can spectate(and give them command)
        for(HungerWorld hw : plugin.getHungerWorlds())
        {
            if(hw.IsRunning()){
                //Send the player a message about the game
                HungerPlayer hp = hw.findHungerPlayer(player.getName());
                if((hp != null) && (!hp.HasDied())){
                    if(hp.Entered()){
                        ChatBlock.sendMessage(player, hw.getDisplayName() + ":" +  plugin.getSettingsManager().CCDefaultText + " You are currently in the Hunger Games!");
                    }
                    else if(hp.Enrolled()){
                        Date last_entry = hw.getLastEntryTime();
                        if(last_entry != null){
                            ChatBlock.sendMessage(player, hw.getDisplayName() + ":" +  plugin.getSettingsManager().CCDefaultText + " Last Chance to enter games is: " +  plugin.getSettingsManager().CCParamValue + hw.getLastEntryTimeString());
                        }
                        
                    }
                    if(hw.getLastEntryTime() != null &&  hw.getLastEntryTime().before(new Date())){
                        //Players who are enrolled after the last entry time should be unenrolled
                        if(hp.Enrolled() && !hp.Entered() && !hp.Entered()){
                            hp.SetEnrolled(false);
                            plugin.getStorageManager().updateHungerPlayer(hp);
                            if(player != null){
                                ChatBlock.sendMessage(player, hw.getDisplayName() + " You signed up for the hunger games but the entry window is expired.  You may now " + plugin.getSettingsManager().CCParamValue + "/hg enter " + hw.getName());
                            }
                            HungerGames.log(hw.getDisplayName() + ": Player " + hp.getName() + " was unenrolled after the last entry time.");
                        }
                    }
                    //If the player is dead or has 
                    if(!hp.HasDied() && hp.Entered())
                    {
                        if((player != null) && (player.getLocation() != null))
                        {
                            Location l = player.getLocation();
                            //Check if th player is in the correct world
                            if(!l.getWorld().getName().equals(hw.getName()))
                            {
                                hp.SetDied(true);
                                plugin.getStorageManager().updateHungerPlayer(hp);
                                ChatBlock.sendMessage(player, hw.getDisplayName() + " You previously died in HG and weren't marked as dead properly.  Doing that now.  Thanks for playing!");
                                HungerGames.log(hw.getDisplayName() + ": Player " + player.getName() + " is in the wrong world(" + l.getWorld().getName() + ".  Marking them dead");
                            }
                        }
                    }
                }
            }
        }
        
    }
    
}