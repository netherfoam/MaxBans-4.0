package org.maxgamer.maxbans.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.exception.MessageException;
import org.maxgamer.maxbans.exception.RejectedException;
import org.maxgamer.maxbans.locale.Message;
import org.maxgamer.maxbans.orm.User;
import org.maxgamer.maxbans.service.HistoryService;
import org.maxgamer.maxbans.service.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Executor that provides the /history command
 *
 * @author netherfoam
 */
public class HistoryCommandExecutor extends StandardCommandExecutor {
    @Inject
    protected UserService userService;

    @Inject
    protected HistoryService historyService;

    @Inject
    public HistoryCommandExecutor() {
        super("maxbans.history");
    }

    @Override
    public void perform(CommandSender sender, Command command, String cmd, String[] userArgs) throws MessageException {
        LinkedList<String> args = new LinkedList<>(Arrays.asList(userArgs));

        List<Message> messages;

        int page = 0;
        if (!args.isEmpty()) {
            // Extract the user to view history of
            String banner = args.remove(0);

            User user;
            if (banner.equalsIgnoreCase("console")) {
                user = null;
            } else {
                user = userService.get(banner + "%");

                if (user == null) {
                    throw new RejectedException("User " + banner + " doesn't exist");
                }
            }

            if (!args.isEmpty()) {
                String pageString = args.remove(0);

                try {
                    // Minus one, because humans use 1 indexed pages, but we don't.
                    page = Integer.parseInt(pageString) - 1;

                    if (page < 0) {
                        throw new RejectedException("Page number must be >= 1");
                    }
                } catch (NumberFormatException e) {
                    throw new RejectedException(pageString + " is not a suitable page number");
                }
            }

            messages = historyService.getHistory(page, user);
        } else {
            messages = historyService.getHistory(page);
        }

        for (Message message : messages) {
            message.send(sender);
        }

        sender.sendMessage("--- Page " + (page + 1) + " ---");
    }
}
