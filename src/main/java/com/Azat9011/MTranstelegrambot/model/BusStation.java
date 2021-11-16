package com.Azat9011.MTranstelegrambot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Azat9011
 */
@Data
public class BusStation {
    @JsonProperty(value = "n")
    private String busStationName;
    @JsonProperty(value = "c")
    private int busStationCode;
}
