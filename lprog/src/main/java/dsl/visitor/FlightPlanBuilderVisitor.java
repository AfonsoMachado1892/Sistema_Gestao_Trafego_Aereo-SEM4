package dsl.visitor;

import dsl.model.ImportedCoordinate;
import dsl.model.ImportedFlightPlan;
import dsl.model.ImportedFuel;
import dsl.model.ImportedLeg;
import dsl.model.ImportedRoute;
import dsl.model.ImportedSegment;
import grammar.FlightPlanBaseVisitor;
import grammar.FlightPlanParser;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FlightPlanBuilderVisitor extends FlightPlanBaseVisitor<ImportedFlightPlan> {

    @Override
    public ImportedFlightPlan visitFlightPlan(FlightPlanParser.FlightPlanContext ctx) {
        return visitFlight(ctx.flight());
    }

    @Override
    public ImportedFlightPlan visitFlight(FlightPlanParser.FlightContext ctx) {
        String flightId = ctx.flightID().getText();
        String flightType = ctx.flightType().getText();

        ImportedRoute route = buildRoute(ctx.route());

        String aircraftRegistration = ctx.aircraft()
                .aircraftRegistration()
                .getText();

        String pilotId = ctx.pilot()
                .pilotID()
                .getText();

        List<ImportedLeg> legs = new ArrayList<>();

        for (FlightPlanParser.LegContext legContext : ctx.leg()) {
            legs.add(buildLeg(legContext));
        }

        return new ImportedFlightPlan(
                flightId,
                flightType,
                route,
                aircraftRegistration,
                pilotId,
                legs
        );
    }

    private ImportedRoute buildRoute(FlightPlanParser.RouteContext ctx) {
        String origin = ctx.origin.getText();
        String destination = ctx.destination.getText();

        return new ImportedRoute(origin, destination);
    }

    private ImportedLeg buildLeg(FlightPlanParser.LegContext ctx) {
        FlightPlanParser.DepartureContext departure = ctx.departure();
        FlightPlanParser.ArrivalContext arrival = ctx.arrival();

        String departureAirport = departure.airportCode().getText();

        LocalDateTime departureDateTime = buildDateTime(
                departure.date().getText(),
                departure.time().getText()
        );

        String arrivalAirport = arrival.airportCode().getText();

        LocalDateTime arrivalDateTime = buildDateTime(
                arrival.date().getText(),
                arrival.time().getText()
        );

        List<ImportedSegment> segments = new ArrayList<>();

        for (FlightPlanParser.SegmentContext segmentContext : ctx.segment()) {
            segments.add(buildSegment(segmentContext));
        }

        ImportedFuel fuel = buildFuel(ctx.fuel());

        return new ImportedLeg(
                departureAirport,
                departureDateTime,
                departure.start.getLine(),
                departure.start.getCharPositionInLine(),
                arrivalAirport,
                arrivalDateTime,
                arrival.start.getLine(),
                arrival.start.getCharPositionInLine(),
                segments,
                fuel
        );
    }

    private ImportedSegment buildSegment(FlightPlanParser.SegmentContext ctx) {
        ImportedCoordinate start = buildCoordinate(ctx.start);
        ImportedCoordinate end = buildCoordinate(ctx.end);

        double altitude = parseNumber(ctx.altitude().quantity.getText());
        double width = parseNumber(ctx.width().quantity.getText());

        double windDirection = parseNumber(ctx.wind().direction.getText());
        double windSpeed = parseNumber(ctx.wind().speed.getText());

        return new ImportedSegment(
                start,
                end,
                altitude,
                width,
                windDirection,
                windSpeed,
                ctx.SEGMENT().getSymbol().getLine(),
                ctx.SEGMENT().getSymbol().getCharPositionInLine()
        );
    }

    private ImportedFuel buildFuel(FlightPlanParser.FuelContext ctx) {
        double quantity = parseNumber(ctx.quantity.getText());
        String unit = ctx.unit.getText();

        return new ImportedFuel(
                quantity,
                unit,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    private ImportedCoordinate buildCoordinate(FlightPlanParser.CoordinateContext ctx) {
        double latitude = parseNumber(ctx.latitude.getText());
        double longitude = parseNumber(ctx.longitude.getText());

        return new ImportedCoordinate(latitude, longitude);
    }

    private LocalDateTime buildDateTime(String dateText, String timeText) {
        try {
            LocalDate date = LocalDate.parse(dateText);
            LocalTime time = LocalTime.parse(timeText);

            return LocalDateTime.of(date, time);
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private double parseNumber(String text) {
        return Double.parseDouble(text);
    }
}