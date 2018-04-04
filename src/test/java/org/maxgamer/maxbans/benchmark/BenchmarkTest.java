package org.maxgamer.maxbans.benchmark;

import org.junit.Test;
import org.maxgamer.maxbans.PluginContextTest;
import org.maxgamer.maxbans.orm.*;
import org.maxgamer.maxbans.test.IntegrationTest;
import org.maxgamer.maxbans.transaction.TransactionLayer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author netherfoam
 */
public class BenchmarkTest extends PluginContextTest implements IntegrationTest {
    private <T> T random(List<T> list, int seed) {
        while(seed < 0) {
            seed += list.size();
        }

        while(seed >= list.size()) {
            seed -= list.size();
        }

        return list.get(seed);
    }

    public List<User> users(int number) {
        List<User> users = new ArrayList<>(number);

        // Users
        try (TransactionLayer tx = getContext().components().transactor().transact()) {
            for(int i = 0; i < number; i++) {
                // Create 100k users
                UUID id = UUID.randomUUID();
                User user = new User(id, id.toString());

                tx.getSession().persist(user);
                users.add(user);
            }
            // Session is flushed at the end automatically
        }

        return users;
    }

    public List<Ban> userBans(int number, List<User> users) {
        List<Ban> bans = new ArrayList<>(number);

        try (TransactionLayer tx = getContext().components().transactor().transact()) {
            for(int i = 0; i < number; i++) {
                User user = random(users, i);
                User source = random(users, i - 1);

                Ban ban = new Ban();
                ban.setCreated(Instant.now());
                ban.setReason("My reason");
                ban.setSource(source);
                ban.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
                user.getBans().add(ban);

                tx.getSession().persist(ban);

                bans.add(ban);
            }
        }

        return bans;
    }

    public List<Ban> addressBans(int number, List<Address> addresses) {
        List<Ban> bans = new ArrayList<>(number);

        try (TransactionLayer tx = getContext().components().transactor().transact()) {
            for (int i = 0; i < number; i++) {
                Address address = random(addresses, i);

                Ban ban = new Ban();
                ban.setCreated(Instant.now());
                ban.setReason("My reason");
                ban.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
                address.getBans().add(ban);

                tx.getSession().persist(ban);

                bans.add(ban);
            }
            tx.getSession().flush();
        }

        return bans;
    }

    public List<Mute> userMutes(int number, List<User> users) {
        List<Mute> mutes = new ArrayList<>(number);

        try (TransactionLayer tx = getContext().components().transactor().transact()) {
            for(int i = 0; i < number; i++) {
                User user = random(users, i);
                User source = random(users, i - 1);

                Mute mute = new Mute();
                mute.setCreated(Instant.now());
                mute.setReason("My reason");
                mute.setSource(source);
                mute.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
                user.getMutes().add(mute);

                tx.getSession().persist(mute);

                mutes.add(mute);
            }
            tx.getSession().flush();
        }

        return mutes;
    }

    public List<Mute> addressMutes(int number, List<Address> addresses) {
        List<Mute> mutes = new ArrayList<>(number);

        try (TransactionLayer tx = getContext().components().transactor().transact()) {
            for(int i = 0; i < number; i++) {
                Address address = random(addresses, i);

                Mute mute = new Mute();
                mute.setCreated(Instant.now());
                mute.setReason("My reason");
                mute.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
                address.getMutes().add(mute);

                tx.getSession().persist(mute);

                mutes.add(mute);
            }
            tx.getSession().flush();
        }

        return mutes;
    }

    public List<Warning> warnings(int number, List<User> users) {
        List<Warning> warnings = new ArrayList<>(number);

        try (TransactionLayer tx = getContext().components().transactor().transact()) {
            for(int i = 0; i < number; i++) {
                User user = random(users, i);
                User source = random(users, i - 1);

                Warning warning = new Warning(user);
                warning.setCreated(Instant.now());
                warning.setReason("My reason");
                warning.setSource(source);
                warning.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
                user.getWarnings().add(warning);

                tx.getSession().persist(warning);

                warnings.add(warning);
            }
            tx.getSession().flush();
        }

        return warnings;
    }

    public List<Address> addresses(int number, List<User> users) {
        List<Address> addresses = new ArrayList<>(number);

        try (TransactionLayer tx = getContext().components().transactor().transact()) {
            for(int i = 0; i < number; i++) {
                User user = random(users, i);

                int[] modulars = new int[4];
                modulars[0] = i & 0xFF;
                modulars[1] = (i >> 8) & 0xFF;
                modulars[2] = (i >> 16) & 0xFF;
                modulars[3] = (i >> 24) & 0xFF;

                Address address = new Address(modulars[3] + "." + modulars[2] + "." + modulars[1] + "." + modulars[0]);
                UserAddress userAddress = new UserAddress(user, address);
                user.getAddresses().add(userAddress);

                tx.getSession().persist(address);
                tx.getSession().saveOrUpdate(user);

                addresses.add(address);
            }
            tx.getSession().flush();
        }

        return addresses;
    }

    /**
     * Even on slow machines, this should be triple the time we need
     */
    @Test(timeout = 20000)
    public void populate() {
        List<User> users = users(1000);
        userBans(500, users);
        userMutes(750, users);

        List<Address> addresses = addresses(2000, users);
        addressBans(100, addresses);
        addressMutes(500, addresses);

        warnings(5000, users);
    }
}
