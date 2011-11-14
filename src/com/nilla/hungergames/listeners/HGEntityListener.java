/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nilla.hungergames.listeners;

import com.nilla.hungergames.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

/**
 *
 * @author telaeris
 */
public class HGEntityListener  extends EntityListener{
    
    private final HungerGames plugin;
    public HGEntityListener()
    {
        plugin = HungerGames.getInstance();
    }
    
    @Override
    public void onEntityDeath(EntityDeathEvent event)
    {
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        Player player = (Player)event.getEntity();
        
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
                    hp.SetSpectating(false);
                    plugin.getStorageManager().updateHungerPlayer(hp);
                    event.getDrops().clear();
                    ChatBlock.sendMessage(player, hw.getDisplayName() + ":" +  ChatColor.RED + " You may NOT Drop anything while spectating!");
                    HungerGames.log(player.getName() + " Died while spectating!");
                }
            }
        }
        
    }
}
