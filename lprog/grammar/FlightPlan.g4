grammar FlightPlan;

flightPlan
    : flight EOF
    ;

flight
    : FLIGHT flightID flightType LBRACE
     route
     aircraft
     pilot
     leg+
     RBRACE
    ;

aircraft
    : AIRCRAFT aircraftRegistration SEMI
    ;

aircraftRegistration
    : AIRCRAFT_REGISTRATION
    ;

route
    : ROUTE origin=airportCode ARROW destination=airportCode SEMI
    ;

pilot
    : PILOT pilotID SEMI
    ;

leg
    : LEG LBRACE
    departure
    arrival
    segment+
    fuel
    RBRACE
    ;


departure
    : DEPARTURE airportCode AT date time SEMI
    ;

arrival
    : ARRIVAL airportCode AT date time SEMI
    ;

segment
    : SEGMENT LBRACE
    start=coordinate ARROW end=coordinate
    altitude
    width
    wind
    RBRACE
    ;

fuel
    : FUEL quantity=number unit=fuelUnit SEMI
    ;

altitude
    : ALTITUDE quantity=number unit=measureUnit SEMI
    ;

width
    : WIDTH quantity=number unit=measureUnit SEMI
    ;

wind
    : WIND direction=number DEG speed=number MS SEMI
    ;

measureUnit
    : M
    ;

fuelUnit
    : KG
    | L
    ;

flightID
    : FLIGHT_ID
    ;

 flightType
    : REGULAR
    | CHARTER
    ;

 airportCode
    : AIRPORT_CODE
    ;

 pilotID
    : PILOT_ID
    ;

 date
    : DATE
    ;

 time
    : TIME
    ;

 coordinate
     : LPAREN latitude=number COMMA longitude=number RPAREN
     ;

 number
     : FLOAT
     | INT
     ;

FLIGHT: [fF][lL][iI][gG][hH][tT];
ROUTE: [rR][oO][uU][tT][eE];
AIRCRAFT: [aA][iI][rR][cC][rR][aA][fF][tT];
PILOT: [pP][iI][lL][oO][tT];
LEG: [lL][eE][gG];
DEPARTURE: [dD][eE][pP][aA][rR][tT][uU][rR][eE];
ARRIVAL: [aA][rR][rR][iI][vV][aA][lL];
SEGMENT: [sS][eE][gG][mM][eE][nN][tT];
FUEL: [fF][uU][eE][lL];

AT: [aA][tT];
ALTITUDE: [aA][lL][tT][iI][tT][uU][dD][eE];
WIDTH: [wW][iI][dD][tT][hH];
WIND: [wW][iI][nN][dD];

REGULAR: [rR][eE][gG][uU][lL][aA][rR];
CHARTER: [cC][hH][aA][rR][tT][eE][rR];

KG: [kK][gG];
L: [lL];
M: [mM];
DEG: [dD][eE][gG];
MS: [mM] '/' [sS];

LBRACE: '{';
RBRACE: '}';
LPAREN: '(';
RPAREN: ')';
COMMA: ',';
SEMI: ';';
ARROW: '->';

DATE: [0-9][0-9][0-9][0-9] '-' [0-9][0-9] '-' [0-9][0-9];
TIME: [0-9][0-9] ':' [0-9][0-9];

FLIGHT_ID: [A-Z][A-Z] [0-9][0-9][0-9] [0-9]? [A-Z]?;
AIRPORT_CODE: [A-Z][A-Z][A-Z] [A-Z]?;
AIRCRAFT_REGISTRATION: [A-Z][A-Z] '-' [A-Z0-9]+;
PILOT_ID: [A-Z] [0-9][0-9][0-9]+;

FLOAT: '-'? [0-9]+ '.' [0-9]+;
INT: '-'? [0-9]+;

WS: [ \t\r\n]+ -> skip;


