package aisafe.app.backoffice.console.presentation.weather;

import aisafe.persistence.PersistenceContext;
import aisafe.weather.application.ImportBulkWeatherDataController;
import aisafe.weather.application.WeatherDataDTO;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

@SuppressWarnings("squid:S106")
public class ImportBulkWeatherDataUI extends AbstractUI {

    private final ImportBulkWeatherDataController controller = new ImportBulkWeatherDataController(
            PersistenceContext.repositories().weatherData()
    );

    @Override
    protected boolean doShow() {

        // 1. Request file path
        final String filePath = Console.readNonEmptyLine("File path:", "File path cannot be empty.");

        // 2. Preview import
        List<WeatherDataDTO> weatherDataDTOList;
        try {
            weatherDataDTOList = controller.previewImport(filePath);
        } catch (final IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }

        if (weatherDataDTOList.isEmpty()) {
            System.out.println("No records found in file.");
            return false;
        }

        // 3. Show preview
        System.out.printf("%nPreview (%d records found):%n", weatherDataDTOList.size());
        System.out.printf("%-10s %-12s %-8s %-8s %-12s %-12s %-10s %-10s %-12s %-12s %-12s %-10s%n",
                "Area Code", "Day", "Start", "End",
                "Lat Start", "Lon Start", "Lat End", "Lon End",
                "Alt Min (ft)", "Alt Max (ft)",
                "Wind (kts)", "Dir (°)");
        System.out.println("-".repeat(140));

        for (final WeatherDataDTO dto : weatherDataDTOList) {
            System.out.printf("%-10s %-12s %-8s %-8s %-12.4f %-12.4f %-10.4f %-10.4f %-12d %-12d %-12.2f %-10.2f%n",
                    dto.areaCode(),
                    dto.day(),
                    dto.startTime(),
                    dto.endTime(),
                    dto.latStart(),
                    dto.lonStart(),
                    dto.latEnd(),
                    dto.lonEnd(),
                    dto.minAltitude(),
                    dto.maxAltitude(),
                    dto.windSpeed(),
                    dto.windDirection());
        }

        // 4. Confirm import
        final String confirm = Console.readLine("Confirm import? (y/n):");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Import cancelled.");
            return false;
        }

        // 5. Execute import
        try {
            final int count = controller.confirmImport(weatherDataDTOList);
            System.out.printf("%d record(s) imported successfully.%n", count);
            return true;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Import Bulk Weather Data";
    }
}