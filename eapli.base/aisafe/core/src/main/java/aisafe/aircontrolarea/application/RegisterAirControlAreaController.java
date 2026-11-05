package aisafe.aircontrolarea.application;

import aisafe.aircontrolarea.domain.*;
import aisafe.sharedkernel.Coordinates;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.ArrayList;
import java.util.List;

@UseCaseController
public class RegisterAirControlAreaController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AirControlAreaService airControlAreaService;
    private final AirControlAreaRepository airControlAreaRepository;

    public RegisterAirControlAreaController(
            final AirControlAreaRepository airControlAreaRepository) {
        this.airControlAreaService = new AirControlAreaService(airControlAreaRepository);
        this.airControlAreaRepository = airControlAreaRepository;
    }

    public AirControlArea registerAirControlArea(
            final String code,
            final String name,
            final GeographicBoundary geographicBoundary) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airControlAreaService.registerAirControlArea(
                AirControlAreaCode.of(code),
                geographicBoundary,
                AirControlAreaName.of(name));
    }

    public Iterable<AirControlArea> listAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airControlAreaService.listAirControlAreas();
    }

    public boolean codeAlreadyExists(final String code) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airControlAreaRepository
                .findByCode(AirControlAreaCode.of(code))
                .isPresent();
    }

    public boolean nameAlreadyExists(final String name) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airControlAreaRepository
                .findByName(AirControlAreaName.of(name))
                .isPresent();
    }

    public List<AirControlArea> findOverlappingAreas(
            final double swLat, final double swLon,
            final double neLat, final double neLon) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        final GeographicBoundary boundary = GeographicBoundary.of(
                Coordinates.of(swLat, swLon),
                Coordinates.of(neLat, neLon));

        final AirControlArea candidate = new AirControlArea(
                AirControlAreaCode.of("_TEMP"),
                boundary,
                AirControlAreaName.of("_TEMP"));

        final List<AirControlArea> overlapping = new ArrayList<>();
        for (final AirControlArea existing : airControlAreaRepository.findAll()) {
            if (existing.overlapsWith(candidate)) {
                overlapping.add(existing);
            }
        }
        return overlapping;
    }
}