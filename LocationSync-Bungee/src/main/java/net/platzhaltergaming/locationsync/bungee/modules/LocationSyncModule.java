package net.platzhaltergaming.locationsync.bungee.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.simplix.protocolize.api.Location;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import dev.simplix.protocolize.api.providers.ProtocolizePlayerProvider;
import dev.simplix.protocolize.data.listeners.PlayerPositionLookListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.drepic.proton.common.ProtonManager;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.platzhaltergaming.locationsync.bungee.Main;
import net.platzhaltergaming.locationsync.common.requests.Common;
import net.platzhaltergaming.locationsync.common.requests.PlayerLocationRequest;

@Getter
@RequiredArgsConstructor
public class LocationSyncModule implements Listener {

    private static final ProtocolizePlayerProvider PLAYER_PROVIDER = Protocolize.playerProvider();

    private final Main plugin;
    private final ProtonManager proton;

    private PlayerPositionLookListener playerPositionLookListener;
    private ArrayList<Pattern> enabledServers = new ArrayList<>();

    public void onEnable() {
        Configuration config = getPlugin().getConfig().getSection("locationSync");
        List<?> enabledServers = config.getList("enabledServers");
        enabledServers.forEach((server) -> {
            this.enabledServers.add(Pattern.compile((String) server));
        });

        playerPositionLookListener = new PlayerPositionLookListener();
        Protocolize.listenerProvider().registerListener(playerPositionLookListener);

        getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), this);
    }

    public void onDisable() {
        Protocolize.listenerProvider().unregisterListener(playerPositionLookListener);
    }

    @EventHandler
    public void onServerConnectEvent(ServerConnectEvent event) {
        if (event.getPlayer().getServer() == null || event.getTarget() == null) {
            return;
        }
        // Check if the player is (already) on the same server
        String targetServer = event.getTarget().getName();
        if (event.getPlayer().getServer().getInfo().getName().equals(targetServer)) {
            return;
        }

        if (!checkIfServerIsEnabled(targetServer)) {
            return;
        }

        ProtocolizePlayer player = PLAYER_PROVIDER.player(event.getPlayer().getUniqueId());
        Location location = player.location();

        this.proton.send(Common.NAMESPACE, PlayerLocationRequest.SUBJECT,
                new PlayerLocationRequest(player.uniqueId(), location.x(), location.y(), location.z(), location.yaw(),
                        location.pitch()),
                targetServer);
    }

    protected boolean checkIfServerIsEnabled(String server) {
        for (Pattern pattern : this.enabledServers) {
            Matcher matcher = pattern.matcher(server);
            if (matcher.matches()) {
                return true;
            }
        }

        return false;
    }

}
