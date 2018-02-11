package bot;

import client.TwitterClient;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TweetWatcher {
    private static final Logger logger = LogManager.getLogger(TweetWatcher.class);

    private TwitterClient twitterClient;
    private Set<String> twitterHandlesToWatch;

    private Map<String, Set<Status>> previousTweets;

    private static final int NUM_TWEETS_PER_REQUEST = 10;

    public TweetWatcher(final TwitterClient twitterClient) {
        logger.info("Creating tweetwatcher. TwitterHandles to watch: " + twitterHandlesToWatch);
        this.twitterClient = twitterClient;
        this.twitterHandlesToWatch = new HashSet<>();
        this.previousTweets = new HashMap<>();
    }

    public void addWatchedUser(final String twitterHandle) {
        logger.info("Adding twitter user to watch: " + twitterHandle);
        twitterHandlesToWatch.add(twitterHandle);
    }

    public void removeWatchedUser(final String twitterHandle) {
        logger.info("Removing twitter user from watchlist: " + twitterHandle);
        if(twitterHandlesToWatch.contains(twitterHandle)) {
            twitterHandlesToWatch.remove(twitterHandle);
        }
    }

    public Set<String> getWatchedTwitterUsers() {
        return twitterHandlesToWatch;
    }

    public Set<Status> getNewTweetsForUser(final String twitterHandle) {
        final Set<Status> latestTweets = new HashSet(twitterClient.getLatestNTweetsForUser(twitterHandle, NUM_TWEETS_PER_REQUEST));
        final Set<Status> unseenTweets;
        if(previousTweets.containsKey(twitterHandle)) {
            unseenTweets = Sets.difference(latestTweets, previousTweets.get(twitterHandle));
        } else {
            unseenTweets = latestTweets;
        }

        previousTweets.put(twitterHandle, latestTweets);
        return unseenTweets;
    }
}
