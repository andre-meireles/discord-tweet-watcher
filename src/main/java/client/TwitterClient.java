package client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

public class TwitterClient {
    private static final Logger logger = LogManager.getLogger(TwitterClient.class);

    private Twitter twitter;

    public TwitterClient(final String consumerKey,
                         final String consumerSecret,
                         final String accessToken,
                         final String accessTokenSecret) {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                // Needed to correctly retrieve new >140 char tweets
                .setTweetModeExtended(true);
        twitter = new TwitterFactory(cb.build()).getInstance();
    }

    public List<Status> getLatestNTweetsForUser(final String screenName, final int numTweets) {
        logger.info("Getting latest tweets for user: " + screenName);
        final Paging paging = new Paging();
        paging.setCount(numTweets);
        try{
            return twitter.getUserTimeline(screenName, paging);
        } catch(final Exception e) {
            throw new RuntimeException("Exception while using twitter4j: " + e);
        }
    }
}
