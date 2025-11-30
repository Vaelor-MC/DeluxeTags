package me.clip.deluxetags.utils;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public class RedisUtils {
    private static RedisUtils INSTANCE;

    private Jedis publisherJedis;
    private DefaultJedisClientConfig config;
    private String ip;
    private int port;

    private RedisUtils() {
    }

    public static void init(String ip, int port, String password) {
        if (INSTANCE != null) INSTANCE.shutdown();

        INSTANCE = new RedisUtils();
        INSTANCE.ip = ip;
        INSTANCE.port = port;
        INSTANCE.config = DefaultJedisClientConfig.builder()
                .password(password)
                .build();

        INSTANCE.publisherJedis = new Jedis(ip, port, INSTANCE.config);
    }

    public void subscribe(Consumer<String> handler, String... channels) {
        new Thread(() -> {
            try (Jedis jedis = new Jedis(ip, port, config)) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handler.accept(message);
                    }
                }, channels);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void publish(String channel, String... messages) {
        new Thread(() -> {
            for (String msg : messages) {
                publisherJedis.publish(channel, msg);
            }
        }).start();
    }

    public static RedisUtils getInstance() {
        return INSTANCE;
    }
    public void shutdown() {
        if (publisherJedis != null) publisherJedis.close();
    }
}
