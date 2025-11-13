package dev.jsinco.malts.configuration;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.utility.Text;
import eu.okaeri.configs.OkaeriConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;

public final class NullKeyMerger {

    // TODO: Figure out a better solution to this rather than this messy approach

    /**
     * Merges null values in the config with values from the internal resource file.
     *
     * @param config The config instance to merge defaults into
     * @param bindFile The file path of the config (used to find the internal resource)
     * @param <T> The config type
     * @return true if any values were updated, false otherwise
     */
    public static <T extends OkaeriConfig> boolean mergeWithInternalDefaults(T config, Path bindFile) {
        try {
            String resourcePath = constructResourcePath(bindFile);

            try (InputStream in = Malts.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    return false; // no internal defaults to merge
                }

                Yaml yaml = new Yaml();
                Map<String, Object> internalData = yaml.load(new InputStreamReader(in));

                if (internalData != null) {
                    boolean updated = mergeNullValues(config, internalData);

                    if (updated) {
                        config.save();
                    }

                    return updated;
                }
            }
        } catch (Exception e) {
            Text.error("Failed to merge null keys for config: " + bindFile, e);
        }

        return false;
    }

    /**
     * Constructs the resource path from the bind file path.
     * Assumes resources are organized in the same structure as the data folder.
     *
     * @param bindFile The bind file path
     * @return The resource path (e.g., "/translations/en.yml")
     */
    private static String constructResourcePath(Path bindFile) {
        String resourcePath = "/" + bindFile.getFileName().toString();
        if (bindFile.getParent() != null && bindFile.getParent().getFileName() != null) {
            resourcePath = "/" + bindFile.getParent().getFileName().toString() + "/" + bindFile.getFileName().toString();
        }
        return resourcePath;
    }

    /**
     * Recursively merges null values from defaults into the target object.
     *
     * @param target The target object to merge into
     * @param defaults The default values from the internal resource
     * @return true if any values were updated
     */
    @SuppressWarnings("unchecked")
    private static boolean mergeNullValues(Object target, Map<String, Object> defaults) {
        boolean updated = false;

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String key = entry.getKey();
            Object defaultValue = entry.getValue();

            try {
                Field field = findField(target.getClass(), key);
                if (field == null) continue;

                field.setAccessible(true);
                Object currentValue = field.get(target);

                // If current value is null, set it from defaults
                if (currentValue == null && defaultValue != null) {
                    if (defaultValue instanceof Map) {
                        // For nested configs, create instance and recurse
                        Object nestedConfig = field.getType().getDeclaredConstructor().newInstance();
                        mergeNullValues(nestedConfig, (Map<String, Object>) defaultValue);
                        field.set(target, nestedConfig);
                    } else {
                        field.set(target, defaultValue);
                    }
                    updated = true;
                } else if (currentValue != null && defaultValue instanceof Map && OkaeriConfig.class.isAssignableFrom(field.getType())) {
                    // Recurse into nested configs
                    boolean nestedUpdated = mergeNullValues(currentValue, (Map<String, Object>) defaultValue);
                    updated = updated || nestedUpdated;
                }
            } catch (Exception ignored) {
                // ignore
            }
        }

        return updated;
    }

    /**
     * Finds a field in the class or its superclasses.
     *
     * @param clazz The class to search
     * @param fieldName The name of the field
     * @return The field, or null if not found
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}