package com.wwy.common.lang;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangxiaosan
 * @date 2018/03/07
 *
 *  * 读取配置文件。
 * 首先读取classpath下的所有jar包里面conf文件夹下的所有属性文件；
 * 然后读取当前项目下的conf文件夹下的所有属性文件，同名的属性会覆盖；
 * 最后读取本地文件路径相对项目前一个路径下的conf文件夹下的所有属性文件，同名的属性会覆盖。
 */
public class Configs {
    private static final String PROPERTIES_CLASSPATH_PATH = "conf/";

    private static final String PROPERTIES_FILE_POSTFIX = "yml";

    private static final String DEFAULT_CONFIG_FILE = "application.yml";

    private static Properties config = new Properties();

    public static Map<String, Object> configData = new LinkedHashMap<>();

    static {
        load();
    }

    public static void load() {
        try {
            config.clear();

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            List<URL> resourceList = new ArrayList<>();
            URL resource;
            //加载所有jar包下的配置文件
            Enumeration<URL> resources = contextClassLoader.getResources(PROPERTIES_CLASSPATH_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                resourceList.add(url);
            }
            addResourceBundles(resourceList);

            //加载当前jar包下的配置文件
            resource = contextClassLoader.getResource(PROPERTIES_CLASSPATH_PATH);
            resourceList.clear();
            resourceList.add(resource);
            addResourceBundles(resourceList);

            //加载用户路径下的配置文件
            String relativelyPath = System.getProperty("user.dir");
            File path = new File(relativelyPath, PROPERTIES_CLASSPATH_PATH);
            resource = path.toURI().toURL();
            resourceList.clear();
            resourceList.add(resource);
            addResourceBundles(resourceList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String fileContent) {
        String relativelyPath = System.getProperty("user.dir");
        File path = new File(relativelyPath, PROPERTIES_CLASSPATH_PATH);
        try {
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, DEFAULT_CONFIG_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            Files.write(Paths.get(relativelyPath, PROPERTIES_CLASSPATH_PATH, DEFAULT_CONFIG_FILE), fileContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String dumpString() {
        try {
            return new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir"), PROPERTIES_CLASSPATH_PATH, DEFAULT_CONFIG_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void updateConfigFile() {
        Yaml yaml = new Yaml();
        String fileContent = yaml.dumpAsMap(configData);
        writeFile(fileContent);
    }

    private static void addResourceBundles(List<URL> resources) {
        for (URL url : resources) {
            if (null == url) {
                continue;
            }
            if ("jar".equals(url.getProtocol())) {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
                JarFile jarFile = null;
                try {
                    URLConnection conn = url.openConnection();
                    JarURLConnection jarCon = (JarURLConnection) conn;
                    jarFile = jarCon.getJarFile();
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                        JarEntry entry = entries.nextElement();
                        String entryPath = entry.getName();
                        int lastIndexOf = entryPath.lastIndexOf(".");
                        if (lastIndexOf > 0) {
                            String postfix = entryPath.substring(lastIndexOf + 1);
                            if (entryPath.startsWith(PROPERTIES_CLASSPATH_PATH) && PROPERTIES_FILE_POSTFIX.equals(postfix)) {
                                InputStream resource = classLoader.getResourceAsStream(entryPath);
                                if (null != resource) {
                                    Yaml yaml = new Yaml();
                                    Map<String, Object> load = (Map<String, Object>) yaml.load(resource);
                                    configData.putAll(load);
                                    Map<String, Object> hashMap = asMap(load);
                                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                                    buildFlattenedMap(result, hashMap, null);
                                    parseEnvExprs(result);
                                    config.putAll(result);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != jarFile) {
                        try {
                            jarFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                File path = new File(url.getPath());
                if (path.exists() && path.isDirectory()) {
                    File[] files = path.listFiles();
                    if (null != files && files.length > 0) {
                        for (File file : files) {
                            String fileName = file.getName();
                            int lastIndexOf = fileName.lastIndexOf(".");
                            if (lastIndexOf > 0) {
                                String postfix = fileName.substring(lastIndexOf + 1);
                                if (PROPERTIES_FILE_POSTFIX.equals(postfix)) {
                                    Yaml yaml = new Yaml();
                                    try {
                                        FileInputStream fileInputStream = new FileInputStream(file);
                                        Map<String, Object> load = (Map<String, Object>) yaml.load(fileInputStream);
                                        configData.putAll(load);
                                        Map<String, Object> hashMap = asMap(load);
                                        Map<String, Object> result = new LinkedHashMap<String, Object>();
                                        buildFlattenedMap(result, hashMap, null);
                                        parseEnvExprs(result);
                                        config.putAll(result);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void parseEnvExprs(Map<String, Object> load) {
        for (String key : load.keySet()) {
            Object value = load.get(key);
            if (value instanceof String) {
                String str = (String) value;
                str = parseEnvExpr(str);
                load.put(key, str);
            }
        }
    }

    /**
     * 处理字符串表达式${}使用环境变量的值替换
     *
     * @param str
     * @return
     */
    public static String parseEnvExpr(String str) {
        Pattern pattern = Pattern.compile("\\$\\{(\\w+.?\\w+)+\\}");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String group = matcher.group();
            String var = group.substring(2, group.length() - 1);
            //先从环境变量中查找，找到了替换；如果没有找到，再在系统属性中查找，找到了替换
            String env = System.getenv(var);
            if (Objects.nonNull(env)) {
                str = str.replace(group, env);
            } else {
                env = System.getProperty(var);
                if (Objects.nonNull(env)) {
                    str = str.replace(group, env);
                }
            }
        }
        return str;
    }

    private static Map<String, Object> asMap(Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            Object key = entry.getKey();
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        }
        return result;
    }

    private static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isNotBlank(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + "." + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                result.put(key, value);
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                result.put(key, value);
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result,
                            Collections.singletonMap("[" + (count++) + "]", object), key);
                }
            } else {
                result.put(key, value == null ? "" : value);
            }
        }
    }

    public static Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        Boolean value = (Boolean) getObject(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    public static Integer getInt(String key) {
        return getInt(key, null);
    }

    public static Integer getInt(String key, Integer defaultValue) {
        Integer value = (Integer) getObject(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }


    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String defaultValue) {
        String value = (String) getObject(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    public static Object getObject(String key) {
        return config.get(key);
    }

    public static void setObject(String key, Object value) {
        StringTokenizer stringTokenizer = new StringTokenizer(key, ".");
        Object data = configData;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (token.contains("[")) {
                StringTokenizer stringTokenizer1 = new StringTokenizer(token, "[\n]");
                List<String> tk = Lists.newArrayList();
                while (stringTokenizer1.hasMoreTokens()) {
                    tk.add(stringTokenizer1.nextToken());
                }
                if (!stringTokenizer.hasMoreTokens()) {
                    ((List<Object>) ((Map<String, Object>) data).get(tk.get(0))).set(Integer.parseInt(tk.get(1)), value);
                } else {
                    data = ((List<Object>) ((Map<String, Object>) data).get(tk.get(0))).get(Integer.parseInt(tk.get(1)));
                }
            } else {
                if (!stringTokenizer.hasMoreTokens()) {
                    ((Map<String, Object>) data).put(token, value);
                } else {
                    data = ((Map<String, Object>) data).get(token);
                }
            }
        }

        Map<String, Object> hashMap = asMap(configData);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        buildFlattenedMap(result, hashMap, null);
        parseEnvExprs(result);
        config.putAll(result);
    }

    public static Properties getStringValueConfig() {
        Properties properties = new Properties();
        Set<Object> keySet = config.keySet();
        for (Object key : keySet) {
            properties.put(key, String.valueOf(config.get(key)));
        }
        return properties;
    }
}
