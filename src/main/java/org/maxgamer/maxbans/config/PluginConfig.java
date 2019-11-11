package org.maxgamer.maxbans.config;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.maxgamer.maxbans.exception.ConfigException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dirk Jamieson <dirk@redeye.co>
 */
public class PluginConfig {
    private JdbcConfig jdbcConfig;
    private WarningConfig warningConfig;
    private boolean isOffline;
    private Set<String> chatCommands;
    private boolean errorTracking;
    private boolean metrics;
    private boolean tooltips;
    
    public PluginConfig() {
        setJdbcConfig(new JdbcConfig());
        setWarningConfig(new WarningConfig());
        setOffline(false);
    }
    
    public PluginConfig(Configuration configuration, Server server) throws ConfigException {
        load(configuration, server);
    }
    
    public void load(Configuration configuration, Server server) throws ConfigException {
        this.setJdbcConfig(new JdbcConfig(configuration.getConfigurationSection("database")));
        this.setWarningConfig(new WarningConfig(configuration.getConfigurationSection("warnings")));
        this.setOffline(configuration.getBoolean("offline", !server.getOnlineMode()));
        this.setChatCommands(configuration.getStringList("chat-commands"));
        this.setErrorTracking(configuration.getBoolean("error-tracking", false));
        this.setMetrics(configuration.getBoolean("metrics", true));

        boolean wantsTooltips = configuration.getBoolean("tooltips", true);
        if (wantsTooltips && !server.getVersion().contains("Spigot")) {
            server.getLogger().warning("MaxBans has tooltips: true in it's configuration, but that requires Spigot for Minecraft 1.8.3 or higher");

            wantsTooltips = false;
        }

        this.setTooltips(wantsTooltips);
    }

    public JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public PluginConfig setJdbcConfig(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
        return this;
    }

    public WarningConfig getWarningConfig() {
        return warningConfig;
    }

    public PluginConfig setWarningConfig(WarningConfig warningConfig) {
        this.warningConfig = warningConfig;
        return this;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public boolean isTooltips() {
        return tooltips;
    }

    public PluginConfig setTooltips(boolean tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    public PluginConfig setOffline(boolean offline) {
        isOffline = offline;
        return this;
    }

    public Set<String> getChatCommands() {
        return chatCommands;
    }

    public void setChatCommands(Collection<String> chatCommands) {
        this.chatCommands = new HashSet<>(chatCommands.size());
        for(String command : chatCommands) {
            if(command == null) continue;
            this.chatCommands.add(command.toLowerCase());
        }
    }

    public boolean isErrorTracking() {
        return errorTracking;
    }

    public void setErrorTracking(boolean errorTracking) {
        this.errorTracking = errorTracking;
    }

    public boolean isMetrics() {
        return metrics;
    }

    public void setMetrics(boolean metrics) {
        this.metrics = metrics;
    }
}
