package org.maxgamer.maxbans.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.maxgamer.maxbans.exception.RejectedException;
import org.maxgamer.maxbans.locale.Locale;
import org.maxgamer.maxbans.orm.User;
import org.maxgamer.maxbans.service.LocatorService;
import org.maxgamer.maxbans.test.UnitTest;
import org.maxgamer.maxbans.transaction.Transactor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

/**
 * @author netherfoam
 */
public class UserRestrictionCommandExecutorTest implements UnitTest {
    /**
     * A subclass for testing
     */
    private static class DummyCommandExecutor extends UserRestrictionCommandExecutor {
        public DummyCommandExecutor(Locale locale, Transactor transactor, LocatorService locatorService, String permission) {
            super(permission);
            this.logger = Logger.getLogger("DummyCommandExecutor");
            this.transactor = transactor;
            this.locatorService = locatorService;
            this.locale = locale;
        }

        @Override
        public void restrict(CommandSender source, User user, Duration duration, String reason, boolean silent) throws RejectedException {

        }
    }

    private static final String PERMISSION = "maxbans.test";

    private Transactor transactor;

    @Before
    public void init() {
        transactor = mock(Transactor.class);
        doAnswer(invocationOnMock -> {
            Transactor.VoidJob work = (Transactor.VoidJob) invocationOnMock.getArguments()[0];
            work.run(null);
            return null;
        }).when(transactor).work(any());

        doAnswer(invocationOnMock -> {
            Transactor.VoidJob work = (Transactor.VoidJob) invocationOnMock.getArguments()[0];
            work.run(null);
            return null;
        }).when(transactor).retrieve(any());
    }

    @Test
    public void testWithReason() throws RejectedException {
        LocatorService locator = mock(LocatorService.class);
        Locale locale = mock(Locale.class);
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        User user = mock(User.class);

        doReturn(user).when(locator).user(any(String.class));
        doReturn(true).when(sender).hasPermission(eq(PERMISSION));

        DummyCommandExecutor executor = spy(new DummyCommandExecutor(locale, transactor, locator, PERMISSION));

        String[] args = "player 5 hours for being rude".split(" ");
        executor.onCommand(sender, command, "dummy", args);

        verify(locator, times(1)).user(any(String.class));
        verify(executor, times(1)).restrict(eq(sender), eq(user), eq(Duration.of(5, ChronoUnit.HOURS)), eq("for being rude"), anyBoolean());
    }

    @Test
    public void testWithoutReason() throws RejectedException {
        LocatorService locator = mock(LocatorService.class);
        Locale locale = mock(Locale.class);
        Command command = mock(Command.class);
        CommandSender sender = mock(CommandSender.class);
        User user = mock(User.class);

        doReturn(user).when(locator).user(any(String.class));
        doReturn(true).when(sender).hasPermission(eq(PERMISSION));

        DummyCommandExecutor executor = spy(new DummyCommandExecutor(locale, transactor, locator, PERMISSION));

        String[] args = "player 5 hours".split(" ");
        executor.onCommand(sender, command, "dummy", args);

        verify(locator, times(1)).user(any(String.class));
        verify(executor, times(1)).restrict(eq(sender), eq(user), eq(Duration.of(5, ChronoUnit.HOURS)), any(String.class), anyBoolean());
    }

    @Test
    public void testWithoutDurationOrReason() throws RejectedException {
        LocatorService locator = mock(LocatorService.class);
        Locale locale = mock(Locale.class);
        Command command = mock(Command.class);
        CommandSender sender = mock(CommandSender.class);
        User user = mock(User.class);

        doReturn(user).when(locator).user(any(String.class));
        doReturn(true).when(sender).hasPermission(eq(PERMISSION));

        DummyCommandExecutor executor = spy(new DummyCommandExecutor(locale, transactor, locator, PERMISSION));

        String[] args = "player".split(" ");
        executor.onCommand(sender, command, "dummy", args);

        verify(locator, times(1)).user(any(String.class));
        verify(executor, times(1)).restrict(eq(sender), eq(user), eq(null), any(String.class), anyBoolean());
    }

    @Test
    public void testWithoutDuration() throws RejectedException {
        LocatorService locator = mock(LocatorService.class);
        Locale locale = mock(Locale.class);
        Command command = mock(Command.class);
        CommandSender sender = mock(CommandSender.class);
        User user = mock(User.class);

        doReturn(user).when(locator).user(any(String.class));
        doReturn(true).when(sender).hasPermission(eq(PERMISSION));

        DummyCommandExecutor executor = spy(new DummyCommandExecutor(locale, transactor, locator, PERMISSION));

        String[] args = "player for being rude".split(" ");
        executor.onCommand(sender, command, "dummy", args);

        verify(locator, times(1)).user(any(String.class));
        verify(executor, times(1)).restrict(eq(sender), eq(user), eq(null), eq("for being rude"), anyBoolean());
    }

    @Test
    public void testWithoutPermission() throws RejectedException {
        LocatorService locator = mock(LocatorService.class);
        Locale locale = mock(Locale.class);
        Command command = mock(Command.class);
        CommandSender sender = mock(CommandSender.class);
        User user = mock(User.class);

        doReturn(false).when(sender).hasPermission(eq(PERMISSION));

        DummyCommandExecutor executor = spy(new DummyCommandExecutor(locale, transactor, locator, PERMISSION));

        String[] args = "player".split(" ");
        executor.onCommand(sender, command, "dummy", args);

        verify(locator, never()).user(any(String.class));
        verify(executor, never()).restrict(eq(sender), eq(user), eq(null), eq("for being rude"), anyBoolean());

        // We should have sent the sender a message saying they don't have permission
        verify(sender, times(1)).sendMessage(any(String.class));
    }

    @Test
    public void testFailingCommand() throws RejectedException {
        LocatorService locator = mock(LocatorService.class);
        Locale locale = mock(Locale.class);
        Command command = mock(Command.class);
        CommandSender sender = mock(CommandSender.class);
        User user = mock(User.class);

        doReturn(user).when(locator).user(any(String.class));
        doReturn(true).when(sender).hasPermission(eq(PERMISSION));

        DummyCommandExecutor executor = spy(new DummyCommandExecutor(locale, transactor, locator, PERMISSION));
        doThrow(new RejectedException("not.ok")).when(executor).restrict(any(), any(), any(), any(), anyBoolean());

        String[] args = "player for being rude".split(" ");
        executor.onCommand(sender, command, "dummy", args);

        verify(locator, times(1)).user(any(String.class));
        verify(executor, times(1)).restrict(eq(sender), eq(user), eq(null), eq("for being rude"), anyBoolean());

        // We should have sent the sender a message saying they don't have permission
        verify(sender, times(1)).sendMessage(any(String.class));
    }
}
