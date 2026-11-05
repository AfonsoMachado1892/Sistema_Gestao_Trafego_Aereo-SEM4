package dsl.listener;

import grammar.FlightPlanBaseListener;
import grammar.FlightPlanParser;

public class FlightPlanStructureListener extends FlightPlanBaseListener {

    private int flightCount;
    private int legCount;
    private int segmentCount;

    @Override
    public void enterFlight(FlightPlanParser.FlightContext ctx) {
        flightCount++;
    }

    @Override
    public void enterLeg(FlightPlanParser.LegContext ctx) {
        legCount++;
    }

    @Override
    public void enterSegment(FlightPlanParser.SegmentContext ctx) {
        segmentCount++;
    }

    public int flightCount() {
        return flightCount;
    }

    public int legCount() {
        return legCount;
    }

    public int segmentCount() {
        return segmentCount;
    }
}