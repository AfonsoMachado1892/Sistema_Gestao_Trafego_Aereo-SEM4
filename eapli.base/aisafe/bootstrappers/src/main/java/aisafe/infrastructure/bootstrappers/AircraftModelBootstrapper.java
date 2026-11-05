package aisafe.infrastructure.bootstrappers;

import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.aircraftmodel.domain.AircraftModelService;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class AircraftModelBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AircraftModelBootstrapper.class);

    private final AircraftModelRepository aircraftModelRepository =
            PersistenceContext.repositories().aircraftModels();

    private final MakerRepository makerRepository =
            PersistenceContext.repositories().makers();

    private final EngineModelRepository engineModelRepository =
            PersistenceContext.repositories().engineModels();

    private final AircraftModelService service =
            new AircraftModelService(aircraftModelRepository);

    @Override
    public boolean execute() {
        /*
         * IMPORTANT:
         * Makers must exist in MakerBootstrapper.
         * Engine model IDs must exist in EngineModelBootstrapper.
         *
         * Values are realistic enough for demo/simulation purposes.
         */

        // ─────────────────────────────────────────────────────────────────────
        // BOEING — Narrow-body
        // ─────────────────────────────────────────────────────────────────────

        register("737-800", "BOEING", AircraftType.PASSENGER,
                Set.of("CFM56-7B"),
                41413.0, 79015.0, 62732.0, 20894.0,
                12500.0, 230.0, 124.6, 0.021, 0.50,
                5436.0, 189, 2);

        register("737-MAX8", "BOEING", AircraftType.PASSENGER,
                Set.of("LEAP-1B"),
                45240.0, 82191.0, 65952.0, 20894.0,
                12500.0, 230.0, 127.0, 0.020, 0.51,
                6570.0, 178, 2);

        register("737-MAX9", "BOEING", AircraftType.PASSENGER,
                Set.of("LEAP-1B"),
                47300.0, 88314.0, 70760.0, 20894.0,
                12500.0, 230.0, 127.0, 0.020, 0.51,
                6570.0, 193, 2);

        // ─────────────────────────────────────────────────────────────────────
        // BOEING — Wide-body
        // ─────────────────────────────────────────────────────────────────────

        register("767-300ER", "BOEING", AircraftType.PASSENGER,
                Set.of("CF6-80C2"),
                90000.0, 186880.0, 133810.0, 91380.0,
                13100.0, 230.0, 283.3, 0.019, 0.51,
                11065.0, 261, 2);

        register("777-200ER", "BOEING", AircraftType.PASSENGER,
                Set.of("GE90B"),
                145149.0, 347452.0, 238100.0, 181283.0,
                13137.0, 257.0, 427.8, 0.017, 0.52,
                13650.0, 305, 2);

        register("777-300ER", "BOEING", AircraftType.PASSENGER,
                Set.of("GE90-115B"),
                167829.0, 351534.0, 237682.0, 181283.0,
                13137.0, 257.0, 436.8, 0.017, 0.52,
                13650.0, 396, 2);

        register("787-8", "BOEING", AircraftType.PASSENGER,
                Set.of("GEnx-1B"),
                119950.0, 227930.0, 161025.0, 101456.0,
                13100.0, 248.0, 325.0, 0.018, 0.52,
                13620.0, 242, 2);

        register("787-9", "BOEING", AircraftType.PASSENGER,
                Set.of("GEnx-1B"),
                128850.0, 254011.0, 181437.0, 101456.0,
                13100.0, 248.0, 360.5, 0.018, 0.52,
                14140.0, 290, 2);

        register("787-10", "BOEING", AircraftType.PASSENGER,
                Set.of("GEnx-1B"),
                135500.0, 254011.0, 193230.0, 101456.0,
                13100.0, 248.0, 360.5, 0.018, 0.52,
                11910.0, 330, 2);

        register("747-8I", "BOEING", AircraftType.PASSENGER,
                Set.of("GEnx-2B"),
                220128.0, 447696.0, 295742.0, 242470.0,
                13100.0, 252.0, 554.0, 0.022, 0.50,
                14320.0, 467, 4);

        // ─────────────────────────────────────────────────────────────────────
        // AIRBUS — Narrow-body
        // ─────────────────────────────────────────────────────────────────────

        register("A319", "AIRBUS", AircraftType.PASSENGER,
                Set.of("CFM56-5B"),
                40600.0, 75500.0, 62500.0, 23860.0,
                12100.0, 230.0, 122.6, 0.021, 0.50,
                6950.0, 156, 2);

        register("A320", "AIRBUS", AircraftType.PASSENGER,
                Set.of("CFM56-5B"),
                42600.0, 78000.0, 64300.0, 23860.0,
                12100.0, 230.0, 122.6, 0.021, 0.50,
                6100.0, 180, 2);

        register("A320neo", "AIRBUS", AircraftType.PASSENGER,
                Set.of("LEAP-1A"),
                44500.0, 79000.0, 67400.0, 26730.0,
                12100.0, 230.0, 122.6, 0.020, 0.51,
                6850.0, 194, 2);

        register("A321", "AIRBUS", AircraftType.PASSENGER,
                Set.of("CFM56-5B"),
                48500.0, 93500.0, 77800.0, 23860.0,
                12100.0, 230.0, 122.6, 0.021, 0.50,
                5950.0, 220, 2);

        register("A321neo", "AIRBUS", AircraftType.PASSENGER,
                Set.of("LEAP-1A"),
                50500.0, 97000.0, 79200.0, 32940.0,
                12100.0, 230.0, 122.6, 0.020, 0.51,
                7400.0, 244, 2);

        // ─────────────────────────────────────────────────────────────────────
        // AIRBUS — Wide-body
        // ─────────────────────────────────────────────────────────────────────

        register("A330-200", "AIRBUS", AircraftType.PASSENGER,
                Set.of("CF6-80E1"),
                120600.0, 242000.0, 170000.0, 139090.0,
                12500.0, 240.0, 361.6, 0.019, 0.51,
                13450.0, 247, 2);

        register("A330-300", "AIRBUS", AircraftType.PASSENGER,
                Set.of("CF6-80E1"),
                122000.0, 242000.0, 175000.0, 97530.0,
                12500.0, 240.0, 361.6, 0.019, 0.51,
                11750.0, 277, 2);

        register("A330-900neo", "AIRBUS", AircraftType.PASSENGER,
                Set.of("RRTrent7000"),
                135000.0, 251000.0, 181000.0, 139090.0,
                12500.0, 240.0, 361.6, 0.018, 0.52,
                13334.0, 287, 2);

        register("A350-900", "AIRBUS", AircraftType.PASSENGER,
                Set.of("RRTrentXWB"),
                142400.0, 280000.0, 192000.0, 140795.0,
                13100.0, 248.0, 443.0, 0.018, 0.52,
                15000.0, 315, 2);

        register("A350-1000", "AIRBUS", AircraftType.PASSENGER,
                Set.of("RRTrentXWB"),
                155000.0, 319000.0, 223000.0, 159000.0,
                13100.0, 248.0, 464.3, 0.018, 0.52,
                16100.0, 369, 2);

        register("A380", "AIRBUS", AircraftType.PASSENGER,
                Set.of("RRTrent970"),
                276800.0, 575000.0, 361000.0, 320000.0,
                13106.0, 261.0, 845.0, 0.020, 0.52,
                15200.0, 555, 4);

        // ─────────────────────────────────────────────────────────────────────
        // Cargo / Freighter examples
        // ─────────────────────────────────────────────────────────────────────

        register("777F", "BOEING", AircraftType.CARGO,
                Set.of("GE90-115B"),
                144379.0, 347452.0, 248115.0, 181283.0,
                13137.0, 257.0, 427.8, 0.017, 0.52,
                9200.0, 0, 2);

        register("747-8F", "BOEING", AircraftType.CARGO,
                Set.of("GEnx-2B"),
                197131.0, 447696.0, 346091.0, 242470.0,
                13100.0, 252.0, 554.0, 0.022, 0.50,
                8130.0, 0, 4);

        register("A330-200F", "AIRBUS", AircraftType.CARGO,
                Set.of("CF6-80E1"),
                109000.0, 233000.0, 178000.0, 97530.0,
                12500.0, 240.0, 361.6, 0.019, 0.51,
                7400.0, 0, 2);

        return true;
    }

    private void register(final String modelID,
                          final String makerID,
                          final AircraftType type,
                          final Set<String> engineModelIDs,
                          final double emptyWeight,
                          final double mtow,
                          final double mzfw,
                          final double maxFuelCapacity,
                          final double serviceCeiling,
                          final double cruiseSpeed,
                          final double wingArea,
                          final double dragCoefficient,
                          final double liftCoefficient,
                          final double maximumRange,
                          final int maxPassengerCapacity,
                          final int engineNumber) {
        try {
            final Maker maker = makerRepository.findByMakerID(MakerID.of(makerID))
                    .orElseThrow(() -> new IllegalArgumentException("Maker not found: " + makerID));

            if (aircraftModelRepository.findByModelIDAndMaker(
                    AircraftModelID.of(modelID),
                    maker.identity()).isPresent()) {
                LOGGER.debug("»»» AircraftModel already exists: {}", modelID);
                return;
            }

            final Set<EngineModel> engineModels = engineModelIDs.stream()
                    .map(id -> engineModelRepository.findByEngineModelID(EngineModelID.of(id))
                            .orElseThrow(() -> new IllegalArgumentException("Engine model not found: " + id)))
                    .collect(Collectors.toSet());

            service.registerAircraftModel(
                    AircraftModelID.of(modelID),
                    maker,
                    type,
                    engineModels,
                    emptyWeight,
                    mtow,
                    mzfw,
                    maxFuelCapacity,
                    serviceCeiling,
                    cruiseSpeed,
                    wingArea,
                    dragCoefficient,
                    liftCoefficient,
                    maximumRange,
                    maxPassengerCapacity,
                    engineNumber
            );

            LOGGER.debug("»»» AircraftModel: {}", modelID);

        } catch (final Exception e) {
            LOGGER.warn("AircraftModel skipped: {} ({})", modelID, e.getMessage());
        }
    }
}