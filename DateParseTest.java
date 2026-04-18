import com.example.demo.pet.Vaccination;
import java.time.LocalDateTime;

public class DateParseTest {
    public static void main(String[] args) {
        // Test various date formats
        String[] testDates = {
            "2026-04-19 00:00:00", // standard format
            "2026-04-19", // date only
            "2026-04-19T00:00:00", // ISO format
            "2026-04-19 00:00:00+00", // with timezone
            "2026-04-19T00:00:00+00:00" // standard ISO with timezone
        };

        for (String dateStr : testDates) {
            LocalDateTime result = Vaccination.parseToLocalDateTime(dateStr);
            System.out.println("Input: " + dateStr);
            System.out.println("Output: " + result);
            System.out.println("--------------------------------");
        }

        // Test VaccinationReminder
        com.example.demo.pet.VaccinationReminder reminder = new com.example.demo.pet.VaccinationReminder(
            1L, "Da Huang", "Golden Retriever", "Cat Triple Vaccine", "Dose 1", "2026-04-19 00:00:00+00"
        );
        System.out.println("VaccinationReminder:");
        System.out.println("DueDate: " + reminder.getDueDate());
        System.out.println("DaysUntilDue: " + reminder.getDaysUntilDue());
        System.out.println("IsOverdue: " + reminder.isOverdue());
    }
}