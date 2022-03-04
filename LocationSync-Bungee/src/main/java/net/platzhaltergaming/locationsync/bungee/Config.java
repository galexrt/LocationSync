package net.platzhaltergaming.locationsync.bungee;

import com.google.common.io.ByteStreams;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class Config {

    private final Plugin plugin;
    @Getter
    private Configuration config;

    public Config(Plugin plugin) {
        this.plugin = plugin;
    }

    public void saveDefault() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) { // Simply save default config into datafolder
            this.plugin.getDataFolder().mkdir();
            try {
                configFile.createNewFile();
                try (InputStream is = this.plugin.getResourceAsStream("config.yml");
                        OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to save default configuration file.");
            }
        } else {
            try {
                update("config.yml", configFile);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save default configuration file.");
            }

        }
    }

    private void update(String newResource, File oldResource) throws IOException {
        Configuration newConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class)
                .load(this.plugin.getResourceAsStream(newResource));
        Configuration oldConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(oldResource);

        List<String> newKeys = newConfiguration.getKeys().stream().filter(key -> !oldConfiguration.contains(key))
                .collect(Collectors.toList());
        newKeys.forEach((key) -> {
            oldConfiguration.set(key, newConfiguration.get(key));
        });

        ConfigurationProvider.getProvider(YamlConfiguration.class).save(oldConfiguration,
                new File(plugin.getDataFolder(), "config.yml"));
    }

    public void load() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            throw new RuntimeException("No config file exists");
        }
        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config file.");
        }
    }

}
