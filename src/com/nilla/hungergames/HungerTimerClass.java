/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nilla.hungergames;

import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author telaeris
 */
public class HungerTimerClass implements Runnable
{
	private transient HungerGames plugin = null;
        private CommandSender cs;

	public HungerTimerClass(HungerGames instance)
	{
		this.plugin = instance;
                cs = plugin.getServer().getConsoleSender();
	}

	public void run()
	{
//		long startTime = Config.Now();  // for monitoring plugin efficiency
		if (plugin == null)
		{
//			Config.timeUsed += Config.Now() - startTime;  // for monitoring plugin efficiency
			return;
		}

                
                for(HungerWorld hw : plugin.getHungerWorlds()){
                    Date start = hw.getStartTime(); 
                    Date shrink = hw.getShrinkStart();
                    /*
                    //Check for whether to announce that the games have started
                    if(hw.IsRunning()){
                        
                    }*/
                    
                    //Check for if we need to shrink any maps
                    if((start != null) && (shrink != null)){
                        //If we've started shrinking
                        if(shrink.before(new Date())){
                            long diffInMinutes = ((new Date().getTime() - shrink.getTime()) / 1000) / 60;
                            String interval = hw.getShrinkRateInterval();
                            double rate = hw.getShrinkRate();
                            int current_radius = hw.getRadius();
                            int original_radius = hw.getOriginalRadius();
                            int min_radius = hw.getMinimumRadius();
                            //Our expected radius should be
                            if((rate > 0) && (interval != null)){
                                if(interval.equals("minute")){                                    
                                    //Do nothing, the rate is already in minutes
                                    //rate = rate;
                                }
                                else if(interval.equals("hour")){
                                    rate = rate / 60;
                                }
                                else if(interval.equals("day")){
                                    rate = rate / (60 * 24);
                                }
                                //Rate is now in minutes
                                int iExpectedRadius  = (int)(original_radius -  diffInMinutes * rate);
                                int new_radius = -1;
                                boolean override = false;
                                //plugin.log("Expected:" + iExpectedRadius + " Original: " + original_radius + " diffInMinutes: " + diffInMinutes + " Rate: " + rate);
                                if(iExpectedRadius <= min_radius){
                                    if(current_radius != min_radius){
                                        new_radius = min_radius;
                                        override = true;
                                    }
                                }
                                else if(iExpectedRadius != current_radius){
                                    new_radius = iExpectedRadius;
                                }
                                
                                //If our new radius isn't greater than 5 or set to the minimum
                                //Don't bother
                                if(!override){
                                    if(Math.abs(new_radius - current_radius) < 5){
                                        new_radius = -1;
                                    }
                                }
                                
                                if(new_radius >= 0){
                                    hw.SetNewRadius(new_radius);
                                    plugin.AnnounceAll(hw.getDisplayName() + " radius is now " + plugin.getSettingsManager().CCParamValue + new_radius);
                                }
                                
                                
                            }
                            

                            
                        }
                    }
                    
                    
                    if(hw.IsRunning()){
                        for(HungerPlayer hp : hw.getPlayers())
                        {
                            //If the player is dead or has 
                            if(!hp.HasDied() && hp.Entered())
                            {
                                //
                                Player player = plugin.getServer().getPlayer(hp.getName());
                                if((player != null) && (player.getLocation() != null))
                                {
                                    Location l = player.getLocation();
                                    if(hw.getPlayerMoveDistance() > 0)
                                    {
                                        //can't add a null location
                                        hp.addLocation(player.getLocation());
                                        int dist = hp.CalculateTotalLocationDistance();
                                        if((dist != -1) && (hw.getPlayerMoveDistance() > 0) && (dist < hw.getPlayerMoveDistance())) {
                                            int iFoodLevel = player.getFoodLevel();
                                            if(iFoodLevel > 5){
                                                player.setFoodLevel(iFoodLevel - 1);
                                            }
                                            player.damage(1);
                                            ChatBlock.sendMessage(player, hw.getDisplayName() + " You can't sit on your ass.  Keep moving!");
                                        }
                                    }                           
                                }
                            }
                        }
                    }
                    
                    //If the games have already ended
                    if((hw.getEndTime() != null) && hw.getEndTime().before(new Date())) {
                        //This makes sure the games are ended
                        plugin.StopGame(hw);
                    }
                }
                
                

		
	}

    
}
