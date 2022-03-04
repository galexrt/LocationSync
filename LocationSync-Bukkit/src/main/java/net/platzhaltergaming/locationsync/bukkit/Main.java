package net.platzhaltergaming.locationsync.bukkit;

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

import me.drepic.proton.common.ProtonManager;
import me.drepic.proton.common.ProtonProvider;
import me.drepic.proton.common.message.MessageAttributes;
import me.drepic.proton.common.message.MessageHandler;
import net.platzhaltergaming.locationsync.common.requests.Common;
import net.platzhaltergaming.locationsync.common.requests.PlayerLocationRequest;

public class Main extends JavaPlugin implements Listener {

    private ProtonManager proton;

    private long locationExpireSeconds = 7;

    // Why a cache? Because you don't want to keep player locations around forever
    private Cache<UUID, Location> locations;

    @Override
    public void onEnable() {
        // Base Setup (external APIs, etc.)
        this.proton = ProtonProvider.get();

        this.locations = Caffeine.newBuilder().maximumSize(getServer().getMaxPlayers())
                .expireAfterWrite(this.locationExpireSeconds, TimeUnit.SECONDS).build();

        getServer().getPluginManager().registerEvents(this, this);
        this.proton.registerMessageHandlers(this);
    }

    @MessageHandler(namespace = Common.NAMESPACE, subject = PlayerLocationRequest.SUBJECT)
    public void onPlayerLocationRequest(PlayerLocationRequest request, MessageAttributes attributes) {
        // Save the player location in the cache
        World world = getServer().getWorld("world");
        locations.put(request.getUniqueId(), new Location(world, request.getX(), request.getY(), request.getZ(),
                request.getYaw(), request.getPitch()));
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only teleport the player if a location is in the cache
        Location location = locations.getIfPresent(player.getUniqueId());
        if (location != null) {
            getServer().getScheduler().runTaskLater(this, () -> {
                player.teleport(location);
            }, 1L);
        }
    }

}
