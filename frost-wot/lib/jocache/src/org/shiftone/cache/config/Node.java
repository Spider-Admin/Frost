package org.shiftone.cache.config;



import org.shiftone.cache.ConfigurationException;
import org.shiftone.cache.util.Log;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class Node
{

    private static final Log LOG      = new Log(Node.class);
    private Map              children = new HashMap();
    private String           key      = null;
    private String           value    = null;

    /**
     * Constructor StackNode
     */
    public Node(String key, String value)
    {
        this.value = value;
        this.key   = key;
    }


    /**
     * Method getKey
     */
    public String getKey()
    {
        return key;
    }


    /**
     * Method getValue
     */
    public String getValue()
    {
        return value;
    }


    /**
     * Method getChildren
     */
    public Collection getChildren()
    {
        return children.values();
    }


    public boolean hasNode(String key)
    {
        return getNode(key) != null;
    }


    public Node getRequiredNode(String key) throws ConfigurationException
    {

        Node node = getNode(key);

        if (node == null)
        {
            throw new ConfigurationException("node not found : " + key);
        }

        return node;
    }


    public Node getNode(String key)
    {
        return getNodeInternal(PropertiesTree.tokenize(key), 0);
    }


    /**
     * Method getNode
     */
    private Node getNodeInternal(String[] keyParts, int partIndex)
    {

        if (keyParts.length == partIndex)
        {
            return this;
        }
        else
        {
            String part      = keyParts[partIndex];
            Node   nextChild = (Node) children.get(part);

            if (nextChild == null)
            {
                LOG.debug("node not found : " + fullKey(keyParts, partIndex));

                return null;
            }

            return nextChild.getNodeInternal(keyParts, partIndex + 1);
        }
    }


    /**
     * Method fullKey
     */
    private String fullKey(String[] keyParts, int partIndex)
    {

        String fullKey = keyParts[0];

        for (int i = 1; i < partIndex; i++)
        {
            fullKey += ("." + keyParts[i]);
        }

        return fullKey;
    }


    /**
     * Method createNode
     */
    public void createNode(String key, String value)
    {
        createNode(PropertiesTree.tokenize(key, "."), 0, value);
    }


    /**
     * Method createNode
     */
    private void createNode(String[] keyParts, int partIndex, String value)
    {

        if (keyParts.length == partIndex)
        {
            this.value = value;
        }
        else
        {
            Node   nextChild = null;
            String part      = keyParts[partIndex];

            if (children.containsKey(part))
            {
                nextChild = (Node) children.get(part);
            }
            else
            {
                nextChild = new Node(part, null);

                children.put(part, nextChild);
            }

            nextChild.createNode(keyParts, partIndex + 1, value);
        }
    }


    public void print()
    {
        print(System.out, 0);
    }


    /**
        * Method print
        */
    private void print(PrintStream out, int indentLevel)
    {

        Collection children = getChildren();

        if (children.size() == 0)
        {
            out.println(bufferString(indentLevel, '\t') + "<" + getKey() + " value=\"" + getValue() + "\"/>");
        }
        else
        {
            out.println(bufferString(indentLevel, '\t') + "<" + getKey() + " value=\"" + getValue() + "\">");

            Iterator i = children.iterator();

            while (i.hasNext())
            {
                ((Node) i.next()).print(out, indentLevel + 1);
            }

            out.println(bufferString(indentLevel, '\t') + "</" + getKey() + ">");
        }
    }


    private static String bufferString(int indentLevel, char c)
    {

        StringBuffer sb = new StringBuffer(indentLevel);

        for (int i = 0; i < indentLevel; i++)
        {
            sb.append(c);
        }

        return sb.toString();
    }
}
