package org.AFM.rssbridge.uitl;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
public class DateTimeFormatterUtil {
    private final String YESTERDAY = "Вчера";

    public LocalDateTime parseTengriTime(String input) {
        try {
            if (input.startsWith(YESTERDAY)) {
                String timePart = input.split("\\|")[1].trim();
                LocalTime time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDate yesterday = LocalDate.now().minusDays(1);

                return LocalDateTime.of(yesterday, time);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy | HH:mm");

                return LocalDateTime.parse(input, formatter);
            }
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Failed to parse date-time string: " + input, e);
        }
    }

    public LocalDateTime parseKazTagTime(String input) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm");
            return LocalDateTime.parse(input, formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Failed to parse date-time string: " + input, e);
        }
    }

    public LocalDateTime parseAzattyqTime(String input){
        try{
            LocalDateTime now = LocalDateTime.now();
            if (input.matches("\\d{1,2} \\p{IsCyrillic}+ \\d{4}, \\d{2}:\\d{2}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", new Locale("ru"));
                return LocalDateTime.parse(input, formatter);
            }
            if (input.contains("час")) {
                int hoursAgo = Integer.parseInt(input.replaceAll("\\D+", ""));
                return now.minusHours(hoursAgo);
            }
            if (input.contains("минут")) {
                int minutesAgo = Integer.parseInt(input.replaceAll("\\D+", ""));
                return now.minusMinutes(minutesAgo);
            }
            throw new DateTimeParseException("Unrecognized date-time format", input, 0);
        }catch (DateTimeParseException e){
            throw new RuntimeException("Failed to parse date-time string: " + input, e);
        }
    }
}
