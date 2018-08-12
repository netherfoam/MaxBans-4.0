package org.maxgamer.maxbans.service;

import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.maxgamer.maxbans.PluginContextTest;
import org.maxgamer.maxbans.config.WarningConfig;
import org.maxgamer.maxbans.event.BanAddressEvent;
import org.maxgamer.maxbans.event.WarnUserEvent;
import org.maxgamer.maxbans.exception.CancelledException;
import org.maxgamer.maxbans.locale.Locale;
import org.maxgamer.maxbans.orm.User;
import org.maxgamer.maxbans.test.IntegrationTest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author netherfoam
 */
public class WarningServiceTest extends PluginContextTest implements IntegrationTest {
    private PluginManager pluginManager;

    @Before
    public void setup() {
        pluginManager = getContext().getPluginModule().getPluginManager();
    }

    @Test
    public void testWarn() throws CancelledException {
        WarningService service = getContext().components().services().warn();

        WarningConfig config = getContext().getConfig().getWarningConfig();
        config.setDuration(Duration.ofMinutes(30));
        config.setPenalties(1, "/strike {{name}}");

        User user = getContext().components().services().user().create(UUID.randomUUID(), "JoeBlogs", Instant.now());

        Locale locale = new Locale();
        locale.put("warn.broadcast", "broadcast message");

        service.warn(null, user, "Warning", locale);
        verify(getContext().getServer(), times(1)).dispatchCommand(any(), eq("strike JoeBlogs"));

        verify(pluginManager, times(1)).callEvent(isA(WarnUserEvent.class));
    }

    @Test
    public void testMultiCommandStrike() throws CancelledException {
        WarningService service = getContext().components().services().warn();

        WarningConfig config = getContext().getConfig().getWarningConfig();
        config.setDuration(Duration.ofMinutes(30));
        config.setPenalties(1, "/strike {{name}}", "/kill {{name}}");

        User user = getContext().components().services().user().create(UUID.randomUUID(), "JoeBlogs", Instant.now());

        Locale locale = new Locale();
        locale.put("warn.broadcast", "broadcast message");

        service.warn(null, user, "Warning", locale);
        verify(getContext().getServer(), times(2)).dispatchCommand(any(), anyString());
    }

    @Test
    public void testWarnReset() throws CancelledException {
        WarningService service = getContext().components().services().warn();

        WarningConfig config = getContext().getConfig().getWarningConfig();
        config.setDuration(Duration.ofMinutes(30));
        config.setPenalties(1, "/strike {{name}}");
        config.setStrikes(2);

        User user = getContext().components().services().user().create(UUID.randomUUID(), "JoeBlogs", Instant.now());

        Locale locale = new Locale();
        locale.put("warn.broadcast", "broadcast message");

        service.warn(null, user, "First", locale);
        service.warn(null, user, "Second", locale);
        service.warn(null, user, "Third", locale);

        // We shouldn't generate a command for the second command, but the first and third should.
        verify(getContext().getServer(), times(2)).dispatchCommand(any(), eq("strike JoeBlogs"));
    }
}
