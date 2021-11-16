package com.Azat9011.MTranstelegrambot.cache;

import java.util.Optional;

/**
 * @author Azat9011
 */
public interface StationsCache {
    static void addStationToCache(String busStationName, int busStationCode) {
    }

    Optional<String> getBusStationName(String busStationNameParam);

    Optional<Integer> getBusStationCode(String busStationNameParam);

    void addBusStationToCache(String busStationName, int busStationCode);
}
