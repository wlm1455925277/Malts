package dev.jsinco.malts.utility;

import dev.jsinco.malts.Malts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class FileUtil {


    public static String readInternalResource(String path) {
        ClassLoader loader = Malts.class.getClassLoader();
        try (InputStream inputStream = loader.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + path + " (using classloader: " + loader + ")");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }


    public static File[] listInternalFiles(String path) {
        try {
            URL url = FileUtil.class.getResource(path);
            if (url == null) {
                return new File[0];
            }
            if (url.getProtocol().equals("file")) {
                // Running from IDE or exploded classes
                File dir = new File(url.toURI());
                File[] files = dir.listFiles();
                return files != null ? files : new File[0];
            } else if (url.getProtocol().equals("jar")) {
                // Running from JAR
                String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                    List<File> result = new ArrayList<>();
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().startsWith(path.substring(1) + "/") && !entry.isDirectory()) {
                            result.add(new File(entry.getName()));
                        }
                    }
                    return result.toArray(new File[0]);
                }
            } else {
                return new File[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new File[0];
        }
    }

    public static File getInternalFile(String path) {
        URL url = FileUtil.class.getResource(path);
        if (url == null) {
            return null;
        }
        return new File(url.getFile());
    }
}
