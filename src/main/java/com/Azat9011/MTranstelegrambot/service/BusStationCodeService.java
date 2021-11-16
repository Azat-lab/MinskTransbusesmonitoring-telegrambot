package com.Azat9011.MTranstelegrambot.service;

import com.Azat9011.MTranstelegrambot.cache.StationsCache;
import com.Azat9011.MTranstelegrambot.model.BusStation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Позволяет получить код остановки по ее названию.
 *
 * @author Azat9011
 */
@Component
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class BusStationCodeService<BusStationsDataCache> {
    @Value("https://minsktrans.by/lookout_yard/Home/Index/minsk#/routes/bus ")
    private String busStationCodeRequestTemplate;
    private RestTemplate restTemplate;
    private BusStationsDataCache busStationsCache;


    public BusStationCodeService(RestTemplate restTemplate, BusStationsDataCache busStationsCache) {
        this.restTemplate = restTemplate;
        this.busStationsCache = busStationsCache;
    }

    public int getStationCode(String busStationName) {
        String busStationsNameParam = busStationName.toUpperCase();

        String busStationNameParam = null;
        StationsCache busStationCache = null;
        Optional<Integer> busStationCodeOptional = busStationCache.getBusStationCode(busStationNameParam);
        if (busStationCodeOptional.isPresent()) return busStationCodeOptional.get();

        if (processBusStationCodeRequest(busStationNameParam).isEmpty()) {
            return -1;
        }

        return busStationCache.getBusStationCode(busStationNameParam).orElse(-1);

    }

    private Optional<BusStation[]> processBusStationCodeRequest(String busStationNamePart) {
        String busStationCodeRequestTemplate = null;
        ResponseEntity<BusStation[]> response =
                restTemplate.getForEntity(
                        busStationCodeRequestTemplate,
                        BusStation[].class, busStationNamePart);
        BusStation[] busStations = response.getBody();
        if (busStations == null) {
            return Optional.empty();
        }

        for (BusStation busStation : busStations) {
            StationsCache busStationCache = null;
            busStationCache.addBusStationToCache(busStation.getBusStationName(), busStation.getBusStationCode());
        }

        return Optional.of(busStations);
    }
}
