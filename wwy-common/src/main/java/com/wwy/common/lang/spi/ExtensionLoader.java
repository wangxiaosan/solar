package com.wwy.common.lang.spi;

import com.wwy.common.lang.utils.ConcurrentHashSet;
import com.wwy.common.lang.utils.Holder;
import com.wwy.common.lang.utils.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @author wangxiaosan
 * @date 2017/10/18
 */
public class ExtensionLoader<T> {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String SERVICES_DIRECTORY = "META-INF/extensions/";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    public static final String DEFAULT_EXTENSION_NAME = "true";

    // ==============================

    private final Class<?> type;

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private String cachedDefaultName;

    private Set<Class<?>> cachedWrapperClasses;

    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
	        throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <S> S getDefaultExtension(Class<S> type, Object... args) {
        return getExtension(type, DEFAULT_EXTENSION_NAME, args);
    }

    public static <S> S getExtension(Class<S> type, String name, Object... args) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<S> loader = ExtensionLoader.getExtensionLoader(type);
            if (null != loader) {
                return loader.getExtension(name, args);
            }
        }
        return null;
    }

    /**
     * 返回指定名字的扩展。如果指定名字的扩展不存在，则抛异常 {@link IllegalStateException}.
     *
     * @param name
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name, Object... args) {
        if (name == null || name.length() == 0 || DEFAULT_EXTENSION_NAME.equals(name)) {
            return getDefaultExtension(args);
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name, args);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 返回缺省的扩展，如果没有设置则返回<code>null</code>。
     */
    public T getDefaultExtension(Object... args) {
        getExtensionClasses();
        if (null == cachedDefaultName || cachedDefaultName.length() == 0
                || DEFAULT_EXTENSION_NAME.equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName, args);
    }

    /**
     * 是否有该名称的扩展
     *
     * @param name
     * @return
     */
    public boolean hasExtension(String name) {
        if (name == null || name.length() == 0) {
	        throw new IllegalArgumentException("Extension name == null");
        }
        try {
            getExtensionClass(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private Class<?> getExtensionClass(String name) {
        if (type == null) {
	        throw new IllegalArgumentException("Extension type == null");
        }
        if (name == null) {
	        throw new IllegalArgumentException("Extension name == null");
        }
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
	        throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
        }
        return clazz;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

	/**
	 *  此方法已经getExtensionClasses方法同步过。
	 * @return
	 */
	private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if (value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1) {
                	cachedDefaultName = names[0];
                }
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                final int ci = line.indexOf('#');
                                if (ci >= 0) {
                                	line = line.substring(0, ci);
                                }
                                line = line.trim();
                                if (line.length() > 0) {
                                    try {
                                        String name = null;
                                        int i = line.indexOf('=');
                                        if (i > 0) {
                                            name = line.substring(0, i).trim();
                                            line = line.substring(i + 1).trim();
                                        }
                                        if (line.length() > 0) {
                                            Class<?> clazz = Class.forName(line, true, classLoader);
                                            if (!type.isAssignableFrom(clazz)) {
                                                throw new IllegalStateException("Error when load extension class(interface: " +
                                                        type + ", class line: " + clazz.getName() + "), class "
                                                        + clazz.getName() + "is not subtype of interface.");
                                            }
                                            Set<Class<?>> wrappers = cachedWrapperClasses;
                                            if (wrappers == null) {
                                                cachedWrapperClasses = new ConcurrentHashSet<>();
                                                wrappers = cachedWrapperClasses;
                                            }
                                            wrappers.add(clazz);
                                            extensionClasses.put(name, clazz);
                                        }
                                    } catch (Throwable t) {
                                        IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                        exceptions.put(line, e);
                                    }
                                }
                            } // end of while read lines
                        } finally {
                            reader.close();
                        }
                    } catch (Throwable t) {
                        logger.error("Exception when load extension class(interface: " +
                                type + ", class file: " + url + ") in " + url, t);
                    }
                } // end of while urls
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private static ClassLoader findClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private T createExtension(String name, Object... args) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                Constructor constructor = getMatchingConstructor(clazz, args);
                EXTENSION_INSTANCES.putIfAbsent(clazz, (T) constructor.newInstance(args));
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    private Constructor getMatchingConstructor(Class clazz, Object... args) {
        Constructor[] constructors = clazz.getConstructors();
        if (1 == constructors.length) {
            return constructors[0];
        }
        for (int i = 0; i < constructors.length; i++) {
            if (args.length == constructors[i].getParameters().length) {
                boolean isMatching = true;
                for (int j = 0; j < constructors[i].getParameters().length; j++) {
                    if (null != args[j] && !args[j].getClass().equals(constructors[i].getParameters()[j].getType())) {
                        isMatching = false;
                    }
                }
                if (isMatching) {
                    return constructors[i];
                }
            }
        }
        throw new IllegalStateException("Constructor not found");
    }

    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);


        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(StringHelper.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }
}
