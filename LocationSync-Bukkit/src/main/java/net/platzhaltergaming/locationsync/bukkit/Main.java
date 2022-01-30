package net.platzhaltergaming.locationsync.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import me.drepic.proton.common.ProtonManager;
import me.drepic.proton.common.ProtonProvider;
import net.platzhaltergaming.locationsync.bukkit.modules.LocationSyncModule;

public class Main extends JavaPlugin {

    private ProtonManager proton;

    private LocationSyncModule locationSyncModule;

    public void onEnable() {
        // Base Setup (external APIs, etc.)
        this.proton = ProtonProvider.get();

        // Modules
        locationSyncModule = new LocationSyncModule(this, this.proton);
        locationSyncModule.onEnable();
    }

    public void onDisable() {
        locationSyncModule.onDisable();
    }

}
