package com.Azat9011.MTranstelegrambot.botapi.handlers.bussearch;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BusSearchRequestData {
    String departureBusStation;
    String arrivalBusStation;
    int departureBusStationCode;
    int arrivalBusStationCode;
    Date dateDepart;
}
