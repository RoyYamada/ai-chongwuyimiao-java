import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimestampTest {
    public static void main(String[] args) {
        // Test timestamps
        long timestamp1 = 1776441600;
        long timestamp2 = 1776528000;
        
        // Convert to Instant
        Instant instant1 = Instant.ofEpochSecond(timestamp1);
        Instant instant2 = Instant.ofEpochSecond(timestamp2);
        
        // Convert to local time
        LocalDateTime dateTime1 = LocalDateTime.ofInstant(instant1, ZoneId.systemDefault());
        LocalDateTime dateTime2 = LocalDateTime.ofInstant(instant2, ZoneId.systemDefault());
        
        // Format output
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("Timestamp 1: " + timestamp1 + " -> " + dateTime1.format(formatter));
        System.out.println("Timestamp 2: " + timestamp2 + " -> " + dateTime2.format(formatter));
        
        // Convert to UTC time
        LocalDateTime utcDateTime1 = LocalDateTime.ofInstant(instant1, ZoneId.of("UTC"));
        LocalDateTime utcDateTime2 = LocalDateTime.ofInstant(instant2, ZoneId.of("UTC"));
        System.out.println("UTC Timestamp 1: " + timestamp1 + " -> " + utcDateTime1.format(formatter));
        System.out.println("UTC Timestamp 2: " + timestamp2 + " -> " + utcDateTime2.format(formatter));
    }
}