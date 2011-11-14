package com.nilla.hungergames.managers;

import com.nilla.hungergames.HungerGames;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

import java.util.*;
import org.bukkit.ChatColor;

/**
 * @author phaed
 */
public final class SettingsManager
{
    
    //These are the plugin settings
    private boolean logToHawkEye;
    private int saveFrequency;
    private boolean debug;
    private boolean debugdb;
    private boolean debugsql;
    private int linesPerPage;
    private boolean useMysql;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;

    //These are the default hungergames settings
    private int default_radius;
    private int default_minimum_radius;
    private int default_center_x;
    private int default_center_z;
    private int default_shrink_rate;
    private String default_shrink_rate_interval;
    private int default_max_players;
    private int default_tp_player_distance;
    private int default_spectator_tp_limit;
    
    public ChatColor CCDefaultText;
    public ChatColor CCWorldName;
    public ChatColor CCPlayerName;
    public ChatColor CCParamName;
    public ChatColor CCParamValue;
    private HungerGames plugin;

    /**
     *
     */
    public SettingsManager()
    {
        plugin = HungerGames.getInstance();
        load();
    }

    /**
     * Load the configuration
     */
    @SuppressWarnings("unchecked")
    public void load()
    {
        
        CCDefaultText = ChatColor.AQUA;
        CCParamName = ChatColor.GREEN;
        CCParamValue = ChatColor.YELLOW;
        CCWorldName = ChatColor.GOLD;
        
        Configuration config = plugin.getConfiguration();
        config.load();
        linesPerPage = config.getInt("settings.lines-per-page", 12);
        logToHawkEye = config.getBoolean("settings.log-to-hawkeye", true);
        debugdb = config.getBoolean("settings.debug-on", false);
        saveFrequency = config.getInt("saving.frequency-seconds", 300);
        useMysql = config.getBoolean("mysql.enable", false);
        host = config.getString("mysql.host", "localhost");
        port = config.getInt("mysql.port", 3306);
        database = config.getString("mysql.database", "minecraft");
        username = config.getString("mysql.username", "minecraft");
        password = config.getString("mysql.password", "");
    
        //Plugin specific settings
        default_radius = config.getInt("default.radius", 800);;
        default_minimum_radius = config.getInt("default.minimum_radius", 50);
        default_center_x = config.getInt("default.center_x", 0);
        default_center_z  = config.getInt("default.center_z", 0);
        default_shrink_rate = config.getInt("default.shrint_rate", 100);
        default_shrink_rate_interval = config.getString("default.shrint_rate_interval", "hour");
        default_max_players = config.getInt("default.max_players", 0);
        default_tp_player_distance = config.getInt("default.tp_player_distance", 40);
        default_spectator_tp_limit = config.getInt("default.spectator_tp_limit", 0);
    
        save();
    }

    /**
     *
     */
    public void save()
    {
        Configuration config = plugin.getConfiguration();
        config.setProperty("settings.lines-per-page", getLinesPerPage());
        config.setProperty("settings.log-to-hawkeye", isLogToHawkEye());
        config.setProperty("saving.frequency-seconds", getSaveFrequency());
        config.setProperty("mysql.enable", isUseMysql());
        config.setProperty("mysql.host", getHost());
        config.setProperty("mysql.port", getPort());
        config.setProperty("mysql.database", getDatabase());
        config.setProperty("mysql.username", getUsername());
        config.setProperty("mysql.password", getPassword());

        config.save();
    }

    /**
     * @return the logToHawkEye
     */
    public boolean isLogToHawkEye()
    {
        return logToHawkEye;
    }

    /**
     * @return the saveFrequency
     */
    public int getSaveFrequency()
    {
        return saveFrequency;
    }

    /**
     * @return the debug
     */
    public boolean isDebug()
    {
        return debug;
    }

    /**
     * @return the debugdb
     */
    public boolean isDebugdb()
    {
        return debugdb;
    }

    /**
     * @return the debugsql
     */
    public boolean isDebugsql()
    {
        return debugsql;
    }


    /**
     * @return the linesPerPage
     */
    public int getLinesPerPage()
    {
        return linesPerPage;
    }

    /**
     * @return the useMysql
     */
    public boolean isUseMysql()
    {
        return useMysql;
    }

    /**
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @return the database
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    /**
     * @param debugdb the debugdb to set
     */
    public void setDebugdb(boolean debugdb)
    {
        this.debugdb = debugdb;
    }

    /**
     * @param debugsql the debugsql to set
     */
    public void setDebugsql(boolean debugsql)
    {
        this.debugsql = debugsql;
    }

    

    public int getPort()
    {
        return port;
    }
    
    public int getDefaultRadius(){
        return default_radius;
    }
    public int getDefaultMinimumRadius(){
        return default_minimum_radius;
    }
    public int getDefaultCenterX(){
        return default_center_x;
    }
    public int getDefaultCenterZ(){
        return default_center_z;
    }
    public int getDefaultShrinkRate(){
        return default_shrink_rate;
    }
    public String getDefaultShrinkRateInterval(){
        return default_shrink_rate_interval;
    }
    
    public int getDefaultMaxPlayers(){
        return default_max_players;
    }
    public int getDefaultTpPlayerDistance(){
        return default_tp_player_distance;
    }
    public int getDefaultSpectatorTpLimit(){
        return default_spectator_tp_limit;
    }
    
    
}
