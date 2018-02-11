package bot;

import client.JDADiscordClient;
import client.TwitterClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String CONFIG_FILE_NAME = "src/main/resources/config.json";
    public static void main(String[] args) throws IOException, JSONException {

        // TODO: add tests
        final JSONObject configJsonObj = new JSONObject(new String(Files.readAllBytes(Paths.get(CONFIG_FILE_NAME))));
        final JDADiscordClient jdaDiscordClient = new JDADiscordClient(configJsonObj.getString("discordToken"));
        final TwitterClient twitterClient = new TwitterClient(
                configJsonObj.getString("twitterConsumerKey"),
                configJsonObj.getString("twitterConsumerSecret"),
                configJsonObj.getString("twitterAccessToken"),
                configJsonObj.getString("twitterAccessTokenSecret"));

        final Bot bot = new Bot(jdaDiscordClient, twitterClient);
        bot.run();
    }
}
