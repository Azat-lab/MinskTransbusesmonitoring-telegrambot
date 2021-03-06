package com.Azat9011.MTranstelegrambot.service;

import com.Azat9011.MTranstelegrambot.model.Car;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Выполняет служебную обработку коллекций "Машин".
 *
 * @author Azat9011
 */
@Service
public class CarsProcessingService {

    public List<Car> filterCarsWithMinimumPrice(List<Car> cars) {
        return new ArrayList<>(cars.stream()
                .collect(Collectors.toMap(Car::getCarType, Function.identity(),
                        BinaryOperator.minBy(Comparator.comparing(Car::getMinimalPrice)))).values());
    }

}
