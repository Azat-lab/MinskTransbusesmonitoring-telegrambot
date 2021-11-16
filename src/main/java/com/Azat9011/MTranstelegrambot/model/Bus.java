package com.Azat9011.MTranstelegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * автобус
 *
 * @author Azat9011
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bus {
    @JsonProperty(value = "number")
    private String number;

    @JsonProperty(value = "brand")
    private String brand;

    @JsonProperty(value = "bus_station0")
    private String busStationDepart;

    @JsonProperty(value = "bus_station1")
    private String busStationArrival;

    @JsonProperty(value = "date0")
    private String dateDepart;

    @JsonProperty(value = "date1")
    private String dateArrival;

    @JsonProperty(value = "time0")
    private String timeDepart;

    @JsonProperty(value = "time1")
    private String timeArrival;

    @JsonProperty(value = "cars")
    private List<Car> availableCars;

    @JsonProperty(value = "timeInWay")
    private String timeInWay;

}