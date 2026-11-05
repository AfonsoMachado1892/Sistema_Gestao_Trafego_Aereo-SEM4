package aisafe.aircontrolarea.domain;

import eapli.framework.domain.model.DomainFactory;
import eapli.framework.validations.Preconditions;

public class AirControlAreaFactory implements DomainFactory<AirControlArea> {

    private AirControlAreaCode code;
    private GeographicBoundary geographicBoundary;
    private AirControlAreaName name;

    public AirControlAreaFactory withCode(AirControlAreaCode code){

        this.code = code;
        return this;

    }

    public AirControlAreaFactory withGeographicBoundary(GeographicBoundary geographicBoundary){

        this.geographicBoundary = geographicBoundary;
        return this;

    }

    public AirControlAreaFactory withName(AirControlAreaName name){

        this.name = name;
        return this;

    }

    @Override
    public AirControlArea build() {
        return new AirControlArea(code, geographicBoundary, name);
    }
}
