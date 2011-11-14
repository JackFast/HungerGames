package com.nilla.hungergames;

import com.nilla.hungergames.listeners.*;
import com.nilla.hungergames.managers.*;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.wimbli.WorldBorder.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;

/**
 * HungerGames Plugin for Bukkit
 * 
 * /hg or /hungergames
 * Admin Commands
 * /hg create WORLD_NAME - this will be the ID for the rest of the commands
 * /hg delete WORLD_NAME
 * /hg start WORLD_NAME
 * /hg set WORLD_NAME PARAMETER_NAME PARAMETER_VALUE
 * /hg player enroll WORLD_NAME PLAYER_NAME
 * /hg player enter WORLD_NAME PLAYER_NAME
 * /hg player remove WORLD_NAME PLAYER_NAME - remove player from lists or game
 * /hg spectator reset WORLD_NAME PLAYER_NAME - reset spectator TP count
 *
 * PARAMETERS:
 * start time
 * end time
 * world radius
 * world center
 * shrink min size
 * shrink rate
 * shrink start time
 * max # players
 * TP player distance
 * spectator TP limit - # TPs allowed per games per spectator
 *
 * 
 * Player Commands
 * /hg info [WORLD_NAME] - get all params about the game
 * /hg size [WORLD_NAME] - get size of the game you're currently in
 * /hg enroll [WORLD_NAME] - Enroll yourself in the games as a player
 * /hg enter [WORLD_NAME] - Enter an open game
 * /hg spectate [WORLD_NAME] - TP player to spectator arena inside world
 * /hg players [WORLD_NAME] - get remaining players
 * /hg coords [WORLD_NAME] - (permissions required) - get the coords of all players in games.  maybe make 5 mins old data?
 * 
 * @author EvilNilla 
 */
public class HungerGames extends JavaPlugin
{    
    private static HungerGames instance;
    private static final Logger logger = Logger.getLogger("Minecraft");
    private SettingsManager settingsManager;
    private CommandManager commandManager;
    private StorageManager storageManager;
    private PermissionsManager permissionsManager;
    private WorldBorder worldBorderPlugin;
    private WorldEditPlugin worldEditPlugin;
    private WorldGuardPlugin worldGuardPlugin;
    
    private HGPlayerListener playerListener;
    private HGEntityListener entityListener;
    
    private List<HungerWorld> hungerWorlds;
    

    public List<HungerWorld> getHungerWorlds(){
        return hungerWorlds;
    }

    public void setHungerWorlds(List<HungerWorld> ws){
        hungerWorlds = ws;
    }
    /*
     * Fake main to allow us to run from netbeans
     */
    public static void main(String[] args) {
        
        
    }
    public WorldEditPlugin getWorldEditPlugin(){
        if(worldEditPlugin != null){
            return worldEditPlugin;
        }
        else{
            return null;
        }
    }
    public WorldGuardPlugin getWorldGuardPlugin(){
        if(worldGuardPlugin != null){
            return worldGuardPlugin;
        }
        else{
            return null;
        }
    }
    
    public WorldBorder getWorldBorderPlugin(){
        if(worldBorderPlugin != null){
            return worldBorderPlugin;
        }
        else{
            return null;
        }
    }
    
    @Override
    public void onDisable()
    {
        StopHungerTimer();
        getStorageManager().processQueue();
        getServer().getScheduler().cancelTasks(this);
        getStorageManager().closeConnection();

    }

    @Override
    public void onEnable()
    {
        
        Plugin wtest = getServer().getPluginManager().getPlugin("WorldBorder");
        if(wtest != null){
            worldBorderPlugin = (WorldBorder)wtest;
        }
        
        wtest = getServer().getPluginManager().getPlugin("WorldEdit");
        if(wtest != null){
            worldEditPlugin = (WorldEditPlugin)wtest;
        }
        
        wtest = getServer().getPluginManager().getPlugin("WorldGuard");
        if(wtest != null){
            worldGuardPlugin = (WorldGuardPlugin)wtest;
        }
        
        hungerWorlds = new ArrayList<HungerWorld>();
        instance = this;

        settingsManager = new SettingsManager();
        commandManager = new CommandManager();
        permissionsManager = new PermissionsManager();
        storageManager = new StorageManager();
        
        
        playerListener = new HGPlayerListener();
        entityListener = new HGEntityListener();
        
        registerEvents();
        registerCommands();
        
        
        displayStatusInfo();
        //Loading the hungerWorlds from MySQL is done when we create the storageManager
        
        ConsoleCommandSender sender = instance.getServer().getConsoleSender();
        listHungerWorlds(sender);
        
        StartHungerTimer();

    }
    
    //
    public void CreateWorld(CommandSender sender, String world_name)
    {
      if(world_name.isEmpty()){
          ChatBlock.sendMessage(sender, settingsManager.CCDefaultText + "World name can not be blank");
          return;
      }
      HungerWorld hw = findHungerWorld(world_name);
      if(hw != null)
      {
          ChatBlock.sendMessage(sender, settingsManager.CCDefaultText + "Hunger World '" + hw.getDisplayName() + "' already exists.");
      }
      else
      {   
          World this_w = null;
          //Check if we already have that world in our list of worlds
          for (World w : getServer().getWorlds()){
              if(w.getName().equals(world_name)){
                this_w = w;
                break;
              }
          }
          if(getServer().getWorlds().get(0).getName().equals(world_name)){
            ChatBlock.sendMessage(sender, ChatColor.RED + "ERROR: Can't Use Default World");
            return;
          }
          if(this_w == null){
            ChatBlock.sendMessage(sender, ChatColor.YELLOW + "WARNING: World '" + settingsManager.CCWorldName + world_name + ChatColor.RED + "' doesn't exist!  Proceed with caution.");    
          }
          hw = new HungerWorld(this_w, world_name);
          hungerWorlds.add(hw);
          getStorageManager().insertHungerWorld(hw);
          getStorageManager().updateHungerWorld(hw);
          ChatBlock.sendMessage(sender, settingsManager.CCDefaultText + "Hunger World '" + hw.getDisplayName() + "' created");
         /* }
          else{
              ChatBlock.sendMessage(sender, ChatColor.RED + "ERROR: World '" + settingsManager.CCWorldName + world_name + ChatColor.RED + "' doesn't exist!");
          }*/
      }
    }
    
    public void StopGame(HungerWorld hw){
        //just gotta look for games that have ended and if there are any active or spectating players in them
        //if so, return those players home
        for(HungerPlayer hp : hw.getPlayers()){
            if((hp.Entered() && !hp.HasDied()) || hp.Spectating())
            {
                //See if the player is online
                Player p= instance.getServer().getPlayer(hp.getName());
                if(p != null){
                    //If the player is online, take the appropriate action:
                    if(hp.Spectating())
                    {
                        if(hw.TeleportPlayer(hp, p, hp.getStartingLocation(), "SPECTATE GAME OVER")){
                            hp.SetSpectating(false);
                            instance.getStorageManager().updateHungerPlayer(hp);
                            ChatBlock.sendMessage(p, hw.getDisplayName() + " These Hunger Games are Over!");
                        }
                    }
                    else if(hp.Entered())
                    {
                        p.getInventory().clear();
                        if(hw.TeleportPlayer(hp, p, hp.getStartingLocation(), "PLAYER GAME OVER")){
                            hp.SetDied(true);
                            instance.getStorageManager().updateHungerPlayer(hp);
                            ChatBlock.sendMessage(p, hw.getDisplayName() + " These Hunger Games are Over!");
                        }
                    }

                }
            }
        }
    }
            
    
    public void DeleteWorld(CommandSender sender, HungerWorld hw)
    {
        hw.Stop(sender);
        getStorageManager().deleteHungerWorld(hw.getName());
        ChatBlock.sendMessage(sender, settingsManager.CCDefaultText + "Hunger World '" + hw.getDisplayName() + "' removed.");
        //Remove the world
        hungerWorlds.remove(hw);
        
    }
    
    public HungerWorld findHungerWorld(String world){
        for (HungerWorld hw : hungerWorlds){
            if(hw.getName().equals(world)){
                return hw;
            }
        }
        return null;
    }
    
    public void listHungerWorlds(CommandSender sender){
        ChatBlock cb = new ChatBlock();
        cb.addRow(settingsManager.CCDefaultText + "List of Hunger Worlds:");
        for (HungerWorld hw : hungerWorlds){
            String s = hw.getDisplayName();
            s += " " + ChatColor.GREEN + hw.getEnrolledPlayerCount() + getSettingsManager().CCDefaultText + " players enrolled";
            cb.addRow(s);
        }
        if(hungerWorlds.isEmpty()){
            cb.addRow(ChatColor.RED + "(none)");
        }
        cb.sendBlock(sender);        
    }
    
    private void registerEvents()
    {
        //Events for playerListener
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
        
        //Events for entityListener
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
    }

    private void registerCommands()
    {
        getCommand("hg").setExecutor(commandManager);
    }

    
    public void AnnounceAll(String sMsg){
        for(World w : instance.getServer().getWorlds()){
            for(Player p : w.getPlayers()){
                ChatBlock.sendMessage(p, sMsg);
            }
        }
        instance.log(sMsg);
    }
    
    
    /*
     * Announce to those that are enrolled or were enrolled
     */
    public void AnnounceEnrolled(String sMsg){
        for(World w : instance.getServer().getWorlds()){
            for(HungerWorld hw : hungerWorlds){
                if(hw.getName().equals(w.getName())){
                    for(Player p : w.getPlayers()){
                        HungerPlayer hp = hw.findHungerPlayer(p.getName());
                        if((hp !=  null) && hp.Enrolled() ) {
                            ChatBlock.sendMessage(p, sMsg);
                        }
                    }
                }
                
            }
        }
        instance.log(sMsg);

    }
    
    /*
     * Announce to those that are enrolled but not dead yet
     */
    public void AnnounceAlive(String sMsg){
        for(World w : instance.getServer().getWorlds()){
            for(HungerWorld hw : hungerWorlds){
                if(hw.getName().equals(w.getName())){
                    for(Player p : w.getPlayers()){
                        HungerPlayer hp = hw.findHungerPlayer(p.getName());
                        if((hp !=  null) && hp.Enrolled() && !hp.HasDied()) {
                            ChatBlock.sendMessage(p, sMsg);
                        }
                    }
                }
                
            }
        }
        instance.log(sMsg);
    }
    
     /**
     * @return the instance
     */
    public static HungerGames getInstance()
    {
        return instance;
    }

    /**
     * @return the logger
     */
    public static Logger getLogger()
    {
        return logger;
    }
    
    /**
     * @return the storageManager
     */
    public StorageManager getStorageManager()
    {
        return storageManager;
    }
    
    /**
     * @return the settingsManager
     */
    public SettingsManager getSettingsManager()
    {
        return settingsManager;
    }

    /**
     * @return the commandManager
     */
    public CommandManager getCommandManager()
    {
        return commandManager;
    }
    /**
     * @return the permissionsManager
     */
    public PermissionsManager getPermissionsManager()
    {
        return permissionsManager;
    }
    
    /**
     * Parameterized logger
     *
     * @param level
     * @param msg   the message
     * @param arg   the arguments
     */
    public static void log(Level level, String msg, Object... arg)
    {
        logger.log(level, new StringBuilder().append("[HungerGames] ").append(MessageFormat.format(msg, arg)).toString());
    }

    /**
     * Parameterized info logger
     *
     * @param msg
     * @param arg
     */
    public static void log(String msg, Object... arg)
    {
        log(Level.INFO, msg, arg);
    }
    
    private void displayStatusInfo()
    {
        log("Version {0} loaded", getDescription().getVersion());
    }
    
    private static int HungerTimerTask = -1;
    public static void StartHungerTimer()
    {
            StopHungerTimer();

            //Theoretically check thsi every 5 seconds
            HungerTimerTask = instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, new HungerTimerClass(instance),150, 150);

            if (HungerTimerTask == -1)
                    log(Level.WARNING, "Failed to start timed border-checking task! This will prevent the plugin from working. Try restarting Bukkit.");

            log("Border-checking timed task started.");
    }

    public static void StopHungerTimer()
    {
            if (HungerTimerTask == -1) return;

            instance.getServer().getScheduler().cancelTask(HungerTimerTask);
            HungerTimerTask = -1;
            log("Border-checking timed task stopped.");
    }
}
