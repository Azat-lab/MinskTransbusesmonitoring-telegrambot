package com.Azat9011.MTranstelegrambot.service;

import com.Azat9011.MTranstelegrambot.cache.BusStationsDataCache;
import com.Azat9011.MTranstelegrambot.cache.StationsCache;
import com.Azat9011.MTranstelegrambot.model.BusStation;
import com.Azat9011.MTranstelegrambot.utils.Emojis;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Поиск станции в справочнике станций
 *
 * @author Azat9011
 */
@Component
@Service
@Slf4j
public class BusStationBookService {
    @Value("${stationcodeservice.requesttemplate}")  //https://minsktrans.by/lookout_yard/Home/Index/minsk#/routes/bus
    private String busStationSearchTemplate;
    private RestTemplate restTemplate;
    private BusStationsDataCache busStationsCache;
    private ReplyMessagesService messagesService;

    public BusStationBookService(RestTemplate restTemplate, BusStationsDataCache busStationsCache, ReplyMessagesService messagesService) {
        this.restTemplate = restTemplate;
        this.busStationsCache = busStationsCache;
        this.messagesService = messagesService;
    }

    public SendMessage processBusStationNamePart(long chatId, @NotNull String busStationNamePartParam) {
        String searchedBusStationName = busStationNamePartParam.toUpperCase();

        Optional<String> optionalBusStationName = busStationsCache.getBusStationName(searchedBusStationName);
        if (optionalBusStationName.isPresent()) {
            return messagesService.getReplyMessage(chatId, "reply.busStationBook.stationFound",
                    Emojis.SUCCESS_MARK, optionalBusStationName.get());
        }

        List<BusStation> busStations = sendBusStationSearchRequest(searchedBusStationName);

        List<String> foundedBusStationNames = busStations.stream().
                map(BusStation::getBusStationName).filter(busStationName -> busStationName.contains(searchedBusStationName)).
                collect(Collectors.toList());

        if (foundedBusStationNames.isEmpty()) {
            return messagesService.getReplyMessage(chatId, "reply.busStationBookMenu.busStationNotFound");
        }

        StringBuilder busStationsList = new StringBuilder();
        foundedBusStationNames.forEach(busStationName -> busStationsList.append(busStationName).append("\n"));

        return messagesService.getReplyMessage(chatId, "reply.busStationBook.busStationsFound", Emojis.SUCCESS_MARK,
                busStationsList.toString());

    }

    private List<BusStation> sendBusStationSearchRequest(String busStationNamePart) {
        ResponseEntity<BusStation[]> response =
                restTemplate.getForEntity(
                        busStationSearchTemplate,
                        BusStation[].class, busStationNamePart);
        BusStation[] busStations = response.getBody();
        if (busStations == null) {
            return Collections.emptyList();
        }

        for (BusStation busStation : busStations) {
            StationsCache.addStationToCache(busStation.getBusStationName(), busStation.getBusStationCode());
        }

        return List.of(busStations);
    }

}
