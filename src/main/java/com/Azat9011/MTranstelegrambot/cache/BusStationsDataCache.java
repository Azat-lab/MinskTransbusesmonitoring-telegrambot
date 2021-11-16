package com.Azat9011.MTranstelegrambot.cache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Azat9011
 */
@Component
@Getter
public class BusStationsDataCache implements StationsCache {
    private Map<String, Integer> busStationCodeCache = new HashMap<>();

    @Override
    public Optional<String> getBusStationName(String busStationNameParam) {
        return busStationCodeCache.keySet().stream().filter(busStationName -> busStationName.equals
                (busStationNameParam)).findFirst();
    }

    @Override
    public Optional<Integer> getBusStationCode(String busStationNameParam) {
        return Optional.ofNullable(busStationCodeCache.get(busStationNameParam));
    }

    @Override
    public void addBusStationToCache(String busStationName, int busStationCode) {
        busStationCodeCache.put(busStationName,busStationCode);
    }

}
