package frost.identities;

import java.io.Serializable;
import java.util.HashMap;

/**
 * contains the people the local user trusts
 */

public class BuddyList extends HashMap implements Serializable
{
    /**constructor*/
    public BuddyList()
    {
        super(100);  //that sounds like a reasonable number
    }

    /**
     * adds a user to the list
     * returns false if the user exists
     */
    public synchronized boolean Add(Identity user)
    {
        if (containsKey(user.getName()))
        {
            return false;
        }
        else
        {
            put(user.getName(), user);
            return true;
        }
    }

    /**
     * returns the user in the list, null if not in
     */
    public synchronized Identity Get(String name)
    {
        if (containsKey(name))
        {
            return (Identity)get(name);
        }
        else
        {
            return null;
        }
    }
}
