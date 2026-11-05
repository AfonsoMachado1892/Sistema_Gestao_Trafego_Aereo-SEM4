package aisafe.simulation.application;

/**
 * Thrown when the C simulation fails or the report cannot be read.
 */
public class SimulationException extends RuntimeException {

    public SimulationException(final String message) {
        super(message);
    }
}
