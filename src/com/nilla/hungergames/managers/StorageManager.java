package com.nilla.hungergames.managers;

import com.nilla.hungergames.HungerGames;
import com.nilla.hungergames.HungerPlayer;
import com.nilla.hungergames.HungerWorld;
import com.nilla.hungergames.storage.*;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;

/**
 * @author phaed
 */
public final class StorageManager
{
    /**
     *
     */
    private DBCore core;
    private HungerGames plugin;
    
    /**
     *
     */
    public StorageManager()
    {
        plugin = HungerGames.getInstance();

        initiateDB();
        migrateDB();
        loadWorldData();
//        saverScheduler();
    }

    private void migrateDB(){
        //So far, this is all good for both SQLite and MySQL
        String sMigrateWorldStart = "ALTER TABLE hunger_worlds add column ";
        
        //Add a new try/catch for each migration
        try{
            if(!core.execute(sMigrateWorldStart + "`spectator_x2` integer(10) default 0")){
                return;
            }
            core.execute(sMigrateWorldStart + "`spectator_y2` integer(10) default 0");
            core.execute(sMigrateWorldStart + "`spectator_z2` integer(10) default 0");
            
            HungerGames.log("Updated DB with Spectator_2 fields");
        }
        catch(Exception ex){
            //ignore the exception
        }
    }
    /**
     * Initiates the db
     * borrowed from pstones
     */
    private void initiateDB()
    {
        HashMap<String, String> htMySQL = new HashMap<String, String>();
        HashMap<String, String> htSQLite = new HashMap<String, String>();
        
        //Assess the different syntaxes in the table types
        //By keeping them matching here
        htMySQL.put("hunger_worlds","CREATE TABLE IF NOT EXISTS `hunger_worlds` "+
                "(  `id` bigint(20) NOT NULL auto_increment, " +
                "`world` varchar(25) default NULL,  " +
                "`radius` integer(10) default NULL,  " +
                "`original_radius` integer(10) default NULL,  " +                
                "`minimum_radius` integer(10) default NULL,  " +
                "`center_x` integer(10) default NULL,  " +
                "`center_z` integer(10) default NULL,  " +
                "`spectator_x` integer(10) default 0,  " +
                "`spectator_y` integer(10) default -1,  " +
                "`spectator_z` integer(10) default 0,  " +
                "`spectator_x2` integer(10) default 0,  " +
                "`spectator_y2` integer(10) default -1,  " +
                "`spectator_z2` integer(10) default 0,  " +
                "`max_players` integer(10) default NULL,  " +
                "`tp_player_distance` integer(10) default NULL,  " +
                "`spectator_tp_limit` integer(10) default NULL,  " +
                "`start_time` DATETIME default NULL,  " +
                "`last_entry_time` DATETIME default NULL,  " +                
                "`end_time` DATETIME default NULL,  " +
                "`shrink_rate` integer(10) default NULL,  " +
                "`shrink_rate_interval` varchar(40) default NULL,  " +
                "`shrink_start` varchar(40) default NULL,  " +
                "" +
                "PRIMARY KEY  (`id`),  UNIQUE KEY `world_name` (`world`));"+
                "" );
        htSQLite.put("hunger_worlds","CREATE TABLE IF NOT EXISTS `hunger_worlds` "+
                "(  `id` bigint(20) PRIMARY KEY, " +
                "`world` varchar(25) UNIQUE default NULL,  " +
                "`radius` integer(10) default NULL,  " +
                "`original_radius` integer(10) default NULL,  " +
                "`minimum_radius` integer(10) default NULL,  " +
                "`center_x` integer(10) default NULL,  " +
                "`center_z` integer(10) default NULL,  " +
                "`spectator_x` integer(10) default 0,  " +
                "`spectator_y` integer(10) default -1,  " +
                "`spectator_z` integer(10) default 0,  " +
                "`spectator_x2` integer(10) default 0,  " +
                "`spectator_y2` integer(10) default -1,  " +
                "`spectator_z2` integer(10) default 0,  " +
                "`max_players` integer(10) default NULL,  " +
                "`tp_player_distance` integer(10) default NULL,  " +
                "`spectator_tp_limit` integer(10) default NULL,  " +
                "`start_time` TICKS default NULL,  " +
                "`last_entry_time` TICKS default NULL,  " +
                "`end_time` TICKS default NULL,  " +
                "`shrink_rate` integer(10) default NULL,  " +
                "`shrink_rate_interval` varchar(40) default NULL,  " +
                "`shrink_start` varchar(40) default NULL  " +                
                "" +
                ")" );
            
        htMySQL.put("hunger_players","CREATE TABLE IF NOT EXISTS `hunger_players` "+
                "(  `id` bigint(20) NOT NULL auto_increment, " +
                "`world` varchar(25) default NULL,  " +
               "`name` varchar(25) default NULL,  " +
               "`is_entered` boolean default 0,  " +
               "`is_enrolled` boolean default 0,  " +
               "`is_spectating` boolean default 0,  " +
               "`has_died` boolean default 0,  " +
               "`spectate_tp_count` integer(10) default 0, " +
               "`starting_world` varchar(25) default NULL,  " +
               "`starting_x` integer(10) default NULL,  " +
               "`starting_y` integer(10) default NULL,  " +
               "`starting_z` integer(10) default NULL,  " +
                "" +
                "PRIMARY KEY  (`id`),  UNIQUE KEY `player_and_world_name` (`name`,`world`));"+
                "");
        htSQLite.put("hunger_players","CREATE TABLE IF NOT EXISTS `hunger_players` "+
                "(  `id` bigint(20) PRIMARY KEY, " +
                "`world` varchar(25) default NULL,  " +
               "`name` varchar(25) default NULL,  " +
               "`is_entered` boolean default 0,  " +
               "`is_enrolled` boolean default 0,  " +
               "`is_spectating` boolean default 0,  " + 
               "`has_died` boolean default 0,  " +
               "`spectate_tp_count` integer(10) default 0, " +
               "`starting_world` varchar(25) default NULL,  " +
               "`starting_x` integer(10) default NULL,  " +
               "`starting_y` integer(10) default NULL,  " +
               "`starting_z` integer(10) default NULL)  " +
                "");
        if (plugin.getSettingsManager().isUseMysql())
        {
            core = new MySQLCore(plugin.getSettingsManager().getHost(), plugin.getSettingsManager().getPort(), plugin.getSettingsManager().getDatabase(), plugin.getSettingsManager().getUsername(), plugin.getSettingsManager().getPassword());

            if (core.checkConnection())
            {
                HungerGames.log("MySQL Connection successful");
                for(String sTable : htMySQL.keySet()){
                    if (!core.existsTable(sTable)){
                        HungerGames.log("Creating table: " + sTable);
                        core.execute(htMySQL.get(sTable));    
                    }
                }
            }
            else
            {
                HungerGames.log("MySQL Connection failed");
            }
        }
        else
        {
            core = new SQLiteCore("HungerGames", plugin.getDataFolder().getPath());

            if (core.checkConnection())
            {
                HungerGames.log("SQLite Connection successful");

                for(String sTable : htSQLite.keySet()){
                    if (!core.existsTable(sTable)){
                        HungerGames.log("Creating table: " + sTable);
                        core.execute(htSQLite.get(sTable));    
                    }
                }

            }
            else
            {
                HungerGames.log("SQLite Connection failed");
            }
        }
    }

    /**
     * Closes DB connection
     */
    public void closeConnection()
    {
        core.close();
    }

    /**
     * Load hunger games info for any world that is loaded
     */
    public void loadWorldData()
    {
        List<HungerWorld> hws = plugin.getHungerWorlds();
        hws.clear();
        
        //Only operate on worlds that are loaded into the database
        List<World> worlds = plugin.getServer().getWorlds();        
        
        
        plugin.setHungerWorlds(loadWorldsFromDb(worlds));
        

        
    }

    
    
    
    /**
     * Loads all fields for a specific world into memory
     *
     * @param world the world to load
     */
    public List<HungerWorld> loadWorldsFromDb(List<World> worlds)
    {
        List<HungerWorld> hw_list = new ArrayList<HungerWorld>();

        synchronized (this)
        {
            String query = "SELECT * from hunger_worlds;";
            ResultSet res = core.select(query);

            if (res != null)
            {
                try
                {
                    while (res.next())
                    {
                        try
                        {
                            String world_name = res.getString("world");
                            World this_world = null;
                            for(World w : worlds){
                                if(w.getName().equals(world_name)){
                                    this_world = w;
                                    break;
                                }
                            }
                            
                            //Note: this_world is allowed to be null
                            HungerWorld m_hungerWorld;
                            //Create the world from the existing world of the same name
                            m_hungerWorld = new HungerWorld(this_world, world_name);
                            m_hungerWorld.loadFromResultSet(res);
                            m_hungerWorld.setBorders();
                            if(m_hungerWorld != null){
                                List<HungerPlayer> p = loadPlayersFromDb(m_hungerWorld);
                                m_hungerWorld.setPlayers(p);
                                hw_list.add(m_hungerWorld);
                                HungerGames.log("world: {0} loaded", m_hungerWorld.getName());
                            }
                        }
                        catch (Exception ex)
                        {
                            HungerGames.getLogger().info(ex.getMessage());
                        }
                    }
                }
                catch (SQLException ex)
                {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }



        }

        return hw_list;
    }
    
    public List<HungerPlayer> loadPlayersFromDb(HungerWorld world){
        List<HungerPlayer> lst;
        synchronized (this)
        {
            lst = getHungerPlayers(world);
        }
        if (lst != null)
        {
            HungerGames.log("world ({0}): {1} players loaded", world.getName(), lst.size());
        }
        return lst;
    }


    
    /**
     * Retrieves Data for this world from the database
     *
     * @param worldName
     * @return
     */
    public List<HungerPlayer> getHungerPlayers(HungerWorld world)
    {
        List<HungerPlayer> out = new ArrayList<HungerPlayer>();
        String query = "SELECT * from hunger_players  WHERE world = '" + world.getName() + "';";
        ResultSet res = core.select(query);
        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        HungerPlayer p = new HungerPlayer(world,res);
                        
                        //Create the world from the existing world of the same name
                        out.add(p);

                    }
                    catch (Exception ex)
                    {
                        HungerGames.getLogger().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return out;
    }

   
    public void updateHungerPlayer(HungerPlayer p)
    {
        Location starting_loc = p.getStartingLocation();
        
        String query = "UPDATE `hunger_players` set ";
        query += "is_entered=" + (p.Entered() ? "1" : "0" );
        query += ",is_enrolled=" + (p.Enrolled() ? "1" : "0" );
        query += ",is_spectating=" + (p.Spectating() ? "1" : "0" );
        query += ",has_died=" + (p.HasDied() ? "1" : "0" );
        query += ",spectate_tp_count=" + p.getSpectateTPCount();
        if(starting_loc != null){
            query += ",starting_world='" + starting_loc.getWorld().getName() + "'";
            query += ",starting_x=" + starting_loc.getBlockX();
            query += ",starting_y=" + starting_loc.getBlockY();
            query += ",starting_z=" + starting_loc.getBlockZ();
        }
        
        query += " WHERE ";
        query += "name='" + p.getName() + "' AND ";
        query += "world='" + p.getWorldName() + "';";
        core.execute(query);
        if (plugin.getSettingsManager().isDebugsql())
        {
            HungerGames.getLogger().info(query);
        }
    }
    
     /*
     * Insert an world into the database
     *
     * @param ub
     */
        public void insertHungerPlayer(HungerPlayer hp)
    {
        String query = "INSERT INTO `hunger_players` (`name`, `world`) ";
        String values = "VALUES ( '" + hp.getName() + "','" + hp.getWorldName() + "');";
        core.insert(query + values);
        if (plugin.getSettingsManager().isDebugsql())
        {
            HungerGames.getLogger().info(query + values);
        }
    }
    
    public void updateHungerWorld(HungerWorld hw)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = hw.getStartTime();
        Date last_entry = hw.getLastEntryTime();
        Date end = hw.getEndTime();
        Date shrink = hw.getShrinkStart();
        String query = "UPDATE `hunger_worlds` set ";
        query += "center_x=" + hw.getCenterX();
        query += ",center_z=" + hw.getCenterZ();
        if(start != null){
            query += ",start_time='" + formatter.format(start) + "'";
        }
        if(last_entry != null){
            query += ",last_entry_time='" + formatter.format(last_entry) + "'";
        }
        if(end != null){
            query += ",end_time='" + formatter.format(end) + "'";
        }
        if(shrink != null){
            query += ",shrink_start='" + formatter.format(shrink) + "'";
        }
        
        query += ",spectator_x=" + hw.getSpectatorX();
        query += ",spectator_y=" + hw.getSpectatorY();
        query += ",spectator_z=" + hw.getSpectatorZ();
        query += ",spectator_x2=" + hw.getSpectatorX2();
        query += ",spectator_y2=" + hw.getSpectatorY2();
        query += ",spectator_z2=" + hw.getSpectatorZ2();
        query += ",max_players=" + hw.getMaxPlayers();
        query += ",minimum_radius=" + hw.getMinimumRadius();
        query += ",original_radius=" + hw.getOriginalRadius();
        query += ",radius=" + hw.getRadius();
        query += ",shrink_rate=" + hw.getShrinkRate();
        query += ",shrink_rate_interval='" + hw.getShrinkRateInterval() + "'";
        query += ",spectator_tp_limit=" + hw.getSpectatorTpLimit();
        query += ",tp_player_distance=" + hw.getTpPlayerDistance();
        query += " WHERE world='" + hw.getName() + "';";
        core.execute(query);
        if (plugin.getSettingsManager().isDebugsql())
        {
            HungerGames.getLogger().info(query);
        }
    }

    /*
     * Insert an world into the database
     *
     * @param ub
     */
    public void insertHungerWorld(HungerWorld hw)
    {
        String query = "INSERT INTO `hunger_worlds` (`world`) ";
        String values = "VALUES ( '" + hw.getName() + "');";
        core.insert(query + values);
        if (plugin.getSettingsManager().isDebugsql())
        {
            HungerGames.getLogger().info(query + values);
        }
    }

    /**
     * Delete an world from the database
     *
     * @param ub
     */
    public void deleteHungerWorld(String worldName)
    {
        //Delete the players
        String query = "DELETE FROM `hunger_players` WHERE world = '" + worldName + "';";
        core.delete(query);
        if (plugin.getSettingsManager().isDebugsql())
        {
            HungerGames.getLogger().info(query);
        }
        //Delete the world
        query = "DELETE FROM `hunger_worlds` WHERE world = '" + worldName + "';";
        core.delete(query);
        if (plugin.getSettingsManager().isDebugsql())
        {
            HungerGames.getLogger().info(query);
        }
        
    }


    /**
     * Delete a player from the hunger_players table
     *
     * @param playerName
     */
    public void deleteHungerPlayer(String playerName)
    {
        String query = "DELETE FROM `hunger_players` WHERE name = '" + playerName + "';";
        core.delete(query);
    }



    /**
     * Schedules the pending queue on save frequency
     *
     * @return
     */
    /*
    public int saverScheduler()
    {
        return plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable()
        {
            public void run()
            {
                if (plugin.getSettingsManager().isDebugsql())
                {
                    HungerGames.getLogger().info("[Queue] processing queue...");
                }
                processQueue();
            }
        }, 0, 20L * plugin.getSettingsManager().getSaveFrequency());
    }
*/
    /**
     * Process entire queue
     */
    public void processQueue()
    {

        if (plugin.getSettingsManager().isDebugdb())
        {
            HungerGames.getLogger().info("[Queue] done");
        }
    }


 
 
}
