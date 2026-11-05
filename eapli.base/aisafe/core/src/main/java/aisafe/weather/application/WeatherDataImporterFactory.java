package aisafe.weather.application;

public class WeatherDataImporterFactory {

    public WeatherDataImporter getImporter(final String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }

        final String lowerCasePath = filePath.toLowerCase();

        if (lowerCasePath.endsWith(".csv")) {
            return new CsvWeatherDataImporter();
        }

        throw new IllegalArgumentException("Unsupported file format: " + filePath);
    }
}