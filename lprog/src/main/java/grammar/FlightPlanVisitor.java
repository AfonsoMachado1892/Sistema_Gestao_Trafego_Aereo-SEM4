// Generated from ./grammar/FlightPlan.g4 by ANTLR 4.13.1
package grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FlightPlanParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FlightPlanVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#flightPlan}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlightPlan(FlightPlanParser.FlightPlanContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#flight}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlight(FlightPlanParser.FlightContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#aircraft}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAircraft(FlightPlanParser.AircraftContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#aircraftRegistration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAircraftRegistration(FlightPlanParser.AircraftRegistrationContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#route}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoute(FlightPlanParser.RouteContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#pilot}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPilot(FlightPlanParser.PilotContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#leg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeg(FlightPlanParser.LegContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#departure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeparture(FlightPlanParser.DepartureContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#arrival}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrival(FlightPlanParser.ArrivalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#segment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSegment(FlightPlanParser.SegmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#fuel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuel(FlightPlanParser.FuelContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#altitude}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltitude(FlightPlanParser.AltitudeContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#width}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWidth(FlightPlanParser.WidthContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#wind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWind(FlightPlanParser.WindContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#measureUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeasureUnit(FlightPlanParser.MeasureUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#fuelUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuelUnit(FlightPlanParser.FuelUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#flightID}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlightID(FlightPlanParser.FlightIDContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#flightType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlightType(FlightPlanParser.FlightTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#airportCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAirportCode(FlightPlanParser.AirportCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#pilotID}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPilotID(FlightPlanParser.PilotIDContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#date}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDate(FlightPlanParser.DateContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#time}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTime(FlightPlanParser.TimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#coordinate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCoordinate(FlightPlanParser.CoordinateContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlightPlanParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(FlightPlanParser.NumberContext ctx);
}