package org.maxgamer.maxbans.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.exception.RejectedException;
import org.maxgamer.maxbans.locale.MessageBuilder;
import org.maxgamer.maxbans.orm.User;
import org.maxgamer.maxbans.service.BroadcastService;
import org.maxgamer.maxbans.service.UserService;
import org.maxgamer.maxbans.service.metric.MetricService;
import org.maxgamer.maxbans.util.TemporalDuration;

import javax.inject.Inject;
import java.time.Duration;

/**
 * @author Dirk Jamieson <dirk@redeye.co>
 */
public class BanCommandExecutor extends UserRestrictionCommandExecutor {
    @Inject
    protected BroadcastService broadcastService;

    @Inject
    protected UserService userService;

    @Inject
    protected MetricService metricService;

    @Inject
    public BanCommandExecutor() {
        super("maxbans.ban");
    }

    @Override
    public void restrict(CommandSender source, User user, Duration duration, String reason, boolean silent) throws RejectedException {
        User banner = (source instanceof Player ? userService.getOrCreate((Player) source) : null);

        userService.ban(banner, user, reason, duration);
        
        MessageBuilder message = locale.get()
                .with("name", user.getName())
                .with("reason", reason)
                .with("source", banner == null ? "Console" : banner.getName())
                .with("duration", TemporalDuration.of(duration));

        Player player = locatorService.player(user);
        if(player != null) player.kickPlayer(message.get("ban.kick"));

        broadcastService.broadcast(message.get("ban.broadcast"), silent, source, player);

        metricService.increment(MetricService.USER_BANS);
    }
}
