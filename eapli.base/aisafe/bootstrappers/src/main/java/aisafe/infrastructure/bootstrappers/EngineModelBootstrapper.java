package aisafe.infrastructure.bootstrappers;

import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.enginemodel.domain.EngineModelService;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineModelBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EngineModelBootstrapper.class);

    private final EngineModelRepository engineModelRepository =
            PersistenceContext.repositories().engineModels();

    private final MakerRepository makerRepository =
            PersistenceContext.repositories().makers();

    private final EngineModelService service =
            new EngineModelService(engineModelRepository);

    @Override
    public boolean execute() {
        /*
         * IDs are explicit on purpose.
         * AircraftModelBootstrapper looks up engines by EngineModelID, so these IDs
         * must match the IDs used there.
         */

        // CFM International
        register("CFM56-7B", "CFM56-7B", EngineType.TURBOFAN,
                121000.0, FuelType.JET_A1, 0.37, "CFM");

        register("CFM56-5B", "CFM56-5B", EngineType.TURBOFAN,
                120000.0, FuelType.JET_A1, 0.36, "CFM");

        register("LEAP-1A", "LEAP-1A", EngineType.TURBOFAN,
                143000.0, FuelType.JET_A1, 0.32, "CFM");

        register("LEAP-1B", "LEAP-1B", EngineType.TURBOFAN,
                130000.0, FuelType.JET_A1, 0.33, "CFM");

        // GE Aviation
        register("CF6-80C2", "CF6-80C2", EngineType.TURBOFAN,
                282000.0, FuelType.JET_A1, 0.56, "GE");

        register("CF6-80E1", "CF6-80E1", EngineType.TURBOFAN,
                300000.0, FuelType.JET_A1, 0.55, "GE");

        register("GE90B", "GE90B", EngineType.TURBOFAN,
                489000.0, FuelType.JET_A1, 0.51, "GE");

        register("GE90-115B", "GE90-115B", EngineType.TURBOFAN,
                513000.0, FuelType.JET_A1, 0.50, "GE");

        register("GEnx-1B", "GEnx-1B", EngineType.TURBOFAN,
                339000.0, FuelType.JET_A1, 0.51, "GE");

        register("GEnx-2B", "GEnx-2B", EngineType.TURBOFAN,
                296000.0, FuelType.JET_A1, 0.52, "GE");

        // Rolls-Royce
        register("Trent-1000", "Trent 1000", EngineType.TURBOFAN,
                320000.0, FuelType.JET_A1, 0.52, "RR");

        register("RRTrent970", "RR Trent 970", EngineType.TURBOFAN,
                348310.0, FuelType.JET_A1, 0.52, "RR");

        register("RRTrent7000", "RR Trent 7000", EngineType.TURBOFAN,
                324000.0, FuelType.JET_A1, 0.51, "RR");

        register("RRTrentXWB", "RR Trent XWB", EngineType.TURBOFAN,
                431000.0, FuelType.JET_A1, 0.49, "RR");

        // Pratt & Whitney
        register("PW1100G", "PW1100G", EngineType.TURBOFAN,
                120000.0, FuelType.JET_A1, 0.56, "PW");

        return true;
    }

    private void register(final String engineModelID,
                          final String name,
                          final EngineType type,
                          final double power,
                          final FuelType fuel,
                          final double tsfc,
                          final String makerID) {
        try {
            final Maker maker = makerRepository.findByMakerID(MakerID.of(makerID))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Maker not found: " + makerID));

            final EngineModelID id = EngineModelID.of(engineModelID);

            if (engineModelRepository.findByEngineModelID(id).isPresent()) {
                LOGGER.debug("»»» EngineModel already exists: {}", engineModelID);
                return;
            }

            if (engineModelRepository.findByNameAndMaker(name, maker.identity()).isPresent()) {
                LOGGER.debug("»»» EngineModel already exists by name/maker: {}", name);
                return;
            }

            service.registerEngineModel(
                    id,
                    name,
                    type,
                    power,
                    fuel,
                    tsfc,
                    maker
            );

            LOGGER.debug("»»» EngineModel registered: {} ({})", engineModelID, name);

        } catch (final Exception e) {
            LOGGER.warn("EngineModel skipped [{}]: {}", engineModelID, e.getMessage());
        }
    }
}