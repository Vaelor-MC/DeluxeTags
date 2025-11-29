package me.clip.deluxetags.utils;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.function.Consumer;

public class RedisUtils {
    private static RedisUtils INSTANCE;

    private Jedis subscriberJedis;
    private Jedis publisherJedis;

    private RedisUtils() {}

    public static void init(String ip, int port, String password) {
        if (INSTANCE != null) {
            INSTANCE.shutdown();
        }

        INSTANCE = new RedisUtils();
        DefaultJedisClientConfig config = DefaultJedisClientConfig.builder()
                .password(password)
                .build();

        // Separate connections!
        INSTANCE.subscriberJedis = new Jedis(ip, port, config);
        INSTANCE.publisherJedis  = new Jedis(ip, port, config);
    }

    public static RedisUtils getInstance() {
        return INSTANCE;
    }

    public void subscribe(Consumer<String> messageConsumer, String... channels) {
        new Thread(() -> {
            JedisPubSub jedisPubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    messageConsumer.accept(message);
                }
            };

            // BLOCKING call — safe now because it's a separate Jedis
            subscriberJedis.subscribe(jedisPubSub, channels);
        }).start();
    }

    public void publish(String channel, String... messages) {
        new Thread(() ->
                Arrays.stream(messages).forEach(msg ->
                        publisherJedis.publish(channel, msg))
        ).start();
    }

    public void shutdown() {
        if (subscriberJedis != null) subscriberJedis.close();
        if (publisherJedis != null) publisherJedis.close();
    }
}
