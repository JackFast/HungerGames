package com.nilla.hungergames;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author phaed
 */
public class Helper
{
    
    private static final DateFormat sql_in_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
    public static Date ParseDate(String strDate){
        try{
            if(strDate == null)
            {
                return null;
            }
            else
            {
                return sql_in_formatter.parse(strDate);
            }
        }
        catch(Exception ex){
            return null;
        }
    }
    public static int ParseInt(String strInt){
        if(strInt == null){
            return 0;
        }
        int ret = 0;
        try{
            
            ret = Integer.parseInt(strInt);
        }
        catch(Exception ex){
            return 0;
        }
        return ret;
    }
        
    public static double getDistance(Location loc1, int x, int z)
    {
        long xDiff = loc1.getBlockX() - x;
        long zDiff = loc1.getBlockZ() - z;
        return Math.sqrt(xDiff * xDiff + zDiff * zDiff);
    }
    /**
     * Helper function to check for integer
     * @param o
     * @return
     */
    public static boolean isInteger(Object o)
    {
        return o instanceof java.lang.Integer;
    }

    /**
     * Helper function to check for byte
     * @param input
     * @return
     */
    public static boolean isByte(String input)
    {
        try
        {
            Byte.parseByte(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for short
     * @param input
     * @return
     */
    public static boolean isShort(String input)
    {
        try
        {
            Short.parseShort(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for integer
     * @param input
     * @return
     */
    public static boolean isInteger(String input)
    {
        try
        {
            Integer.parseInt(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for integer list
     * @param obj
     * @return
     */
    public static boolean isIntList(Object obj)
    {
        try
        {
            @SuppressWarnings("unchecked")
            List<Integer> list = (List<Integer>) obj;
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for string list
     * @param obj
     * @return
     */
    public static boolean isStringList(Object obj)
    {
        try
        {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) obj;
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for float
     * @param input
     * @return
     */
    public static boolean isFloat(String input)
    {
        try
        {
            Float.parseFloat(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for string
     * @param o
     * @return
     */
    public static boolean isString(Object o)
    {
        return o instanceof java.lang.String;
    }

    /**
     * Helper function to check for boolean
     * @param o
     * @return
     */
    public static boolean isBoolean(Object o)
    {
        return o instanceof java.lang.Boolean;
    }

    /**
     * Remove a character from a string
     * @param s
     * @param c
     * @return
     */
    public static String removeChar(String s, char c)
    {
        String r = "";

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) != c)
            {
                r += s.charAt(i);
            }
        }

        return r;
    }

    /**
     * Remove first character from a string
     * @param s
     * @param c
     * @return
     */
    public static String removeFirstChar(String s, char c)
    {
        String r = "";

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) != c)
            {
                r += s.charAt(i);
                break;
            }
        }

        return r;
    }

    /**
     * Capitalize first word of sentence
     * @param content
     * @return
     */
    public static String capitalize(String content)
    {
        if (content.length() < 2)
        {
            return content;
        }

        String first = content.substring(0, 1).toUpperCase();
        return first + content.substring(1);
    }

    /**
     *
     * @param playername
     * @return
     */
    public static Player matchSinglePlayer(String playername)
    {
        List<Player> players = HungerGames.getInstance().getServer().matchPlayer(playername);

        if (players.size() == 1)
        {
            return players.get(0);
        }

        return null;
    }

    /**
     * Convert block type names to friendly format
     * @param type
     * @return
     */
    public static String friendlyBlockType(String type)
    {
        String out = "";

        type = type.toLowerCase().replace('_', ' ');

        String[] words = type.split("\\s+");

        for (String word : words)
        {
            out += capitalize(word) + " ";
        }

        return out.trim();
    }

    /**
     * Return plural word if count is bigger than one
     * @param count
     * @param word
     * @param ending
     * @return
     */
    public static String plural(int count, String word, String ending)
    {
        return count == 1 ? word : word + ending;
    }

    /**
     * Return plural word if count is bigger than one
     * @param word
     * @return
     */
    public static String posessive(String word)
    {
        if (word.substring(word.length() - 1).equalsIgnoreCase("s"))
        {
            return word + "'";
        }
        else
        {
            return word + "'s";
        }
    }

    /**
     * Removes color codes from strings
     * @param msg
     * @return
     */
    public static String stripColors(String msg)
    {
        String out = msg.replaceAll("[&][0-9a-f]", "");
        out = out.replaceAll(String.valueOf((char) 194), "");
        return out.replaceAll("[\u00a7][0-9a-f]", "");
    }

    /**
     * Removes trailing separators
     * @param msg
     * @param sep
     * @return
     */
    public static String stripTrailing(String msg, String sep)
    {
        if (msg.length() < sep.length() * 2)
        {
            return msg;
        }

        String out = msg;
        String first = msg.substring(0, sep.length());
        String last = msg.substring(msg.length() - sep.length(), msg.length());

        if (first.equals(sep))
        {
            out = msg.substring(sep.length());
        }

        if (last.equals(sep))
        {
            out = msg.substring(0, msg.length() - sep.length());
        }

        return out;
    }

    /**
     * Hex value to ChatColor
     * @param hexValue
     * @return
     */
    public static String toColor(String hexValue)
    {
        return ChatColor.getByCode(Integer.valueOf(hexValue, 16)).toString();
    }

    /**
     * Converts string array to ArrayList<String>, remove empty strings
     * @param values
     * @return
     */
    public static List<String> fromArray(String... values)
    {
        List<String> results = new ArrayList<String>();
        Collections.addAll(results, values);
        results.remove("");
        return results;
    }

    /**
     * Converts string array to HashSet<String>, remove empty strings
     * @param values
     * @return
     */
    public static HashSet<String> fromArray2(String... values)
    {
        HashSet<String> results = new HashSet<String>();
        Collections.addAll(results, values);
        results.remove("");
        return results;
    }

    /**
     * Converts a player array to ArrayList<Player>
     * @param values
     * @return
     */
    public static List<Player> fromPlayerArray(Player... values)
    {
        List<Player> results = new ArrayList<Player>();
        Collections.addAll(results, values);
        return results;
    }

    /**
     * Converts List<String> to string array
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String[] toArray(List<String> list)
    {
        return list.toArray(new String[0]);
    }

    /**
     * Removes first item from a string array
     * @param args
     * @return
     */
    public static String[] removeFirst(String[] args)
    {
        List<String> out = fromArray(args);

        if (!out.isEmpty())
        {
            out.remove(0);
        }
        return toArray(out);
    }

    /**
     * Converts a string array to a space separated string
     * @param args
     * @return
     */
    public static String toMessage(String[] args)
    {
        String out = "";

        for (String arg : args)
        {
            out += arg + " ";
        }

        return out.trim();
    }

    /**
     * Converts a string array to a string with custom separators
     * @param args
     * @param sep
     * @return
     */
    public static String toMessage(String[] args, String sep)
    {
        String out = "";

        for (String arg : args)
        {
            out += arg + sep;
        }

        return stripTrailing(out, sep);
    }

    /**
     * Converts a string array to a string with custom separators
     * @param args
     * @param sep
     * @return
     */
    public static String toMessage(List<String> args, String sep)
    {
        String out = "";

        for (String arg : args)
        {
            out += arg + sep;
        }

        return stripTrailing(out, sep);
    }

    /**
     * Returns a prettier coordinate, does not include world
     * @param loc
     * @return
     */
    public static String toLocationString(Location loc)
    {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

    /**
     * Escapes single quotes
     * @param str
     * @return
     */
    public static String escapeQuotes(String str)
    {
        if (str == null)
        {
            return "";
        }
        return str.replace("'", "''");
    }

    /**
     * Whether the two locations refer to the same block
     * @param loc
     * @param loc2
     * @return
     */
    public static boolean isSameBlock(Location loc, Location loc2)
    {
        if (loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY() && loc.getBlockZ() == loc2.getBlockZ())
        {
            return true;
        }
        return false;
    }

    /**
     * Whether the two locations refer to the same location, ignoring pitch and yaw
     * @param loc
     * @param loc2
     * @return
     */
    public static boolean isSameLocation(Location loc, Location loc2)
    {
        if (loc.getX() == loc2.getX() && loc.getY() == loc2.getY() && loc.getZ() == loc2.getZ())
        {
            return true;
        }
        return false;
    }
}
