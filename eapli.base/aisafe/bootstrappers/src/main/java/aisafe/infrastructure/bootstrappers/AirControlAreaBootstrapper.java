package aisafe.infrastructure.bootstrappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import aisafe.aircontrolarea.application.AirControlAreaService;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaName;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.aircontrolarea.domain.GeographicBoundary;
import aisafe.persistence.PersistenceContext;
import aisafe.sharedkernel.Coordinates;
import eapli.framework.actions.Action;


@SuppressWarnings("squid:S106")
public class AirControlAreaBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AirControlAreaBootstrapper.class);

    private final AirControlAreaRepository repository =
            PersistenceContext.repositories().airControlAreas();

    private final AirControlAreaService service =
            new AirControlAreaService(repository);

    @Override
    public boolean execute() {
        register("LPPC", "Lisbon FIR", 30.0, -32.0, 43.0, -5.0);
        register("LFFF", "Paris FIR", 44.0, -4.5, 51.5, 7.5);
        register("EDGG", "Langen FIR", 47.0, 7.6, 53.5, 14.5);
        register("KZNY", "New York FIR", 24.0, -85.0, 47.0, -65.0);
        register("KZKC", "Kansas City FIR", 30.0, -115.0, 53.0, -86.0);
        register("KZLA", "Los Angeles FIR", 18.0, -160.0, 42.0, -115.0);
        register("SBAZ", "Brazil Southeast FIR", -30.0, -50.0, -15.0, -35.0);
        register("FAJA", "Africa FIR", -35.0, 10.0, 5.0, 35.0);
        register("UUWV", "Eurasia FIR", 40.0, 30.0, 60.0, 80.0);
        register("RJTG", "East Asia FIR", 20.0, 85.0, 45.0, 145.0);
        register("WSJC", "Singapore FIR", -2.0, 100.0, 5.0, 110.0);
        register("YMMM", "Melbourne FIR", -45.0, 115.0, -10.0, 155.0);
        register("LECM", "Madrid FIR", 35.0, -4.9, 42.9, 4.9);
        register("GCCC", "Canarias FIR", 26.0, -18.5, 30.0, -13.0);
        register("EGPX", "Scottish FIR", 52.0, -25.0, 66.0, -1.0);
        register("ENOR", "Norway FIR", 60.0, 5.0, 72.0, 32.0);
        register("EHAA", "Amsterdam FIR", 51.6, 3.0, 53.8, 7.5);
        register("LIMM", "Italy FIR", 35.0, 7.6, 47.0, 18.5);
        register("MMFR", "Mexico FIR", 14.0, -114.9, 29.9, -86.0);
        register("SKED", "Colombia FIR", -5.0, -82.0, 13.0, -66.0);
        register("SBRE", "Brazil Northeast FIR", -15.0, -40.0, 0.0, -30.0);

        return true;
    }

    private void register(
            final String code,
            final String name,
            final double southwestLatitude,
            final double southwestLongitude,
            final double northeastLatitude,
            final double northeastLongitude
    ) {
        try {
            final AirControlAreaCode areaCode = AirControlAreaCode.of(code);

            if (repository.findByCode(areaCode).isPresent()) {
                LOGGER.debug("Air Control Area already exists: %s%n", code);
                return;
            }

            service.registerAirControlArea(
                    areaCode,
                    GeographicBoundary.of(
                            Coordinates.of(southwestLatitude, southwestLongitude),
                            Coordinates.of(northeastLatitude, northeastLongitude)
                    ),
                    AirControlAreaName.of(name)
            );

            LOGGER.debug("Air Control Area registered: {} - {}", code, name);
        } catch (final IllegalArgumentException | IllegalStateException e)
        {
            LOGGER.debug("Air Control Area already exists: {}", code);
        }
    }
}