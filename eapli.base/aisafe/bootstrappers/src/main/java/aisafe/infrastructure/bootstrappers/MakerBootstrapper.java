package aisafe.infrastructure.bootstrappers;

import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.maker.domain.MakerType;
import aisafe.persistence.PersistenceContext;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import eapli.framework.actions.Action;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MakerBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(MakerBootstrapper.class);

    private final MakerRepository makerRepository =
            PersistenceContext.repositories().makers();

    @Override
    public boolean execute() {
        registerMaker("CFM", "CFM International",
                "FR", "France", Set.of(MakerType.ENGINE));
        registerMaker("GE", "GE Aviation",
                "US", "United States", Set.of(MakerType.ENGINE));
        registerMaker("RR", "Rolls-Royce",
                "GB", "United Kingdom", Set.of(MakerType.ENGINE, MakerType.AIRCRAFT));
        registerMaker("PW", "Pratt & Whitney",
                "US", "United States", Set.of(MakerType.ENGINE));
        registerMaker("AIRBUS", "Airbus",
                "FR", "France", Set.of(MakerType.AIRCRAFT));
        registerMaker("BOEING", "Boeing",
                "US", "United States", Set.of(MakerType.AIRCRAFT));
        return true;
    }

    private void registerMaker(final String id, final String name,
                               final String isoCode, final String countryName,
                               final Set<MakerType> types) {
        try {
            if (makerRepository.findByName(name).isPresent()) {
                LOGGER.debug("»»» Maker already exists: {}", name);
                return;
            }

            final var maker = new Maker(
                    MakerID.of(id),
                    name,
                    new Country(IsoCode.of(isoCode), countryName),
                    types
            );

            makerRepository.save(maker);
            LOGGER.debug("»»» Maker: {}", name);
        } catch (final IntegrityViolationException | ConcurrencyException e) {
            LOGGER.warn("Maker already exists: {}", name);
        }
    }
}