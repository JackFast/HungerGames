package com.nilla.hungergames.storage;

import com.nilla.hungergames.HungerGames;

import java.sql.*;
import java.util.logging.Logger;

/**
 * @author cc_madelg
 */
public class MySQLCore implements DBCore
{
    private Logger log;
    private Connection connection;
    private String host;
    private String username;
    private String password;
    private String database;
    private int port;

    /**
     * @param host
     * @param database
     * @param username
     * @param password
     */
    public MySQLCore(String host, int port, String database, String username, String password)
    {
        this.database = database;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.log = HungerGames.getLogger();

        initialize();
    }

    private void initialize()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        }
        catch (ClassNotFoundException e)
        {
            log.severe("ClassNotFoundException! " + e.getMessage());
        }
        catch (SQLException e)
        {
            log.severe("SQLException! " + e.getMessage());
        }
    }

    /**
     * @return connection
     */
    public Connection getConnection()
    {
        if (connection == null)
        {
            initialize();
        }

        return connection;
    }

    /**
     * @return whether connection can be established
     */
    public Boolean checkConnection()
    {
        return getConnection() != null;
    }

    /**
     * Close connection
     */
    public void close()
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (Exception e)
        {
            log.severe("Failed to close database connection! " + e.getMessage());
        }
    }

    /**
     * Execute a select statement
     *
     * @param query
     * @return
     */
    public ResultSet select(String query)
    {
        try
        {
            return getConnection().createStatement().executeQuery(query);
        }
        catch (SQLException ex)
        {
            log.severe("Error at SQL Query: " + ex.getMessage());
        }

        return null;
    }

    /**
     * Execute an insert statement
     *
     * @param query
     */
    public long insert(String query)
    {
        try
        {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = statement.getGeneratedKeys();

            if (keys.next())
            {
                return keys.getLong(1);
            }
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL INSERT Query: " + ex);
            }
        }

        return 0;
    }

    /**
     * Execute an update statement
     *
     * @param query
     */
    public void update(String query)
    {
        try
        {
            getConnection().createStatement().executeUpdate(query);
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL UPDATE Query: " + ex);
            }
        }
    }

    /**
     * Execute a delete statement
     *
     * @param query
     */
    public void delete(String query)
    {
        try
        {
            getConnection().createStatement().executeUpdate(query);
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL DELETE Query: " + ex);
            }
        }
    }

    /**
     * Execute a statement
     *
     * @param query
     * @return
     */
    public Boolean execute(String query)
    {
        try
        {
            getConnection().createStatement().execute(query);
            return true;
        }
        catch (SQLException ex)
        {
            log.severe(ex.getMessage());
            return false;
        }
    }

    /**
     * Check whether a table exists
     *
     * @param table
     * @return
     */
    public Boolean existsTable(String table)
    {
        try
        {
            ResultSet result = getConnection().createStatement().executeQuery("SELECT * FROM " + table);
            return result != null;
        }
        catch (SQLException ex)
        {
            if (!ex.getMessage().contains("exist"))
            {
                log.warning("Error at SQL Query: " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * Check whether a column exists
     *
     * @param table
     * @param column
     * @return
     */
    public Boolean existsColumn(String table, String column)
    {
        try
        {
            ResultSet result = getConnection().createStatement().executeQuery("SELECT " + column + " FROM " + table);
            return result != null;
        }
        catch (SQLException ex)
        {
            if (!ex.getMessage().contains("exist"))
            {
                log.warning("Error at SQL Query: " + ex.getMessage());
            }
            return false;
        }
    }
}
