grammar Reconf;

name : primitive
       | structure;

structure: OPEN_BRACKET CLOSE_BRACKET
          |collection
          | map;

collection: '[' name (',' name)* ']';

map: '[' mapEntry (',' mapEntry)* ']';

mapEntry : primitive ':' name;

primitive : LITERAL;


LITERAL
   : '\'''\''
   | '\'' (ESC | ~ ['\\])+ '\''
   ;
fragment ESC
   : '\\' (['\\/bfnrt] | UNICODE)
   ;
fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;
fragment HEX
   : [0-9a-fA-F]
   ;

OPEN_BRACKET : '[';
CLOSE_BRACKET : ']';

WS : [ \t\r\n]+ -> skip ;