// aisafe/core/src/main/java/aisafe/airtransportcompany/domain/AirTransportCompanyRepository.java
package aisafe.airtransportcompany.domain;

import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

public interface AirTransportCompanyRepository
        extends Repository<AirTransportCompany, AirTransportCompanyCode> {

    Optional<AirTransportCompany> findByIcaoCode(String icaoCode);

    Optional<AirTransportCompany> findByIataCode(AirTransportCompanyCode iataCode);}