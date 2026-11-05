package aisafe.persistence.impl.jpa;

import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaAirTransportCompanyRepository
        extends AiSafeJpaRepositoryBase<AirTransportCompany, AirTransportCompanyCode, AirTransportCompanyCode>
        implements AirTransportCompanyRepository {

    public JpaAirTransportCompanyRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "iataCode");
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(final String icaoCode) {
        final TypedQuery<AirTransportCompany> query = entityManager().createQuery(
                "SELECT c FROM AirTransportCompany c WHERE c.icaoCode = :icaoCode",
                AirTransportCompany.class);
        query.setParameter("icaoCode", icaoCode.toUpperCase());
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<AirTransportCompany> findByIataCode(final AirTransportCompanyCode iataCode) {
        final var query = entityManager().createQuery(
                "SELECT c FROM AirTransportCompany c WHERE c.iataCode = :iataCode",
                AirTransportCompany.class
        );
        query.setParameter("iataCode", iataCode);
        return query.getResultList().stream().findFirst();
    }
}