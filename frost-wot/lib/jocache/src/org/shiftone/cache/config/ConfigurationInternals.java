package org.shiftone.cache.config;



import org.shiftone.cache.CacheFactory;
import org.shiftone.cache.ConfigurationException;
import org.shiftone.cache.policy.zero.ZeroCacheFactory;
import org.shiftone.cache.util.Log;

import java.util.*;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class ConfigurationInternals
{

    private static final Log LOG       = new Log(ConfigurationInternals.class);
    private Map              factories = new Hashtable();
    Node                     rootNode;

    public ConfigurationInternals(Properties properties) throws ConfigurationException
    {

        PropertiesTree tree = new PropertiesTree(properties);

        rootNode = tree.getRoot();

        Node factoryNode = rootNode.getRequiredNode("factory");
        Node titleNode   = rootNode.getRequiredNode("title");
        Node versionNode = rootNode.getRequiredNode("version");
        List factoryList = new ArrayList(factoryNode.getChildren());

        LOG.info(titleNode.getValue() + " v" + versionNode.getValue());
        rootNode.print();

        // pass one (create stage)
        for (int i = 0; i < factoryList.size(); i++)
        {
            addFactory((Node) factoryList.get(i));
        }

        // pass two - set property stage
        for (int i = 0; i < factoryList.size(); i++)
        {
            setFactoryProperties((Node) factoryList.get(i));
        }
    }


    public String getConfiguredCacheProperty(String property, String cacheName) throws ConfigurationException
    {

        String[] cacheNameTokens = PropertiesTree.tokenize(cacheName);
        Node     node            = rootNode.getRequiredNode("cache").getRequiredNode(property);
        String   value           = node.getValue();

        for (int i = 0; i < cacheNameTokens.length; i++)
        {
            LOG.debug(i + " => " + cacheNameTokens[i]);

            if (node.hasNode(cacheNameTokens[i]))
            {
                node = node.getNode(cacheNameTokens[i]);

                if (node.getValue() != null)
                {
                    value = node.getValue();
                }
            }
            else
            {
                break;
            }
        }

        return value;
    }


    private void addFactory(Node factoryNode) throws ConfigurationException
    {

        String       name             = factoryNode.getKey();
        String       factoryClassName = factoryNode.getValue();
        Class        factoryClass;
        CacheFactory cacheFactory;

        try
        {
            factoryClass = Class.forName(factoryClassName);
            cacheFactory = (CacheFactory) factoryClass.newInstance();

            factories.put(name, cacheFactory);
        }
        catch (Exception e)
        {
            throw new ConfigurationException("unable to create factory : " + name + "=" + factoryClassName, e);
        }
    }


    private void setFactoryProperties(Node factoryNode) throws ConfigurationException
    {

        String       name         = factoryNode.getKey();
        CacheFactory cacheFactory = getFactory(name);
        BeanWrapper  wrapper      = new BeanWrapper(cacheFactory);
        Iterator     iterator     = factoryNode.getChildren().iterator();
        Node         node;

        while (iterator.hasNext())
        {
            node = (Node) iterator.next();

            setFactoryProperty(name, wrapper, node.getKey(), node.getValue());
        }

        LOG.debug(name + " => " + cacheFactory);
    }


    private void setFactoryProperty(String cacheName, BeanWrapper factoryWrapper, String name, String value) throws ConfigurationException
    {

        CacheFactory factory;

        try
        {
            Class type = factoryWrapper.getType(name);

            if (CacheFactory.class.isAssignableFrom(type))
            {
                factory = getFactory(value);

                if (factory == null)
                {
                    LOG.warn("factory '" + value + "' is not defined.  Property '" + name + "' can not be set on '" + cacheName + "'.  Setting zero cache factory instead.");

                    factory = ZeroCacheFactory.NULL_CACHE_FACTORY;
                }

                factoryWrapper.setProperty(name, factory);
            }
            else
            {
                factoryWrapper.setProperty(name, value);
            }
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e);
        }
    }


    public CacheFactory getFactory(String name)
    {
        return (CacheFactory) factories.get(name);
    }
}
