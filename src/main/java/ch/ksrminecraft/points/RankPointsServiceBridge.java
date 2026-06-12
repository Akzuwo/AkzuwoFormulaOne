package ch.ksrminecraft.points;

import ch.ksrminecraft.RankPointsAPI.RankPointsService;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

final class RankPointsServiceBridge {

    private final RankPointsService service;

    private RankPointsServiceBridge(RankPointsService service) {
        this.service = service;
    }

    static RankPointsServiceBridge find() {
        RegisteredServiceProvider<RankPointsService> provider =
            Bukkit.getServicesManager().getRegistration(RankPointsService.class);
        if (provider == null || provider.getProvider() == null) {
            return null;
        }
        return new RankPointsServiceBridge(provider.getProvider());
    }

    void addPoints(UUID uuid, int points) {
        service.addPoints(uuid, points);
    }
}
