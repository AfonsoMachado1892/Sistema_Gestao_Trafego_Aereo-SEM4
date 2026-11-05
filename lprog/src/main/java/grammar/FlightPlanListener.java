// Generated from ./grammar/FlightPlan.g4 by ANTLR 4.13.1
package grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FlightPlanParser}.
 */
public interface FlightPlanListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#flightPlan}.
	 * @param ctx the parse tree
	 */
	void enterFlightPlan(FlightPlanParser.FlightPlanContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#flightPlan}.
	 * @param ctx the parse tree
	 */
	void exitFlightPlan(FlightPlanParser.FlightPlanContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#flight}.
	 * @param ctx the parse tree
	 */
	void enterFlight(FlightPlanParser.FlightContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#flight}.
	 * @param ctx the parse tree
	 */
	void exitFlight(FlightPlanParser.FlightContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#aircraft}.
	 * @param ctx the parse tree
	 */
	void enterAircraft(FlightPlanParser.AircraftContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#aircraft}.
	 * @param ctx the parse tree
	 */
	void exitAircraft(FlightPlanParser.AircraftContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#aircraftRegistration}.
	 * @param ctx the parse tree
	 */
	void enterAircraftRegistration(FlightPlanParser.AircraftRegistrationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#aircraftRegistration}.
	 * @param ctx the parse tree
	 */
	void exitAircraftRegistration(FlightPlanParser.AircraftRegistrationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#route}.
	 * @param ctx the parse tree
	 */
	void enterRoute(FlightPlanParser.RouteContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#route}.
	 * @param ctx the parse tree
	 */
	void exitRoute(FlightPlanParser.RouteContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#pilot}.
	 * @param ctx the parse tree
	 */
	void enterPilot(FlightPlanParser.PilotContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#pilot}.
	 * @param ctx the parse tree
	 */
	void exitPilot(FlightPlanParser.PilotContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#leg}.
	 * @param ctx the parse tree
	 */
	void enterLeg(FlightPlanParser.LegContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#leg}.
	 * @param ctx the parse tree
	 */
	void exitLeg(FlightPlanParser.LegContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#departure}.
	 * @param ctx the parse tree
	 */
	void enterDeparture(FlightPlanParser.DepartureContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#departure}.
	 * @param ctx the parse tree
	 */
	void exitDeparture(FlightPlanParser.DepartureContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#arrival}.
	 * @param ctx the parse tree
	 */
	void enterArrival(FlightPlanParser.ArrivalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#arrival}.
	 * @param ctx the parse tree
	 */
	void exitArrival(FlightPlanParser.ArrivalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#segment}.
	 * @param ctx the parse tree
	 */
	void enterSegment(FlightPlanParser.SegmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#segment}.
	 * @param ctx the parse tree
	 */
	void exitSegment(FlightPlanParser.SegmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#fuel}.
	 * @param ctx the parse tree
	 */
	void enterFuel(FlightPlanParser.FuelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#fuel}.
	 * @param ctx the parse tree
	 */
	void exitFuel(FlightPlanParser.FuelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#altitude}.
	 * @param ctx the parse tree
	 */
	void enterAltitude(FlightPlanParser.AltitudeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#altitude}.
	 * @param ctx the parse tree
	 */
	void exitAltitude(FlightPlanParser.AltitudeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#width}.
	 * @param ctx the parse tree
	 */
	void enterWidth(FlightPlanParser.WidthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#width}.
	 * @param ctx the parse tree
	 */
	void exitWidth(FlightPlanParser.WidthContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#wind}.
	 * @param ctx the parse tree
	 */
	void enterWind(FlightPlanParser.WindContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#wind}.
	 * @param ctx the parse tree
	 */
	void exitWind(FlightPlanParser.WindContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#measureUnit}.
	 * @param ctx the parse tree
	 */
	void enterMeasureUnit(FlightPlanParser.MeasureUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#measureUnit}.
	 * @param ctx the parse tree
	 */
	void exitMeasureUnit(FlightPlanParser.MeasureUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#fuelUnit}.
	 * @param ctx the parse tree
	 */
	void enterFuelUnit(FlightPlanParser.FuelUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#fuelUnit}.
	 * @param ctx the parse tree
	 */
	void exitFuelUnit(FlightPlanParser.FuelUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#flightID}.
	 * @param ctx the parse tree
	 */
	void enterFlightID(FlightPlanParser.FlightIDContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#flightID}.
	 * @param ctx the parse tree
	 */
	void exitFlightID(FlightPlanParser.FlightIDContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#flightType}.
	 * @param ctx the parse tree
	 */
	void enterFlightType(FlightPlanParser.FlightTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#flightType}.
	 * @param ctx the parse tree
	 */
	void exitFlightType(FlightPlanParser.FlightTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#airportCode}.
	 * @param ctx the parse tree
	 */
	void enterAirportCode(FlightPlanParser.AirportCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#airportCode}.
	 * @param ctx the parse tree
	 */
	void exitAirportCode(FlightPlanParser.AirportCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#pilotID}.
	 * @param ctx the parse tree
	 */
	void enterPilotID(FlightPlanParser.PilotIDContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#pilotID}.
	 * @param ctx the parse tree
	 */
	void exitPilotID(FlightPlanParser.PilotIDContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#date}.
	 * @param ctx the parse tree
	 */
	void enterDate(FlightPlanParser.DateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#date}.
	 * @param ctx the parse tree
	 */
	void exitDate(FlightPlanParser.DateContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#time}.
	 * @param ctx the parse tree
	 */
	void enterTime(FlightPlanParser.TimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#time}.
	 * @param ctx the parse tree
	 */
	void exitTime(FlightPlanParser.TimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#coordinate}.
	 * @param ctx the parse tree
	 */
	void enterCoordinate(FlightPlanParser.CoordinateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#coordinate}.
	 * @param ctx the parse tree
	 */
	void exitCoordinate(FlightPlanParser.CoordinateContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlightPlanParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(FlightPlanParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlightPlanParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(FlightPlanParser.NumberContext ctx);
}