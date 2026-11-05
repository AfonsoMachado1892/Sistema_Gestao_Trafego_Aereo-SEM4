package aisafe.pilot.domain;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Pilot}.
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class PilotTest {

    private static final AirTransportCompanyCode TP = AirTransportCompanyCode.of("TP");
    private static final LocalDate ISSUED      = LocalDate.of(2023, 1, 1);
    private static final LocalDate VALID_UNTIL = LocalDate.of(2028, 1, 1);

    private AircraftModel a320;
    private AircraftModel b737;
    private SystemUser systemUser;
    private SystemUser otherSystemUser;
    private Set<Certification> certifications;

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
        systemUser = UserBuilderHelper.builder()
                .with("pilot1", "Password1", "António", "Silva", "antonio.silva@tap.pt")
                .withRoles(Set.of())
                .build();
        otherSystemUser = UserBuilderHelper.builder()
                .with("pilot2", "Password1", "Maria", "Santos", "maria.santos@tap.pt")
                .withRoles(Set.of())
                .build();
        certifications = Set.of(Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL));
    }

    private Pilot validPilot() {
        return new PilotFactory()
                .withSystemUser(systemUser)
                .withCompany(TP)
                .withCertifications(certifications)
                .build();
    }

    // ── Valid construction ───────────────────────────────────────────────────

    @Test
    void ensureValidPilotIsCreatedWithActiveStatus() {
        // Act
        final Pilot pilot = validPilot();

        // Assert
        assertEquals(PilotStatus.ACTIVE, pilot.status());
        assertTrue(pilot.isActive());
    }

    @Test
    void ensureSystemUserIsStored() {
        assertEquals(systemUser, validPilot().systemUser());
    }

    @Test
    void ensureCompanyIataCodeIsStored() {
        assertEquals(TP, validPilot().currentCompanyIataCode());
    }

    @Test
    void ensureCertificationsAreStored() {
        assertEquals(1, validPilot().certifications().size());
    }

    @Test
    void ensureCertificationsAreUnmodifiable() {
        // Arrange
        final Pilot pilot = validPilot();
        final Certification extra = Certification.of(b737, FlightHours.of(100), ISSUED, VALID_UNTIL);

        // Act + Assert
        assertThrows(UnsupportedOperationException.class,
                () -> pilot.certifications().add(extra));
    }

    @Test
    void ensureIdentityReturnsNonNullPilotID() {
        assertNotNull(validPilot().identity());
    }

    // ── Invariants ───────────────────────────────────────────────────────────

    @Test
    void ensureNullSystemUserIsRejected() {
        assertThrows(Exception.class, () ->
                new PilotFactory()
                        .withSystemUser(null)
                        .withCompany(TP)
                        .withCertifications(certifications)
                        .build());
    }

    @Test
    void ensureNullCompanyIsRejected() {
        assertThrows(Exception.class, () ->
                new PilotFactory()
                        .withSystemUser(systemUser)
                        .withCompany(null)
                        .withCertifications(certifications)
                        .build());
    }

    @Test
    void ensureEmptyCertificationsAreRejected() {
        assertThrows(Exception.class, () ->
                new PilotFactory()
                        .withSystemUser(systemUser)
                        .withCompany(TP)
                        .withCertifications(Collections.emptySet())
                        .build());
    }

    @Test
    void ensureNullCertificationsAreRejected() {
        assertThrows(Exception.class, () ->
                new PilotFactory()
                        .withSystemUser(systemUser)
                        .withCompany(TP)
                        .withCertifications(null)
                        .build());
    }

    // ── isCertifiedFor ───────────────────────────────────────────────────────

    @Test
    void ensureIsCertifiedForReturnsTrueForValidModelAndDate() {
        // Arrange
        final Pilot pilot = validPilot();

        // Act + Assert
        assertTrue(pilot.isCertifiedFor(a320, LocalDate.of(2025, 6, 1)));
    }

    @Test
    void ensureIsCertifiedForReturnsFalseForUnknownModel() {
        assertFalse(validPilot().isCertifiedFor(b737, LocalDate.of(2025, 6, 1)));
    }

    @Test
    void ensureIsCertifiedForReturnsFalseForExpiredCertification() {
        assertFalse(validPilot().isCertifiedFor(a320, LocalDate.of(2029, 1, 1)));
    }

    // ── deactivate ────────────────────────────────────────────────────────────

    @Test
    void ensureDeactivateSetsStatusToInactive() {
        // Arrange
        final Pilot pilot = validPilot();

        // Act
        pilot.deactivate();

        // Assert
        assertEquals(PilotStatus.INACTIVE, pilot.status());
        assertFalse(pilot.isActive());
    }

    @Test
    void ensureDeactivatingAlreadyInactivePilotIsRejected() {
        // Arrange
        final Pilot pilot = validPilot();
        pilot.deactivate();

        // Act + Assert
        assertThrows(IllegalArgumentException.class, pilot::deactivate);
    }

    // ── sameAs ────────────────────────────────────────────────────────────────

    @Test
    void ensureSameAsReturnsTrueForSamePilotID() {
        final Pilot pilot = validPilot();
        assertTrue(pilot.sameAs(pilot));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentPilot() {
        // Arrange
        final Pilot a = validPilot();
        final Pilot b = new PilotFactory()
                .withSystemUser(otherSystemUser)
                .withCompany(TP)
                .withCertifications(certifications)
                .build();

        // Act + Assert
        assertFalse(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForNonPilotObject() {
        assertFalse(validPilot().sameAs("not a pilot"));
    }

    @Test
    void ensureToStringContainsCompanyIata() {
        assertTrue(validPilot().toString().contains("TP"));
    }
}
