package org.shiftone.cache.config;



import org.shiftone.cache.ConfigurationException;
import org.shiftone.cache.util.Log;

import java.io.PrintStream;
import java.util.*;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class PropertiesTree
{

    private static final Log LOG  = new Log(PropertiesTree.class);
    private Node             root = null;

    /**
     * Constructor PropertiesTree
     */
    public PropertiesTree() throws ConfigurationException
    {
        initialize(new Properties());
    }


    /**
     * Constructor PropertiesTree
     */
    public PropertiesTree(Properties properties) throws ConfigurationException
    {
        initialize(properties);
    }


    /**
     * Method initialize
     */
    private void initialize(Properties properties) throws ConfigurationException
    {

        this.root = new Node("ROOT", null);

        Enumeration keys = properties.keys();

        while (keys.hasMoreElements())
        {
            String key   = (String) keys.nextElement();
            String value = properties.getProperty(key);

            root.createNode(key, value);
        }
    }


    /**
     * Regenerates a Properties object based on the tree.
     */
    public Properties getProperties()
    {

        Properties properties = new Properties();

        populateProperties(properties, root, "");

        return properties;
    }


    /**
     * Method populateProperties
     */
    private void populateProperties(Properties properties, Node node, String prefix)
    {

        Collection children = node.getChildren();
        Iterator   i        = children.iterator();

        while (i.hasNext())
        {
            Node   child = (Node) i.next();
            String key   = prefix + child.getKey();
            String value = child.getValue();

            if (value != null)
            {
                properties.setProperty(key, value);
            }

            populateProperties(properties, child, key + ".");
        }
    }


    /**
     * Method getRoot
     *
     * @return .
     */
    public Node getRoot()
    {
        return root;
    }


    /**
     * Prints out the tree in a cheezy XML like format
     */
    public void print(PrintStream out)
    {
        root.print();
    }


    public static String[] tokenize(String text)
    {
        return tokenize(text, ".");
    }


    public static String[] tokenize(String text, String token)
    {

        StringTokenizer tokenizer = new StringTokenizer(text, token, false);
        List            list      = new ArrayList(5);
        String[]        array;

        while (tokenizer.hasMoreTokens())
        {
            list.add(tokenizer.nextToken());
        }

        array = new String[list.size()];
        array = (String[]) list.toArray(array);

        return array;
    }
}
