package com.nilla.hungergames.managers;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import com.nilla.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author phaed
 */
public final class PermissionsManager
{
    private PermissionHandler handler;
    private PermissionsPlugin handler2;
    private HungerGames plugin;

    /**
     *
     */
    public PermissionsManager()
    {
        plugin = HungerGames.getInstance();
        detectPermissionsBukkit();
        detectPermissions();
    }

    /**
     * Check whether a player has a permission
     * @param player
     * @param permission
     * @return
     */
    public boolean hasPermission(Player player, String permission)
    {
        if (player == null)
        {
            return true;
        }
        
        if(player.isOp())
        {
            return true;
        }

        if (handler != null)
        {
            return handler.has(player, permission);
        }
        else
        {
            return player.hasPermission(permission);
        }
    }

    /**
     * Check whether a player belongs to a group
     * @param playerName
     * @param group
     * @param world
     * @return
     */
    @SuppressWarnings(
    {
        "deprecation", "deprecation"
    })
    public boolean inGroup(String playerName, World world, String group)
    {
        if (handler2 != null)
        {
            List<Group> groups = handler2.getGroups(playerName);

            for (Group g : groups)
            {
                if (g.getName().equalsIgnoreCase(group))
                {
                    return true;
                }
            }
            return false;
        }

        if (handler != null)
        {
            if (handler.getGroup(world.getName(), playerName).equalsIgnoreCase(group))
            {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Get a player's groups
     * @param worldName
     * @param playerName
     * @return
     */
    public List<String> getGroups(String worldName, String playerName)
    {
        List<String> groups = new LinkedList<String>();

        if (handler2 != null)
        {
            List<Group> gs = handler2.getGroups(playerName);

            for (Group group : gs)
            {
                groups.add(group.getName().toLowerCase());
            }
            return groups;
        }

        if (handler != null)
        {
            @SuppressWarnings("deprecation")
            String group = handler.getGroup(worldName, playerName);

            if (group != null)
            {
                groups.add(group.toLowerCase());
            }
        }

        return groups;
    }

    private void detectPermissions()
    {
        if (handler == null)
        {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");

            if (test != null)
            {
                handler = ((Permissions) test).getHandler();
            }
        }
    }

    private void detectPermissionsBukkit()
    {
        if (handler2 == null)
        {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");

            if (test != null)
            {
                handler2 = ((PermissionsPlugin) test);
            }
        }
    }
}
