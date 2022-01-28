package net.platzhaltergaming.locationsync.bungee;

import me.drepic.proton.common.ProtonManager;
import me.drepic.proton.common.ProtonProvider;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.platzhaltergaming.locationsync.bungee.modules.LocationSyncModule;

public class Main extends Plugin {

    private Config config;
    private ProtonManager proton;

    private LocationSyncModule locationSyncModule;

    public void onEnable() {
        // Config
        this.config = new Config(this);
        this.config.saveDefault();
        this.config.load();

        // Base Setup (external APIs, etc.)
        this.proton = ProtonProvider.get();

        locationSyncModule = new LocationSyncModule(this, this.proton);
        locationSyncModule.onEnable();
    }

    public void onDisable() {
        locationSyncModule.onDisable();
    }

    public Configuration getConfig() {
        return this.config.getConfig();
    }

}
