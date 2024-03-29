grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
OPEN_ROUND_BRACKET: '(';
CLOSE_ROUND_BRACKET: ')';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';
NOT: '!';
//GREATER_THAN: '>';
//SMALLER_THAN: '<';
//EQUALS: '==';
//NOT_EQUALS: '!=';
//XOR: '^';
AND: '&&';
//OR: '||';




//--- PARSER: ---
stylesheet: variableAssignment* stylerule+;
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;
variableReference: CAPITAL_IDENT;
expression: (literal | variableReference) #litOrVariableRef
          | OPEN_ROUND_BRACKET expression CLOSE_ROUND_BRACKET #bracketedExpression
          | expression MUL expression #multiplyOperation
          | expression (PLUS|MIN) expression #addSubtOperation
          | NOT expression #notOperation
          | expression AND expression #andOperation;
literal: COLOR #colorLiteral | PIXELSIZE #pixelLiteral | PERCENTAGE #percentageLiteral | (TRUE|FALSE) #boolLiteral | SCALAR #scalarLiteral;
stylerule: (tagSelector|idSelector|classSelector) OPEN_BRACE body CLOSE_BRACE;
tagSelector: LOWER_IDENT;
idSelector: ID_IDENT;
classSelector: CLASS_IDENT;
body: (declaration|ifClause)+;
declaration: propertyName COLON (expression|variableReference) SEMICOLON;
propertyName: LOWER_IDENT;
ifClause: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE body CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE body CLOSE_BRACE;
