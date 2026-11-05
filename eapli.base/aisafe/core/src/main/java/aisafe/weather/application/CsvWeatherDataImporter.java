package aisafe.weather.application;


import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvWeatherDataImporter implements WeatherDataImporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public List<WeatherDataDTO> readWeatherData(final String filePath) {
        final List<WeatherDataDTO> result = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (line.isBlank()) continue;

                final String[] fields = line.split(";");

                result.add(new WeatherDataDTO(
                        fields[0].trim(),                                        // areaCode
                        LocalDate.parse(fields[9].trim(), DATE_FORMATTER),       // day
                        LocalTime.parse(fields[10].trim()),                      // startTime
                        LocalTime.parse(fields[11].trim()),                      // endTime
                        parseDouble(fields[1]),                                  // latStart
                        parseDouble(fields[2]),                                  // lonStart
                        parseDouble(fields[3]),                                  // latEnd
                        parseDouble(fields[4]),                                  // lonEnd
                        Integer.parseInt(fields[5].trim()),                      // minAltitude
                        Integer.parseInt(fields[6].trim()),                      // maxAltitude
                        parseDouble(fields[8]),                                  // windSpeed
                        parseDouble(fields[7])                                   // windDirection
                ));
            }
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("File not found: " + filePath, e);
        }

        return result;
    }

    private double parseDouble(final String value) {
        return Double.parseDouble(value.trim().replace(",", "."));
    }
}