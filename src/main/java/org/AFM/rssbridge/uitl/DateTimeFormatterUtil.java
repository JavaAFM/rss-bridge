package org.AFM.rssbridge.uitl;

import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class DateTimeFormatterUtil {
    private final LocalDateTime TODAY = LocalDateTime.now();
    private final String YESTERDAY = "Вчера";
    public LocalDateTime parseDateTime(String input) {
        try{
            if (input.startsWith(YESTERDAY)) {
                String timePart = input.split("\\|")[1].trim();
                LocalTime time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDate yesterday = LocalDate.now().minusDays(1);

                return LocalDateTime.of(yesterday, time);
            }else{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy | HH:mm");

                return LocalDateTime.parse(input, formatter);
            }
        }catch (DateTimeParseException e){
            throw new RuntimeException("Failed to parse date-time string: " + input, e);
        }
    }
}
