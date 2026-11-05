// Generated from ./grammar/FlightPlan.g4 by ANTLR 4.13.1
package grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class FlightPlanParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		FLIGHT=1, ROUTE=2, AIRCRAFT=3, PILOT=4, LEG=5, DEPARTURE=6, ARRIVAL=7, 
		SEGMENT=8, FUEL=9, AT=10, ALTITUDE=11, WIDTH=12, WIND=13, REGULAR=14, 
		CHARTER=15, KG=16, L=17, M=18, DEG=19, MS=20, LBRACE=21, RBRACE=22, LPAREN=23, 
		RPAREN=24, COMMA=25, SEMI=26, ARROW=27, DATE=28, TIME=29, FLIGHT_ID=30, 
		AIRPORT_CODE=31, AIRCRAFT_REGISTRATION=32, PILOT_ID=33, FLOAT=34, INT=35, 
		WS=36;
	public static final int
		RULE_flightPlan = 0, RULE_flight = 1, RULE_aircraft = 2, RULE_aircraftRegistration = 3, 
		RULE_route = 4, RULE_pilot = 5, RULE_leg = 6, RULE_departure = 7, RULE_arrival = 8, 
		RULE_segment = 9, RULE_fuel = 10, RULE_altitude = 11, RULE_width = 12, 
		RULE_wind = 13, RULE_measureUnit = 14, RULE_fuelUnit = 15, RULE_flightID = 16, 
		RULE_flightType = 17, RULE_airportCode = 18, RULE_pilotID = 19, RULE_date = 20, 
		RULE_time = 21, RULE_coordinate = 22, RULE_number = 23;
	private static String[] makeRuleNames() {
		return new String[] {
			"flightPlan", "flight", "aircraft", "aircraftRegistration", "route", 
			"pilot", "leg", "departure", "arrival", "segment", "fuel", "altitude", 
			"width", "wind", "measureUnit", "fuelUnit", "flightID", "flightType", 
			"airportCode", "pilotID", "date", "time", "coordinate", "number"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, "'{'", "'}'", "'('", 
			"')'", "','", "';'", "'->'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "FLIGHT", "ROUTE", "AIRCRAFT", "PILOT", "LEG", "DEPARTURE", "ARRIVAL", 
			"SEGMENT", "FUEL", "AT", "ALTITUDE", "WIDTH", "WIND", "REGULAR", "CHARTER", 
			"KG", "L", "M", "DEG", "MS", "LBRACE", "RBRACE", "LPAREN", "RPAREN", 
			"COMMA", "SEMI", "ARROW", "DATE", "TIME", "FLIGHT_ID", "AIRPORT_CODE", 
			"AIRCRAFT_REGISTRATION", "PILOT_ID", "FLOAT", "INT", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "FlightPlan.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public FlightPlanParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FlightPlanContext extends ParserRuleContext {
		public FlightContext flight() {
			return getRuleContext(FlightContext.class,0);
		}
		public TerminalNode EOF() { return getToken(FlightPlanParser.EOF, 0); }
		public FlightPlanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flightPlan; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterFlightPlan(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitFlightPlan(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitFlightPlan(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlightPlanContext flightPlan() throws RecognitionException {
		FlightPlanContext _localctx = new FlightPlanContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_flightPlan);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			flight();
			setState(49);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FlightContext extends ParserRuleContext {
		public TerminalNode FLIGHT() { return getToken(FlightPlanParser.FLIGHT, 0); }
		public FlightIDContext flightID() {
			return getRuleContext(FlightIDContext.class,0);
		}
		public FlightTypeContext flightType() {
			return getRuleContext(FlightTypeContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(FlightPlanParser.LBRACE, 0); }
		public RouteContext route() {
			return getRuleContext(RouteContext.class,0);
		}
		public AircraftContext aircraft() {
			return getRuleContext(AircraftContext.class,0);
		}
		public PilotContext pilot() {
			return getRuleContext(PilotContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(FlightPlanParser.RBRACE, 0); }
		public List<LegContext> leg() {
			return getRuleContexts(LegContext.class);
		}
		public LegContext leg(int i) {
			return getRuleContext(LegContext.class,i);
		}
		public FlightContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flight; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterFlight(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitFlight(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitFlight(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlightContext flight() throws RecognitionException {
		FlightContext _localctx = new FlightContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_flight);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			match(FLIGHT);
			setState(52);
			flightID();
			setState(53);
			flightType();
			setState(54);
			match(LBRACE);
			setState(55);
			route();
			setState(56);
			aircraft();
			setState(57);
			pilot();
			setState(59); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(58);
				leg();
				}
				}
				setState(61); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==LEG );
			setState(63);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AircraftContext extends ParserRuleContext {
		public TerminalNode AIRCRAFT() { return getToken(FlightPlanParser.AIRCRAFT, 0); }
		public AircraftRegistrationContext aircraftRegistration() {
			return getRuleContext(AircraftRegistrationContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public AircraftContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aircraft; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterAircraft(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitAircraft(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitAircraft(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AircraftContext aircraft() throws RecognitionException {
		AircraftContext _localctx = new AircraftContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_aircraft);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(AIRCRAFT);
			setState(66);
			aircraftRegistration();
			setState(67);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AircraftRegistrationContext extends ParserRuleContext {
		public TerminalNode AIRCRAFT_REGISTRATION() { return getToken(FlightPlanParser.AIRCRAFT_REGISTRATION, 0); }
		public AircraftRegistrationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aircraftRegistration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterAircraftRegistration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitAircraftRegistration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitAircraftRegistration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AircraftRegistrationContext aircraftRegistration() throws RecognitionException {
		AircraftRegistrationContext _localctx = new AircraftRegistrationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_aircraftRegistration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			match(AIRCRAFT_REGISTRATION);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RouteContext extends ParserRuleContext {
		public AirportCodeContext origin;
		public AirportCodeContext destination;
		public TerminalNode ROUTE() { return getToken(FlightPlanParser.ROUTE, 0); }
		public TerminalNode ARROW() { return getToken(FlightPlanParser.ARROW, 0); }
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public List<AirportCodeContext> airportCode() {
			return getRuleContexts(AirportCodeContext.class);
		}
		public AirportCodeContext airportCode(int i) {
			return getRuleContext(AirportCodeContext.class,i);
		}
		public RouteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_route; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterRoute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitRoute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitRoute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RouteContext route() throws RecognitionException {
		RouteContext _localctx = new RouteContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_route);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			match(ROUTE);
			setState(72);
			((RouteContext)_localctx).origin = airportCode();
			setState(73);
			match(ARROW);
			setState(74);
			((RouteContext)_localctx).destination = airportCode();
			setState(75);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PilotContext extends ParserRuleContext {
		public TerminalNode PILOT() { return getToken(FlightPlanParser.PILOT, 0); }
		public PilotIDContext pilotID() {
			return getRuleContext(PilotIDContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public PilotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pilot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterPilot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitPilot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitPilot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PilotContext pilot() throws RecognitionException {
		PilotContext _localctx = new PilotContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_pilot);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			match(PILOT);
			setState(78);
			pilotID();
			setState(79);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LegContext extends ParserRuleContext {
		public TerminalNode LEG() { return getToken(FlightPlanParser.LEG, 0); }
		public TerminalNode LBRACE() { return getToken(FlightPlanParser.LBRACE, 0); }
		public DepartureContext departure() {
			return getRuleContext(DepartureContext.class,0);
		}
		public ArrivalContext arrival() {
			return getRuleContext(ArrivalContext.class,0);
		}
		public FuelContext fuel() {
			return getRuleContext(FuelContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(FlightPlanParser.RBRACE, 0); }
		public List<SegmentContext> segment() {
			return getRuleContexts(SegmentContext.class);
		}
		public SegmentContext segment(int i) {
			return getRuleContext(SegmentContext.class,i);
		}
		public LegContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_leg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterLeg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitLeg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitLeg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LegContext leg() throws RecognitionException {
		LegContext _localctx = new LegContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_leg);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			match(LEG);
			setState(82);
			match(LBRACE);
			setState(83);
			departure();
			setState(84);
			arrival();
			setState(86); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(85);
				segment();
				}
				}
				setState(88); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==SEGMENT );
			setState(90);
			fuel();
			setState(91);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DepartureContext extends ParserRuleContext {
		public TerminalNode DEPARTURE() { return getToken(FlightPlanParser.DEPARTURE, 0); }
		public AirportCodeContext airportCode() {
			return getRuleContext(AirportCodeContext.class,0);
		}
		public TerminalNode AT() { return getToken(FlightPlanParser.AT, 0); }
		public DateContext date() {
			return getRuleContext(DateContext.class,0);
		}
		public TimeContext time() {
			return getRuleContext(TimeContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public DepartureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_departure; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterDeparture(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitDeparture(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitDeparture(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DepartureContext departure() throws RecognitionException {
		DepartureContext _localctx = new DepartureContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_departure);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			match(DEPARTURE);
			setState(94);
			airportCode();
			setState(95);
			match(AT);
			setState(96);
			date();
			setState(97);
			time();
			setState(98);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrivalContext extends ParserRuleContext {
		public TerminalNode ARRIVAL() { return getToken(FlightPlanParser.ARRIVAL, 0); }
		public AirportCodeContext airportCode() {
			return getRuleContext(AirportCodeContext.class,0);
		}
		public TerminalNode AT() { return getToken(FlightPlanParser.AT, 0); }
		public DateContext date() {
			return getRuleContext(DateContext.class,0);
		}
		public TimeContext time() {
			return getRuleContext(TimeContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public ArrivalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrival; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterArrival(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitArrival(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitArrival(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrivalContext arrival() throws RecognitionException {
		ArrivalContext _localctx = new ArrivalContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_arrival);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(ARRIVAL);
			setState(101);
			airportCode();
			setState(102);
			match(AT);
			setState(103);
			date();
			setState(104);
			time();
			setState(105);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SegmentContext extends ParserRuleContext {
		public CoordinateContext start;
		public CoordinateContext end;
		public TerminalNode SEGMENT() { return getToken(FlightPlanParser.SEGMENT, 0); }
		public TerminalNode LBRACE() { return getToken(FlightPlanParser.LBRACE, 0); }
		public TerminalNode ARROW() { return getToken(FlightPlanParser.ARROW, 0); }
		public AltitudeContext altitude() {
			return getRuleContext(AltitudeContext.class,0);
		}
		public WidthContext width() {
			return getRuleContext(WidthContext.class,0);
		}
		public WindContext wind() {
			return getRuleContext(WindContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(FlightPlanParser.RBRACE, 0); }
		public List<CoordinateContext> coordinate() {
			return getRuleContexts(CoordinateContext.class);
		}
		public CoordinateContext coordinate(int i) {
			return getRuleContext(CoordinateContext.class,i);
		}
		public SegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_segment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterSegment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitSegment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SegmentContext segment() throws RecognitionException {
		SegmentContext _localctx = new SegmentContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_segment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			match(SEGMENT);
			setState(108);
			match(LBRACE);
			setState(109);
			((SegmentContext)_localctx).start = coordinate();
			setState(110);
			match(ARROW);
			setState(111);
			((SegmentContext)_localctx).end = coordinate();
			setState(112);
			altitude();
			setState(113);
			width();
			setState(114);
			wind();
			setState(115);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuelContext extends ParserRuleContext {
		public NumberContext quantity;
		public FuelUnitContext unit;
		public TerminalNode FUEL() { return getToken(FlightPlanParser.FUEL, 0); }
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public FuelUnitContext fuelUnit() {
			return getRuleContext(FuelUnitContext.class,0);
		}
		public FuelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fuel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterFuel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitFuel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitFuel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuelContext fuel() throws RecognitionException {
		FuelContext _localctx = new FuelContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_fuel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(FUEL);
			setState(118);
			((FuelContext)_localctx).quantity = number();
			setState(119);
			((FuelContext)_localctx).unit = fuelUnit();
			setState(120);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AltitudeContext extends ParserRuleContext {
		public NumberContext quantity;
		public MeasureUnitContext unit;
		public TerminalNode ALTITUDE() { return getToken(FlightPlanParser.ALTITUDE, 0); }
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public MeasureUnitContext measureUnit() {
			return getRuleContext(MeasureUnitContext.class,0);
		}
		public AltitudeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_altitude; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterAltitude(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitAltitude(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitAltitude(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AltitudeContext altitude() throws RecognitionException {
		AltitudeContext _localctx = new AltitudeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_altitude);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			match(ALTITUDE);
			setState(123);
			((AltitudeContext)_localctx).quantity = number();
			setState(124);
			((AltitudeContext)_localctx).unit = measureUnit();
			setState(125);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WidthContext extends ParserRuleContext {
		public NumberContext quantity;
		public MeasureUnitContext unit;
		public TerminalNode WIDTH() { return getToken(FlightPlanParser.WIDTH, 0); }
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public MeasureUnitContext measureUnit() {
			return getRuleContext(MeasureUnitContext.class,0);
		}
		public WidthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_width; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterWidth(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitWidth(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitWidth(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WidthContext width() throws RecognitionException {
		WidthContext _localctx = new WidthContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_width);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(WIDTH);
			setState(128);
			((WidthContext)_localctx).quantity = number();
			setState(129);
			((WidthContext)_localctx).unit = measureUnit();
			setState(130);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WindContext extends ParserRuleContext {
		public NumberContext direction;
		public NumberContext speed;
		public TerminalNode WIND() { return getToken(FlightPlanParser.WIND, 0); }
		public TerminalNode DEG() { return getToken(FlightPlanParser.DEG, 0); }
		public TerminalNode MS() { return getToken(FlightPlanParser.MS, 0); }
		public TerminalNode SEMI() { return getToken(FlightPlanParser.SEMI, 0); }
		public List<NumberContext> number() {
			return getRuleContexts(NumberContext.class);
		}
		public NumberContext number(int i) {
			return getRuleContext(NumberContext.class,i);
		}
		public WindContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wind; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterWind(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitWind(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitWind(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindContext wind() throws RecognitionException {
		WindContext _localctx = new WindContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_wind);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			match(WIND);
			setState(133);
			((WindContext)_localctx).direction = number();
			setState(134);
			match(DEG);
			setState(135);
			((WindContext)_localctx).speed = number();
			setState(136);
			match(MS);
			setState(137);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MeasureUnitContext extends ParserRuleContext {
		public TerminalNode M() { return getToken(FlightPlanParser.M, 0); }
		public MeasureUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_measureUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterMeasureUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitMeasureUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitMeasureUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MeasureUnitContext measureUnit() throws RecognitionException {
		MeasureUnitContext _localctx = new MeasureUnitContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_measureUnit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(M);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuelUnitContext extends ParserRuleContext {
		public TerminalNode KG() { return getToken(FlightPlanParser.KG, 0); }
		public TerminalNode L() { return getToken(FlightPlanParser.L, 0); }
		public FuelUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fuelUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterFuelUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitFuelUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitFuelUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuelUnitContext fuelUnit() throws RecognitionException {
		FuelUnitContext _localctx = new FuelUnitContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_fuelUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			_la = _input.LA(1);
			if ( !(_la==KG || _la==L) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FlightIDContext extends ParserRuleContext {
		public TerminalNode FLIGHT_ID() { return getToken(FlightPlanParser.FLIGHT_ID, 0); }
		public FlightIDContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flightID; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterFlightID(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitFlightID(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitFlightID(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlightIDContext flightID() throws RecognitionException {
		FlightIDContext _localctx = new FlightIDContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_flightID);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			match(FLIGHT_ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FlightTypeContext extends ParserRuleContext {
		public TerminalNode REGULAR() { return getToken(FlightPlanParser.REGULAR, 0); }
		public TerminalNode CHARTER() { return getToken(FlightPlanParser.CHARTER, 0); }
		public FlightTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flightType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterFlightType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitFlightType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitFlightType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlightTypeContext flightType() throws RecognitionException {
		FlightTypeContext _localctx = new FlightTypeContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_flightType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			_la = _input.LA(1);
			if ( !(_la==REGULAR || _la==CHARTER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AirportCodeContext extends ParserRuleContext {
		public TerminalNode AIRPORT_CODE() { return getToken(FlightPlanParser.AIRPORT_CODE, 0); }
		public AirportCodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_airportCode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterAirportCode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitAirportCode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitAirportCode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AirportCodeContext airportCode() throws RecognitionException {
		AirportCodeContext _localctx = new AirportCodeContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_airportCode);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			match(AIRPORT_CODE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PilotIDContext extends ParserRuleContext {
		public TerminalNode PILOT_ID() { return getToken(FlightPlanParser.PILOT_ID, 0); }
		public PilotIDContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pilotID; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterPilotID(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitPilotID(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitPilotID(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PilotIDContext pilotID() throws RecognitionException {
		PilotIDContext _localctx = new PilotIDContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_pilotID);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			match(PILOT_ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DateContext extends ParserRuleContext {
		public TerminalNode DATE() { return getToken(FlightPlanParser.DATE, 0); }
		public DateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_date; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitDate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitDate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateContext date() throws RecognitionException {
		DateContext _localctx = new DateContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_date);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			match(DATE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TimeContext extends ParserRuleContext {
		public TerminalNode TIME() { return getToken(FlightPlanParser.TIME, 0); }
		public TimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_time; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitTime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitTime(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeContext time() throws RecognitionException {
		TimeContext _localctx = new TimeContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_time);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			match(TIME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CoordinateContext extends ParserRuleContext {
		public NumberContext latitude;
		public NumberContext longitude;
		public TerminalNode LPAREN() { return getToken(FlightPlanParser.LPAREN, 0); }
		public TerminalNode COMMA() { return getToken(FlightPlanParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(FlightPlanParser.RPAREN, 0); }
		public List<NumberContext> number() {
			return getRuleContexts(NumberContext.class);
		}
		public NumberContext number(int i) {
			return getRuleContext(NumberContext.class,i);
		}
		public CoordinateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coordinate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterCoordinate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitCoordinate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitCoordinate(this);
			else return visitor.visitChildren(this);
		}

	}

	public final CoordinateContext coordinate() throws RecognitionException {
		CoordinateContext _localctx = new CoordinateContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_coordinate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(LPAREN);
			setState(156);
			((CoordinateContext)_localctx).latitude = number();
			setState(157);
			match(COMMA);
			setState(158);
			((CoordinateContext)_localctx).longitude = number();
			setState(159);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumberContext extends ParserRuleContext {
		public TerminalNode FLOAT() { return getToken(FlightPlanParser.FLOAT, 0); }
		public TerminalNode INT() { return getToken(FlightPlanParser.INT, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlightPlanListener ) ((FlightPlanListener)listener).exitNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FlightPlanVisitor ) return ((FlightPlanVisitor<? extends T>)visitor).visitNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
			_la = _input.LA(1);
			if ( !(_la==FLOAT || _la==INT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001$\u00a4\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0004\u0001<\b\u0001\u000b\u0001"+
		"\f\u0001=\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0004\u0006W\b\u0006\u000b\u0006\f\u0006X\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011"+
		"\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014"+
		"\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0000\u0000\u0018\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014"+
		"\u0016\u0018\u001a\u001c\u001e \"$&(*,.\u0000\u0003\u0001\u0000\u0010"+
		"\u0011\u0001\u0000\u000e\u000f\u0001\u0000\"#\u008d\u00000\u0001\u0000"+
		"\u0000\u0000\u00023\u0001\u0000\u0000\u0000\u0004A\u0001\u0000\u0000\u0000"+
		"\u0006E\u0001\u0000\u0000\u0000\bG\u0001\u0000\u0000\u0000\nM\u0001\u0000"+
		"\u0000\u0000\fQ\u0001\u0000\u0000\u0000\u000e]\u0001\u0000\u0000\u0000"+
		"\u0010d\u0001\u0000\u0000\u0000\u0012k\u0001\u0000\u0000\u0000\u0014u"+
		"\u0001\u0000\u0000\u0000\u0016z\u0001\u0000\u0000\u0000\u0018\u007f\u0001"+
		"\u0000\u0000\u0000\u001a\u0084\u0001\u0000\u0000\u0000\u001c\u008b\u0001"+
		"\u0000\u0000\u0000\u001e\u008d\u0001\u0000\u0000\u0000 \u008f\u0001\u0000"+
		"\u0000\u0000\"\u0091\u0001\u0000\u0000\u0000$\u0093\u0001\u0000\u0000"+
		"\u0000&\u0095\u0001\u0000\u0000\u0000(\u0097\u0001\u0000\u0000\u0000*"+
		"\u0099\u0001\u0000\u0000\u0000,\u009b\u0001\u0000\u0000\u0000.\u00a1\u0001"+
		"\u0000\u0000\u000001\u0003\u0002\u0001\u000012\u0005\u0000\u0000\u0001"+
		"2\u0001\u0001\u0000\u0000\u000034\u0005\u0001\u0000\u000045\u0003 \u0010"+
		"\u000056\u0003\"\u0011\u000067\u0005\u0015\u0000\u000078\u0003\b\u0004"+
		"\u000089\u0003\u0004\u0002\u00009;\u0003\n\u0005\u0000:<\u0003\f\u0006"+
		"\u0000;:\u0001\u0000\u0000\u0000<=\u0001\u0000\u0000\u0000=;\u0001\u0000"+
		"\u0000\u0000=>\u0001\u0000\u0000\u0000>?\u0001\u0000\u0000\u0000?@\u0005"+
		"\u0016\u0000\u0000@\u0003\u0001\u0000\u0000\u0000AB\u0005\u0003\u0000"+
		"\u0000BC\u0003\u0006\u0003\u0000CD\u0005\u001a\u0000\u0000D\u0005\u0001"+
		"\u0000\u0000\u0000EF\u0005 \u0000\u0000F\u0007\u0001\u0000\u0000\u0000"+
		"GH\u0005\u0002\u0000\u0000HI\u0003$\u0012\u0000IJ\u0005\u001b\u0000\u0000"+
		"JK\u0003$\u0012\u0000KL\u0005\u001a\u0000\u0000L\t\u0001\u0000\u0000\u0000"+
		"MN\u0005\u0004\u0000\u0000NO\u0003&\u0013\u0000OP\u0005\u001a\u0000\u0000"+
		"P\u000b\u0001\u0000\u0000\u0000QR\u0005\u0005\u0000\u0000RS\u0005\u0015"+
		"\u0000\u0000ST\u0003\u000e\u0007\u0000TV\u0003\u0010\b\u0000UW\u0003\u0012"+
		"\t\u0000VU\u0001\u0000\u0000\u0000WX\u0001\u0000\u0000\u0000XV\u0001\u0000"+
		"\u0000\u0000XY\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000Z[\u0003"+
		"\u0014\n\u0000[\\\u0005\u0016\u0000\u0000\\\r\u0001\u0000\u0000\u0000"+
		"]^\u0005\u0006\u0000\u0000^_\u0003$\u0012\u0000_`\u0005\n\u0000\u0000"+
		"`a\u0003(\u0014\u0000ab\u0003*\u0015\u0000bc\u0005\u001a\u0000\u0000c"+
		"\u000f\u0001\u0000\u0000\u0000de\u0005\u0007\u0000\u0000ef\u0003$\u0012"+
		"\u0000fg\u0005\n\u0000\u0000gh\u0003(\u0014\u0000hi\u0003*\u0015\u0000"+
		"ij\u0005\u001a\u0000\u0000j\u0011\u0001\u0000\u0000\u0000kl\u0005\b\u0000"+
		"\u0000lm\u0005\u0015\u0000\u0000mn\u0003,\u0016\u0000no\u0005\u001b\u0000"+
		"\u0000op\u0003,\u0016\u0000pq\u0003\u0016\u000b\u0000qr\u0003\u0018\f"+
		"\u0000rs\u0003\u001a\r\u0000st\u0005\u0016\u0000\u0000t\u0013\u0001\u0000"+
		"\u0000\u0000uv\u0005\t\u0000\u0000vw\u0003.\u0017\u0000wx\u0003\u001e"+
		"\u000f\u0000xy\u0005\u001a\u0000\u0000y\u0015\u0001\u0000\u0000\u0000"+
		"z{\u0005\u000b\u0000\u0000{|\u0003.\u0017\u0000|}\u0003\u001c\u000e\u0000"+
		"}~\u0005\u001a\u0000\u0000~\u0017\u0001\u0000\u0000\u0000\u007f\u0080"+
		"\u0005\f\u0000\u0000\u0080\u0081\u0003.\u0017\u0000\u0081\u0082\u0003"+
		"\u001c\u000e\u0000\u0082\u0083\u0005\u001a\u0000\u0000\u0083\u0019\u0001"+
		"\u0000\u0000\u0000\u0084\u0085\u0005\r\u0000\u0000\u0085\u0086\u0003."+
		"\u0017\u0000\u0086\u0087\u0005\u0013\u0000\u0000\u0087\u0088\u0003.\u0017"+
		"\u0000\u0088\u0089\u0005\u0014\u0000\u0000\u0089\u008a\u0005\u001a\u0000"+
		"\u0000\u008a\u001b\u0001\u0000\u0000\u0000\u008b\u008c\u0005\u0012\u0000"+
		"\u0000\u008c\u001d\u0001\u0000\u0000\u0000\u008d\u008e\u0007\u0000\u0000"+
		"\u0000\u008e\u001f\u0001\u0000\u0000\u0000\u008f\u0090\u0005\u001e\u0000"+
		"\u0000\u0090!\u0001\u0000\u0000\u0000\u0091\u0092\u0007\u0001\u0000\u0000"+
		"\u0092#\u0001\u0000\u0000\u0000\u0093\u0094\u0005\u001f\u0000\u0000\u0094"+
		"%\u0001\u0000\u0000\u0000\u0095\u0096\u0005!\u0000\u0000\u0096\'\u0001"+
		"\u0000\u0000\u0000\u0097\u0098\u0005\u001c\u0000\u0000\u0098)\u0001\u0000"+
		"\u0000\u0000\u0099\u009a\u0005\u001d\u0000\u0000\u009a+\u0001\u0000\u0000"+
		"\u0000\u009b\u009c\u0005\u0017\u0000\u0000\u009c\u009d\u0003.\u0017\u0000"+
		"\u009d\u009e\u0005\u0019\u0000\u0000\u009e\u009f\u0003.\u0017\u0000\u009f"+
		"\u00a0\u0005\u0018\u0000\u0000\u00a0-\u0001\u0000\u0000\u0000\u00a1\u00a2"+
		"\u0007\u0002\u0000\u0000\u00a2/\u0001\u0000\u0000\u0000\u0002=X";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}