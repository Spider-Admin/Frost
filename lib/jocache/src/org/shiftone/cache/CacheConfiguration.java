package org.shiftone.cache;



import org.shiftone.cache.config.ConfigurationInternals;
import org.shiftone.cache.policy.zero.ZeroCacheFactory;
import org.shiftone.cache.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class CacheConfiguration
{

    private static final Log       LOG                  = new Log(CacheConfiguration.class);
    private static final String    DEFAULT_CACHE_CONFIG = "cache.properties";
    private ConfigurationInternals internals;

    /**
     * create a default cache configuration
     */
    public CacheConfiguration() throws ConfigurationException
    {
        this(new String[]{ DEFAULT_CACHE_CONFIG });
    }


    public CacheConfiguration(String fileName) throws ConfigurationException
    {
        this(new String[]{ DEFAULT_CACHE_CONFIG, fileName });
    }


    public CacheConfiguration(String[] fileNames) throws ConfigurationException
    {

        Properties properties = new Properties();

        for (int i = 0; i < fileNames.length; i++)
        {
            init(properties, fileNames[i]);
        }

        internals = new ConfigurationInternals(properties);
    }


    public CacheConfiguration(Properties properties) throws ConfigurationException
    {
        internals = new ConfigurationInternals(properties);
    }


    public void init(Properties properties, String fileName) throws ConfigurationException
    {

        File        file;
        InputStream inputStream = null;

        try
        {
            file = new File(fileName);

            if (file.isFile())
            {
                inputStream = new FileInputStream(fileName);

                LOG.info("file: " + file.getAbsolutePath());
            }
            else
            {
                inputStream = getClass().getResourceAsStream(fileName);

                LOG.info("resource: " + fileName);
            }

            properties.load(inputStream);
        }
        catch (Throwable e)
        {
            throw new ConfigurationException(e);
        }
        finally
        {
            close(inputStream);
        }
    }


    private void close(InputStream inputStream)
    {

        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            }
            catch (Throwable e) {}
        }
    }


    /**
     * Obtain a configured cache factory by it's name.  If no factory
     * exists by this name, a ConfigurationException is thrown.
     */
    public CacheFactory getCacheFactory(String factoryName) throws ConfigurationException
    {

        CacheFactory cacheFactory = null;

        cacheFactory = internals.getFactory(factoryName);

        if (cacheFactory == null)
        {
            throw new ConfigurationException("cache factory not configured : " + cacheFactory);
        }

        return cacheFactory;
    }


    /**
     * Create a new cache, using the configured values for the
     * factory, timeout, and maxSize.
     */
    public Cache createConfiguredCache(String cacheName) throws ConfigurationException
    {

        CacheFactory factory = getConfiguredFactoryForCache(cacheName);
        long         timeout = getConfiguredTimeoutForCache(cacheName);
        int          maxSize = getConfiguredMaxSizeForCache(cacheName);

        return factory.newInstance(cacheName, timeout, maxSize);
    }


    /**
     * Create a new cache by looking up the configured factory, and then using supplied
     * name, timeout and max size.  Method requested by Neville.
     */
    public Cache createConfiguredCache(String cacheName, long timeout, int maxSize) throws ConfigurationException
    {

        CacheFactory factory = getConfiguredFactoryForCache(cacheName);

        return factory.newInstance(cacheName, timeout, maxSize);
    }


    /**
     * Attempt to create a configured cache, as in createConfiguredCache, except if
     * an error occures, a "zero cache" will be returned.  In other words, any exception
     * is supressed, and the failure is hidden from the application (except there won't
     * be any caching).
     */
    public Cache createConfiguredCacheSafely(String cacheName)
    {

        try
        {
            return createConfiguredCache(cacheName);
        }
        catch (Exception e)
        {
            LOG.error("error with configuration for cache : " + cacheName, e);

            return ZeroCacheFactory.NULL_CACHE;
        }
    }


    public CacheFactory getConfiguredFactoryForCache(String cacheName) throws ConfigurationException
    {

        String factoryName = internals.getConfiguredCacheProperty("factory", cacheName);

        return getCacheFactory(factoryName);
    }


    public long getConfiguredTimeoutForCache(String cacheName) throws ConfigurationException
    {

        String timeout = internals.getConfiguredCacheProperty("timeout", cacheName);

        return Long.parseLong(timeout);
    }


    public int getConfiguredMaxSizeForCache(String cacheName) throws ConfigurationException
    {

        String maxsize = internals.getConfiguredCacheProperty("maxsize", cacheName);

        return Integer.parseInt(maxsize);
    }


    public static void main(String[] args) throws Exception
    {

        try
        {
            System.out.println("test");

            CacheConfiguration config = new CacheConfiguration();
            CacheFactory       factory;

            factory = config.getCacheFactory("lru");
            factory = config.getCacheFactory("missTest");
            factory = config.getCacheFactory("statLru");
            factory = config.getCacheFactory("softLfu");

            Cache cache = config.createConfiguredCache("com.indemand.royalty.organization.Channel");

            LOG.info("cache = " + cache);

            // factory.newInstance("test", 100, 100);
            LOG.info(factory.newInstance("xxx", 1, 2));
        }
        catch (Throwable e)
        {
            LOG.error("main", e);
        }
    }
}
