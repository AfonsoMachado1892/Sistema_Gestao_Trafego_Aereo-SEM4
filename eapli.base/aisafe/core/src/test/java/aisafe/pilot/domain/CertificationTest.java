package aisafe.pilot.domain;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Certification}.
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class CertificationTest {

    private static final LocalDate ISSUED      = LocalDate.of(2023, 1, 1);
    private static final LocalDate VALID_UNTIL = LocalDate.of(2028, 1, 1);

    private AircraftModel a320;
    private AircraftModel b737;

    private AircraftModel buildAircraftModel(final String modelID) {
        final Maker engineMaker = new Maker(MakerID.of("CFM"), "CFM International",
                new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.ENGINE));
        final EngineModel engine = new EngineModel(
                EngineModelID.of("CFM56-5B"), "CFM56-5B",
                EngineType.TURBOFAN, 120000.0, FuelType.JET_A1, 0.36, engineMaker);
        final Maker airbus = new Maker(MakerID.of("AIRBUS"), "Airbus",
                new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.AIRCRAFT));
        return new AircraftModel(AircraftModelID.of(modelID), airbus, AircraftType.PASSENGER,
                Set.of(engine), 42000, 78000, 70000, 26000, 12000, 833, 122.6, 0.025, 1.5, 6300, 180, 2);
    }

    @BeforeEach
    void setUp() {
        a320 = buildAircraftModel("A320");
        b737 = buildAircraftModel("B737");
    }

    // ── Valid construction ───────────────────────────────────────────────────

    @Test
    void ensureValidCertificationIsCreated() {
        // Arrange + Act
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);

        // Assert
        assertEquals(a320, cert.aircraftModel());
        assertEquals(FlightHours.of(500), cert.flightHours());
        assertEquals(ISSUED, cert.issuedAt());
        assertEquals(VALID_UNTIL, cert.validUntil());
    }

    @Test
    void ensureZeroHoursIsAccepted() {
        assertDoesNotThrow(() -> Certification.of(a320, FlightHours.of(0), ISSUED, VALID_UNTIL));
    }

    // ── Null guards ───────────────────────────────────────────────────────────

    @Test
    void ensureNullModelIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Certification.of(null, FlightHours.of(500), ISSUED, VALID_UNTIL));
    }

    @Test
    void ensureNullFlightHoursIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Certification.of(a320, null, ISSUED, VALID_UNTIL));
    }

    @Test
    void ensureNullIssuedDateIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Certification.of(a320, FlightHours.of(500), null, VALID_UNTIL));
    }

    @Test
    void ensureNullValidUntilIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Certification.of(a320, FlightHours.of(500), ISSUED, null));
    }

    // ── Date invariants ───────────────────────────────────────────────────────

    @Test
    void ensureValidUntilBeforeIssuedIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Certification.of(a320, FlightHours.of(500), VALID_UNTIL, ISSUED));
    }

    @Test
    void ensureValidUntilEqualToIssuedIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Certification.of(a320, FlightHours.of(500), ISSUED, ISSUED));
    }

    // ── isValidOn ─────────────────────────────────────────────────────────────

    @Test
    void ensureIsValidOnReturnsTrueWithinPeriod() {
        // Arrange
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);

        // Act + Assert
        assertTrue(cert.isValidOn(LocalDate.of(2025, 6, 1)));
    }

    @Test
    void ensureIsValidOnReturnsTrueOnIssuedDate() {
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);
        assertTrue(cert.isValidOn(ISSUED));
    }

    @Test
    void ensureIsValidOnReturnsTrueOnValidUntilDate() {
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);
        assertTrue(cert.isValidOn(VALID_UNTIL));
    }

    @Test
    void ensureIsValidOnReturnsFalseBeforeIssuedDate() {
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);
        assertFalse(cert.isValidOn(LocalDate.of(2022, 12, 31)));
    }

    @Test
    void ensureIsValidOnReturnsFalseAfterValidUntilDate() {
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);
        assertFalse(cert.isValidOn(LocalDate.of(2028, 1, 2)));
    }

    // ── coversModel ───────────────────────────────────────────────────────────

    @Test
    void ensureCoversModelReturnsTrueForCorrectModelAndDate() {
        // Arrange
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);

        // Act + Assert
        assertTrue(cert.coversModel(a320, LocalDate.of(2025, 6, 1)));
    }

    @Test
    void ensureCoversModelReturnsFalseForDifferentModel() {
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);
        assertFalse(cert.coversModel(b737, LocalDate.of(2025, 6, 1)));
    }

    @Test
    void ensureCoversModelReturnsFalseForExpiredCertification() {
        final Certification cert = Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL);
        assertFalse(cert.coversModel(a320, LocalDate.of(2029, 1, 1)));
    }

    // ── Equality ──────────────────────────────────────────────────────────────

    @Test
    void ensureTwoCertificationsWithSameValuesAreEqual() {
        assertEquals(
                Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL),
                Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL)
        );
    }

    @Test
    void ensureTwoCertificationsWithDifferentModelsAreNotEqual() {
        assertNotEquals(
                Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL),
                Certification.of(b737, FlightHours.of(500), ISSUED, VALID_UNTIL)
        );
    }
}
