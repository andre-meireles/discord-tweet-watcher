package client;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JDADiscordClient {
    private static final Logger logger = LogManager.getLogger(JDADiscordClient.class);

    private JDA jda;
    private String token;

    public JDADiscordClient(final String token) {
        this.token = token;
    }

    public <L extends ListenerAdapter> void buildClient(L... listeners) {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .addEventListener((Object[])listeners)
                    .buildAsync();
        } catch (final Exception e) {
            throw new RuntimeException("Failed building JDA." + e);
        }
    }

    public void sendMessageToChannel(final String channelId, final String message) {
        logger.info(String.format("Sending msg to discord channel: %s. Channel ID: %s.", message, channelId));
        jda.getTextChannelById(channelId).sendMessage(message).queue();
    }
}
