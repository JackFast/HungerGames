package com.nilla.hungergames.managers;

import com.nilla.hungergames.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * @author phaed
 */
public final class CommandManager implements CommandExecutor
{
    private HungerGames plugin;
    private HashMap<String, String> hmSetParams  = new HashMap<String,String>();
    

    /**
     *
     */
    public CommandManager()
    {
        plugin = HungerGames.getInstance();
           
        hmSetParams.clear();
        hmSetParams.put("START_TIME", " VALUE - Start of Games");
        hmSetParams.put("END_TIME", " VALUE - end of games");
        hmSetParams.put("LAST_ENTRY", " VALUE- Last time to Enter Games");
        hmSetParams.put("SHRINK_START", " VALUE- shrink start time");
        hmSetParams.put("RADIUS", " VALUE- world radius");
        hmSetParams.put("CENTER", " X_VALUE Z_VALUE- world center at X,Z");
        hmSetParams.put("MINIMUM_SIZE", " VALUE - Minimum Size of the world");
        hmSetParams.put("SHRINK_RATE", " RADIUS per TIME(h/m/d) - Set the Shrink Rate ");
        hmSetParams.put("SPECTATOR", " X Y Z- Location to TP spectators to.  -Y means not set.");
        hmSetParams.put("WG_SPECTATOR", " Use worldedit region for spectators");
        hmSetParams.put("MAX_PLAYERS", " VALUE- max # players");
        hmSetParams.put("TP_MIN_DISTANCE", " VALUE- TP player distance");
        hmSetParams.put("SPECTATOR_TP_LIMIT", " VALUE- # TPs allowed per games per spectator");
        hmSetParams.put("", " - Unknown Parameter");
        
    }

   
    
/**
 * HungerGames Plugin for Bukkit
 * 
 * /hg or /hungergames
 * Admin Commands
 * /hg create WORLD_NAME - this will be the ID for the rest of the commands
 * /hg debug - Toggle Debug info
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
 * /hg list - list all the active games
 * /hg info [WORLD_NAME] - get all params about the game
 * /hg enroll [WORLD_NAME] - Enroll yourself in the games as a player
 * /hg enter [WORLD_NAME] - Enter an open game
 * /hg spectate [WORLD_NAME] - TP player to spectator arena inside world
 * /hg players [WORLD_NAME] - get remaining players
 * /hg coords [WORLD_NAME] - (permissions required) - get the coords of all players in games.  maybe make 5 mins old data?
 * 
 * @author EvilNilla 
 */
    public void SendHelpMessage(CommandSender sender, String sCommand){
        
        Player player = null;
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        List<String> defaultList = new ArrayList<String>();
        defaultList.add(sCommand);
        defaultList.add("Player Commands");
        defaultList.add("/hg list - list all the active games");
        defaultList.add("/hg info [WORLD_NAME] - get all params about the game");
        defaultList.add("/hg enroll [WORLD_NAME] - Enroll yourself in the games as a player");
        defaultList.add("/hg unenroll [WORLD_NAME] - UnEnroll yourself in the games as a player");
        defaultList.add("/hg enter [WORLD_NAME] - Enter an open game");
        defaultList.add("/hg spectate [WORLD_NAME] - TP player to spectator arena inside world");
        defaultList.add("/hg players [WORLD_NAME] - get remaining players");
        
        
        defaultList.add("");
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.create")){
            defaultList.add("/hg create WORLD_NAME");
        }
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.delete")){
            defaultList.add("/hg delete WORLD_NAME");
        }
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.clear_players")){
            defaultList.add("/hg clear_players WORLD_NAME");
        }
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.set")){
            defaultList.add("/hg set WORLD_NAME PARAMATERS VALUES");
        }
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.start")){
            defaultList.add("/hg start WORLD_NAME");
        }
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.stop")){
            defaultList.add("/hg stop WORLD_NAME");
        }
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.player")){
            defaultList.add("/hg player_enroll WORLD_NAME PLAYER_NAME");
            defaultList.add("/hg player_enter WORLD_NAME PLAYER_NAME");
            defaultList.add("/hg player_remove WORLD_NAME PLAYER_NAME - remove player from lists or game(resets their data)");
        }
        if(plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.player")){
            defaultList.add("/hg player_spectate WORLD_NAME PLAYER_NAME - force the player into spectating mode");
        }

        ChatBlock cb = new ChatBlock();
        if(sCommand.equals("hg")){
            for(String s : defaultList){
                cb.addRow(s);
            }
        }
        else if(sCommand.equals("set")){         
            //Add all the parameters
            for(String sParam : hmSetParams.keySet()){
                if(sParam.length() != 0){
                    cb.addRow(sParam + hmSetParams.get(sParam));    
                }
            }
        }
        else if(sCommand.startsWith("set ")){
            //Get the param starting at 4...(length of "set "
            String sParam = sCommand.substring(4).toUpperCase();
            cb.addRow(sParam + hmSetParams.get(sParam));
        }
        else if(sCommand.equals("no world selected")){
            cb.addRow("You must enter the name of a valid Hunger Games world");
        }
        else{
            cb.addRow("Unknown Command.  Try one of the following.");
            for(String s : defaultList){
                cb.addRow(s);
            }
        }
        cb.sendBlock(sender);
        
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        try
        {
            if (command.getName().equals("hg") || command.getName().equals("hungergames"))
            {
                Player player = null;
                HungerWorld hw = null;
                
                if (sender instanceof Player)
                {
                    player = (Player) sender;
                }

                //If we have no args, show the help message
                if (args.length == 0)
                {
                    SendHelpMessage(sender,"hg");
                    return true;
                }
                else if(args.length >= 1)
                {
                    String hunger_world = "";
                    String cmd = args[0];
                    //Pull the command out of the argument
                    args = Helper.removeFirst(args);
                    if(args.length > 0){
                        hunger_world = args[0];
                        args = Helper.removeFirst(args);
                        hw = plugin.findHungerWorld(hunger_world);
                    }
                    else if(plugin.getHungerWorlds().size() > 0)
                    {
                        //Set the default hunger world to the first
                        hw = plugin.getHungerWorlds().get(0);
                    }
                    
                    //ADMIN COMMANDS
                    if (cmd.equals("debug") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebug(!plugin.getSettingsManager().isDebug());
                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Debug output " + (plugin.getSettingsManager().isDebug() ? "enabled" : "disabled"));
                        return true;
                    }
                    else if (cmd.equals("debugdb") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebugdb(!plugin.getSettingsManager().isDebugdb());
                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Debug db output " + (plugin.getSettingsManager().isDebugdb() ? "enabled" : "disabled"));
                        return true;
                    }
                    else if (cmd.equals("debugsql") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebugsql(!plugin.getSettingsManager().isDebugsql());
                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Debug sql output " + (plugin.getSettingsManager().isDebugsql() ? "enabled" : "disabled"));
                        return true;
                    }
                    else if(cmd.equals("list")){
                        //Send a list of games to the player
                        plugin.listHungerWorlds(sender);
                        return true;
                    }
                    else if (cmd.equals("create") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.create"))
                    {
                        plugin.CreateWorld(sender, hunger_world);
                        return true;
                    }
                    else if(hw == null)
                    {
                        //Error message for not entering the name of a world
                        //Otherwise we require a world
                        SendHelpMessage(sender, "no world selected");
                        return true;
                    }
                    else if (cmd.equals("delete") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.delete"))
                    {
                        plugin.DeleteWorld(sender, hw);
                        return true;
                    }
                    else if (cmd.equals("clear_players") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.clear_players"))
                    {
                        hw.ClearPlayers(sender);
                        return true;
                    }
                    else if (cmd.equals("start") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.start"))
                    {
                        hw.Start(sender);
                        return true;
                    }
                    else if (cmd.equals("stop") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.stop"))
                    {
                        hw.Stop(sender);
                        return true;
                    }
                    else if (cmd.equals("set") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.set"))
                    {
                        hw.SetParameter(sender, args);
                        return true;
                    }
                    //Player management commands
                    else if (cmd.equals("player_remove") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.player"))
                    {
                        hw.AdminPlayerRemove(sender, args);
                        return true;
                    }
                    //Player management commands
                    else if (cmd.equals("player_enroll") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.player"))
                    {
                        hw.AdminPlayerEnroll(sender, args);
                        return true;
                    }
                    //Player management commands
                    else if (cmd.equals("player_enter") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.player"))
                    {
                        hw.AdminPlayerEnter(sender, args);
                        return true;
                    }
                    //Player management commands
                    else if (cmd.equals("player_spectate") && plugin.getPermissionsManager().hasPermission(player, "hungergames.admin.player"))
                    {
                        hw.AdminPlayerSpectate(sender, args);
                        return true;
                    }                            
                    //PLAYER COMMANDS
                    else if (cmd.equals("info") && plugin.getPermissionsManager().hasPermission(player, "hungergames.player.info"))
                    {
                        //hg info [WORLD_NAME] - get all params about the game
                        hw.SendWorldInfoToUser(sender);
                        return true;
                    }
                    else if(cmd.equals("enroll") && plugin.getPermissionsManager().hasPermission(player, "hungergames.player.enroll"))
                    {
                        //hg enroll [WORLD_NAME] - Enroll yourself in the games as a player
                        if(player != null){
                            hw.Enroll(player);
                        }
                        
                        return true;
                    }
                    else if(cmd.equals("unenroll") && plugin.getPermissionsManager().hasPermission(player, "hungergames.player.enroll"))
                    {
                        //hg enroll [WORLD_NAME] - Enroll yourself in the games as a player
                        if(player != null){
                            hw.UnEnroll(player);
                        }
                        return true;
                    }
                    else if (cmd.equals("enter") && plugin.getPermissionsManager().hasPermission(player, "hungergames.player.enter"))
                    {
                        //hg enter [WORLD_NAME] - Enter an open game
                        if(player != null){
                            hw.Enter(player);
                        }
                        return true;
                    }
                    else if (cmd.equals("spectate") && plugin.getPermissionsManager().hasPermission(player, "hungergames.player.spectate"))
                    {
                        //hg spectate [WORLD_NAME] - TP player to spectator arena inside world
                        if(player != null){
                            hw.Spectate(player);    
                        }
                        
                        return true;
                    }                            
                    else if (cmd.equals("players") && plugin.getPermissionsManager().hasPermission(player, "hungergames.player.players"))
                    {
                        //hg enter [WORLD_NAME] - Enter an open game
                        hw.PlayerList(sender);
                        return true;
                    }
                    else if (cmd.equals("coords") && plugin.getPermissionsManager().hasPermission(player, "hungergames.player.coords"))
                    {
                        //hg enter [WORLD_NAME] - Enter an open game
                        hw.Coords(sender);
                        return true;
                    }
                    else{
                                    

                        ChatBlock.sendMessage(sender, "Unknown command or no permissions.");
                        SendHelpMessage(sender,cmd);

                        return true;
                    }
                }

                // show the menu for the player
                SendHelpMessage(sender,"hg");
                return true;
            }
        }
        catch (Exception ex)
        {
            HungerGames.log(Level.SEVERE, "Command failure: {0}", ex.getMessage());
        }

        return false;
    }
}
