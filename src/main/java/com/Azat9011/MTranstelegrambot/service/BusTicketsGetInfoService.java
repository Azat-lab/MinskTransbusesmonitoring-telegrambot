package com.Azat9011.MTranstelegrambot.service;

import com.Azat9011.MTranstelegrambot.botapi.MinskTransTelegramBot;
import com.Azat9011.MTranstelegrambot.model.Bus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Отправляет запросы к MT API.
 * Получает данные об актуальных поездах.
 *
 * @author Azat9011
 */
@Slf4j
@Service
@Getter
@Setter
public class BusTicketsGetInfoService {
    @Value(" ")
    private String busInfoRidRequestTemplate;
    @Value(" ")
    private String busInfoRequestTemplate;

    private static final String URI_PARAM_STATION_DEPART_CODE = "BUS_STATION_DEPART_CODE";
    private static final String URI_PARAM_STATION_ARRIVAL_CODE = "BUS_STATION_ARRIVAL_CODE";
    private static final String URI_PARAM_DATE_DEPART = "DATE_DEPART";
    private static final String BUS_DATE_IS_OUT_OF_DATE_MESSAGE = "находится за пределами периода";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    private final RestTemplate restTemplate;
    private final ReplyMessagesService messagesService;
    private final MinskTransTelegramBot telegramBot;

    public BusTicketsGetInfoService(RestTemplate restTemplate, ReplyMessagesService messagesService,
                                    @Lazy MinskTransTelegramBot telegramBot) {
        this.restTemplate = restTemplate;
        this.messagesService = messagesService;
        this.telegramBot = telegramBot;
    }

    public List<Bus> getBusTicketsList(long chatId, int busStationDepartCode, int busStationArrivalCode, Date dateDepart) {
        List<Bus>  busList;
        String dateDepartStr = dateFormatter.format(dateDepart);
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(URI_PARAM_STATION_DEPART_CODE, String.valueOf(busStationDepartCode));
        urlParams.put(URI_PARAM_STATION_ARRIVAL_CODE, String.valueOf(busStationArrivalCode));
        urlParams.put(URI_PARAM_DATE_DEPART, dateDepartStr);

        Map<String, HttpHeaders> ridAndHttpHeaders = sendRidRequest(chatId, urlParams);
        if (ridAndHttpHeaders.isEmpty()) {
            return Collections.emptyList();
        }

        String ridValue = ridAndHttpHeaders.keySet().iterator().next();
        HttpHeaders httpHeaders = ridAndHttpHeaders.get(ridValue);
        List<String> cookies = httpHeaders.get("Set-Cookie");

        if (cookies == null) {
            telegramBot.sendMessage(messagesService.getWarningReplyMessage(chatId, "reply.query.failed"));
            return Collections.emptyList();
        }
        HttpHeaders busInfoRequestHeaders = getDataRequestHeaders(cookies);
        String busInfoResponseBody = sendBusInfoJsonRequest(ridValue, busInfoRequestHeaders);

        busList = parseResponseBody(busInfoResponseBody);

        return busList;
    }


    private Map<String, HttpHeaders> sendRidRequest(long chatId, Map<String, String> urlParams) {
        ResponseEntity<String> passMinskTransResp
                = restTemplate.getForEntity(busInfoRidRequestTemplate, String.class,
                urlParams);

        String jsonRespBody = passMinskTransResp.getBody();

        if (isResponseBodyHasNoBuses(jsonRespBody)) {
            telegramBot.sendMessage(messagesService.getWarningReplyMessage(chatId, "reply.busSearch.dateOutOfBoundError"));
            return Collections.emptyMap();
        }

        Optional<String> parsedRID = parseRID(jsonRespBody);
        if (parsedRID.isEmpty()) {
            return Collections.emptyMap();
        }

        return Collections.singletonMap(parsedRID.get(), passMinskTransResp.getHeaders());
    }


    //Срабатывает если MT ответил снова RID, а не данными по поезду
    private boolean isResponseResultRidDuplicate(ResponseEntity<String> resultResponse) {
        if (resultResponse.getBody() == null) {
            return true;
        }
        return resultResponse.getBody().contains("\"result\":\"RID");
    }

    private List<Bus> parseResponseBody(String responseBody) {
        List<Bus> busList = null;
        try {
            JsonNode busesNode = objectMapper.readTree(responseBody).path("tp").findPath("list");
            busList = Arrays.asList(objectMapper.readValue(busesNode.toString(), Bus[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return Objects.isNull(busList) ? Collections.emptyList() : busList;
    }


    private Optional<String> parseRID(String jsonRespBody) {
        String rid = null;
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonRespBody.trim());
            JsonNode ridNode = jsonNode.get("RID");
            if (ridNode != null) {
                rid = ridNode.asText();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(rid);
    }


    private HttpHeaders getDataRequestHeaders(List<String> cookies) {
        String jSessionId = cookies.get(cookies.size() - 1);
        jSessionId = jSessionId.substring(jSessionId.indexOf("=") + 1, jSessionId.indexOf(";"));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", "lang=ru");
        requestHeaders.add("Cookie", "JSESSIONID=" + jSessionId);
        requestHeaders.add("Cookie", "AuthFlag=false");

        return requestHeaders;
    }


    private String sendBusInfoJsonRequest(String ridValue, HttpHeaders dataRequestHeaders) {
        HttpEntity<String> httpEntity = new HttpEntity<>(dataRequestHeaders);
        ResponseEntity<String> resultResponse = restTemplate.exchange(busInfoRequestTemplate,
                HttpMethod.GET,
                httpEntity,
                String.class, ridValue);

        while (isResponseResultRidDuplicate(resultResponse)) {
            resultResponse = restTemplate.exchange(busInfoRequestTemplate,
                    HttpMethod.GET,
                    httpEntity,
                    String.class, ridValue);
        }

        return resultResponse.getBody();
    }

    private boolean isResponseBodyHasNoBuses(String jsonRespBody) {
        return jsonRespBody == null || jsonRespBody.contains(BUS_DATE_IS_OUT_OF_DATE_MESSAGE);
    }

}