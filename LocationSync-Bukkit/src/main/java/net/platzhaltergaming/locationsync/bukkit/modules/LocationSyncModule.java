package net.platzhaltergaming.locationsync.bukkit.modules;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.drepic.proton.common.ProtonManager;
import me.drepic.proton.common.message.MessageAttributes;
import me.drepic.proton.common.message.MessageHandler;
import net.platzhaltergaming.locationsync.common.requests.Common;
import net.platzhaltergaming.locationsync.common.requests.PlayerLocationRequest;

@Getter
@RequiredArgsConstructor
public class LocationSyncModule implements Listener {

    private final JavaPlugin plugin;
    private final ProtonManager proton;

    private Cache<UUID, Location> locations;

    public void onEnable() {
        this.locations = Caffeine.newBuilder().maximumSize(plugin.getServer().getMaxPlayers())
                .expireAfterWrite(7, TimeUnit.SECONDS).build();

        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        this.proton.registerMessageHandlers(this);
    }

    public void onDisable() {
    }

    @MessageHandler(namespace = Common.NAMESPACE, subject = PlayerLocationRequest.SUBJECT)
    public void onPlayerLocationRequest(PlayerLocationRequest request, MessageAttributes attributes) {
        World world = getPlugin().getServer().getWorld("world");
        locations.put(request.getUniqueId(), new Location(world, request.getX(), request.getY(), request.getZ(),
                request.getYaw(), request.getPitch()));
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Location location = locations.getIfPresent(player.getUniqueId());
        if (location != null) {
            getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                player.teleport(location);
            }, 1L);
        }
    }

}
