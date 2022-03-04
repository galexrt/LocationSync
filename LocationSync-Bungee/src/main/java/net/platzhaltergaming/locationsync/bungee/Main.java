package net.platzhaltergaming.locationsync.bungee;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.simplix.protocolize.api.Location;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import dev.simplix.protocolize.api.providers.ProtocolizePlayerProvider;
import dev.simplix.protocolize.data.listeners.PlayerPositionLookListener;
import me.drepic.proton.common.ProtonManager;
import me.drepic.proton.common.ProtonProvider;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.platzhaltergaming.locationsync.bungee.Main;
import net.platzhaltergaming.locationsync.common.requests.Common;
import net.platzhaltergaming.locationsync.common.requests.PlayerLocationRequest;

public class Main extends Plugin implements Listener {

    private static final ProtocolizePlayerProvider PLAYER_PROVIDER = Protocolize.playerProvider();

    private Config config;
    private ProtonManager proton;

    private PlayerPositionLookListener playerPositionLookListener;
    private ArrayList<Pattern> enabledServers = new ArrayList<>();

    @Override
    public void onEnable() {
        // Config
        this.config = new Config(this);
        this.config.saveDefault();
        this.config.load();

        // Base Setup (external APIs, etc.)
        this.proton = ProtonProvider.get();
        if (getConfig().getBoolean("debug", false)) {
            getLogger().setLevel(Level.FINEST);
        }

        // Location Sync
        List<?> enabledServers = getConfig().getList("enabledServers");
        enabledServers.forEach((server) -> {
            this.enabledServers.add(Pattern.compile((String) server));
        });

        this.playerPositionLookListener = new PlayerPositionLookListener();
        Protocolize.listenerProvider().registerListener(this.playerPositionLookListener);

        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        Protocolize.listenerProvider().unregisterListener(this.playerPositionLookListener);
    }

    public Configuration getConfig() {
        return this.config.getConfig();
    }

    @EventHandler
    public void onServerConnectEvent(ServerConnectEvent event) {
        if (event.getPlayer().getServer() == null || event.getTarget() == null) {
            return;
        }
        // Check if the player is (already) on the same server
        if (event.getPlayer().getServer().getInfo().getName().equals(event.getTarget().getName())) {
            getLogger().fine(String.format("Player %s is already on server %s, not syncing location (again)",
                    event.getPlayer().getName(), event.getTarget().getName()));
            return;
        }

        // Make sure the current and target server are servers were LocationSync is
        // enabled
        if (!checkIfServersAreEnabled(event.getPlayer().getServer().getInfo().getName(), event.getTarget().getName())) {
            getLogger().fine(
                    String.format("Not syncing player %s location to server %s/%s because check came back as disabled",
                            event.getPlayer().getName(), event.getPlayer().getServer().getInfo().getName(),
                            event.getTarget().getName()));
            return;
        }

        // Get Protocolize player data and send the PlayerLocationRequest to the
        // player's target server
        ProtocolizePlayer player = PLAYER_PROVIDER.player(event.getPlayer().getUniqueId());
        if (player == null) {
            return;
        }
        Location location = player.location();

        getLogger().fine(String.format("Syncing player %s location to server %s", player.uniqueId(),
                event.getTarget().getName()));
        this.proton.send(Common.NAMESPACE, PlayerLocationRequest.SUBJECT,
                new PlayerLocationRequest(player.uniqueId(), location.x(), location.y(), location.z(), location.yaw(),
                        location.pitch()),
                event.getTarget().getName());
    }

    protected boolean checkIfServersAreEnabled(String source, String target) {
        for (Pattern pattern : this.enabledServers) {
            Matcher sourceMatch = pattern.matcher(source);
            Matcher targetMatch = pattern.matcher(target);
            if (sourceMatch.matches() && targetMatch.matches()) {
                return true;
            }
        }

        return false;
    }

}
