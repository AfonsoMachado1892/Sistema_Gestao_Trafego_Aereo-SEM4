package aisafe.maker.application;

import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.Set;

@UseCaseController
public class RegisterMakerController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final MakerRepository makerRepository;

    public RegisterMakerController(final MakerRepository makerRepository) {
        this.makerRepository = makerRepository;
    }

    public Maker registerMaker(final String makerID, final String name,
                               final String isoCode, final String countryName,
                               final Set<MakerType> types) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        final var maker = new Maker(
                MakerID.of(makerID),
                name,
                new Country(IsoCode.of(isoCode), countryName),
                types
        );

        try {
            return makerRepository.save(maker);
        } catch (IntegrityViolationException | ConcurrencyException e) {
            throw new IllegalStateException("Maker already exists: " + makerID, e);
        }
    }

    public Iterable<Maker> allMakers() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);
        return makerRepository.findAll();
    }
}