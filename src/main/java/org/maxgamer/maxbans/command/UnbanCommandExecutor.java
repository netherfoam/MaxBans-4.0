package org.maxgamer.maxbans.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.exception.CancelledException;
import org.maxgamer.maxbans.exception.PermissionException;
import org.maxgamer.maxbans.exception.RejectedException;
import org.maxgamer.maxbans.locale.MessageBuilder;
import org.maxgamer.maxbans.orm.Address;
import org.maxgamer.maxbans.orm.User;
import org.maxgamer.maxbans.service.BroadcastService;
import org.maxgamer.maxbans.service.UserService;
import org.maxgamer.maxbans.service.metric.MetricService;

import javax.inject.Inject;
import java.time.Duration;

/**
 * @author netherfoam
 */
public class UnbanCommandExecutor extends IPRestrictionCommandExecutor {
    @Inject
    protected BroadcastService broadcastService;

    @Inject
    protected UserService userService;

    @Inject
    protected MetricService metricService;

    @Inject
    public UnbanCommandExecutor() {
        super("maxbans.ban");
    }

    @Override
    public void restrict(CommandSender sender, Address address, User user, Duration duration, String reason, boolean silent) throws RejectedException, CancelledException {
        User source = (sender instanceof Player ? userService.getOrCreate((Player) sender) : null);

        MessageBuilder message = locale.get()
                .withUserOrConsole("source", source);

        boolean any = false;
        if(user != null && userService.getBan(user) != null) {
            userService.unban(source, user);
            message.with("name", user);
            broadcastService.broadcast(message.get("ban.unban"), silent, sender);
            any = true;
        }

        if(addressService.getBan(address) != null) {
            addressService.unban(source, address);
            message.with("address", address.getHost());
            broadcastService.broadcast(message.get("ban.unban"), silent, sender);
            any = true;
        }

        if(!any) {
            throw new RejectedException("No ban found");
        }
    }
}
