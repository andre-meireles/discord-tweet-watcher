package bot;

import client.JDADiscordClient;
import client.TwitterClient;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Bot {
    private static final Logger logger = LogManager.getLogger(Bot.class);

    private JDADiscordClient discord;
    private TweetWatcher tweetWatcher;

    private static final String CMD_WORD = "!bot";
    private boolean tweetPostingEnabled = false;

    private List<String> enabledChannels;

    public Bot(final JDADiscordClient discord, final TwitterClient twitter) {
        this.discord = discord;
        tweetWatcher = new TweetWatcher(twitter);
        enabledChannels = new ArrayList<>();
    }

    public void run() {
        final CommandListener messageListener = new CommandListener(this);
        discord.buildClient(messageListener);
        try {
            processTweets();
        } catch (final Exception e) {
            // TODO: better exception handling, here and everywhere
            throw new RuntimeException("Caught exception while processing tweets: " + e + " , " + e.getMessage() + " , " + e.getCause() + " , " + Arrays.toString(e.getStackTrace()));
        }
    }

    // TODO: make all these sleeps configurable
    public void processTweets() throws InterruptedException {
        logger.info("Processing tweets.");
        while(true) {
            if(tweetPostingEnabled) {
                logger.info("Tweet posting is enabled, looping through tweets.");
                postNewTweets();
                // How long to wait between making intensive API calls to Twitter and Discord for getting/posting tweets.
                Thread.sleep(10000L);
            }
            // Slow down the main loop in case tweet posting is disabled.
            Thread.sleep(1000L);
        }
    }

    // TODO: make sure tweets always post in chrono order
    // TODO: add feature to ignore initial set of tweets if user is new to the watchlist
    public void postNewTweets() throws InterruptedException {
        for(final String twitterHandle : tweetWatcher.getWatchedTwitterUsers()) {
            final Set<Status> newTweets = tweetWatcher.getNewTweetsForUser(twitterHandle);
            for(final Status tweet : newTweets) {
                for(final String channel : enabledChannels) {
                    // TODO: prettify the tweet being posted instead of mostly raw text
                    discord.sendMessageToChannel(channel, "Tweet from " + tweet.getUser().getScreenName() + ":\n" + tweet.getText());
                }
                // How long to wait between posting each new tweet for a user
                Thread.sleep(200L);
            }
            // TODO: with a lot of watched users, this can get pretty lengthy
            // How long to wait between posting tweets for each user we're keeping track of
            Thread.sleep(3000L);
        }
    }

    public void addChannel(final String channel) {
        logger.info("Adding channel to list of enabled channels: " + channel);
        enabledChannels.add(channel);
    }

    public void removeChannel(final String channel) {
        logger.info("Removing channel from list of enabled channels: " + channel);
        if(enabledChannels.contains(channel)) {
            enabledChannels.remove(channel);
        }
    }

    public void setTweetPostingEnabled(final boolean flag) {
        tweetPostingEnabled = flag;
    }

    public TweetWatcher getTweetWatcher() {
        return tweetWatcher;
    }

    private static class CommandListener extends ListenerAdapter {

        private Bot bot;
        public CommandListener(final Bot bot) {
            this.bot = bot;
        }

        @Override
        public void onMessageReceived(final MessageReceivedEvent event) {
            logger.info("On message received. event: "+ event.getMessage().getContentRaw());
            final String rawMsgContent = event.getMessage().getContentRaw();
            if(rawMsgContent.contains(CMD_WORD)) {

                final String[] cmdTokens = rawMsgContent.split("\\s+");
                logger.info("Parsed tokens from Discord event: " + Arrays.toString(cmdTokens));

                // TODO: modularized command parsing instead of endless if statement
                if("on".equals(cmdTokens[1])) {
                    bot.setTweetPostingEnabled(true);
                    bot.addChannel(event.getChannel().getId());
                    event.getChannel().sendMessage("Tweet posting enabled on channel: " + event.getChannel().getName()).queue();
                } else if("off".equals(cmdTokens[1])) {
                    bot.setTweetPostingEnabled(false);
                    bot.removeChannel(event.getChannel().getId());
                    event.getChannel().sendMessage("Tweet posting disabled.").queue();
                } else if("watch".equals(cmdTokens[1])) {
                    // TODO: handle case where the given user isn't a valid twitter username
                    bot.getTweetWatcher().addWatchedUser(cmdTokens[2]);
                    event.getChannel().sendMessage("Watching user @" + cmdTokens[2] + " for new tweets.").queue();
                } else if("unwatch".equals(cmdTokens[1])) {
                    bot.getTweetWatcher().removeWatchedUser(cmdTokens[2]);
                    event.getChannel().sendMessage("No longer watching user @" + cmdTokens[2] + " for new tweets.").queue();
                } else {
                    event.getChannel().sendMessage("Unrecognized command.").queue();
                }
            }
        }
    }
}
