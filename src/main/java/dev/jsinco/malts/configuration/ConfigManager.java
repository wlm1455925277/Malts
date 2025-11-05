package dev.jsinco.malts.configuration;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.configuration.serdes.IntPairTransformer;
import dev.jsinco.malts.registry.Registry;
import dev.jsinco.malts.registry.RegistryCrafter;
import dev.jsinco.malts.utility.FileUtil;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static dev.jsinco.malts.storage.DataSource.DATA_FOLDER;

@Getter
@Accessors(fluent = true)
public class ConfigManager implements RegistryCrafter.Extension<OkaeriConfig>{

    @Override
    public <T extends OkaeriConfig> T craft(Class<?> clazz) {
        OkaeriFileName annotation = clazz.getAnnotation(OkaeriFileName.class);
        if (annotation == null) {
            throw new IllegalStateException("OkaeriFile must be annotated with @OkaeriFileName");
        }

        String fileName = annotation.dynamicFileName() ? dynamicFileName(annotation) : annotation.value();

        return eu.okaeri.configs.ConfigManager.create((Class<T>) clazz, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new StandardSerdes());
            it.withRemoveOrphans(false);
            it.withBindFile(DATA_FOLDER.resolve(fileName));
            it.withSerdesPack(serdes -> {
                serdes.register(new IntPairTransformer());
            });

            it.saveDefaults();
            it.load(true);
        });
    }

    private String dynamicFileName(OkaeriFileName annotation) {
        OkaeriFile config = craft(annotation.dynamicFileNameHolder());
        String value = config.get(annotation.dynamicFileNameKey(), String.class);
        if (value != null) {
            return String.format(annotation.dynamicFileNameFormat(), value);
        }
        return null;
    }

    public static <T extends OkaeriFile> T get(Class<T> clazz) {
        return Registry.CONFIGS.values().stream()
                .filter(it -> it.getClass().equals(clazz))
                .map(it -> (T) it)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No config found for class " + clazz.getName()));
    }

    public static void createTranslationConfigs() {
        Path targetDir = DATA_FOLDER.resolve("translations");
        File[] internalLangs = FileUtil.listInternalFiles("/translations");

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            for (File file : internalLangs) {
                Path targetFile = targetDir.resolve(file.getName());
                if (Files.exists(targetFile)) continue;

                try (InputStream in = Malts.class.getResourceAsStream("/translations/" + file.getName())) {
                    if (in == null) {
                        //Text.debug("Could not find internal translation: " + file.getName());
                        continue;
                    }
                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    //Text.debug("Copied translation: " + file.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        createTranslationConfigs();
    }

}
