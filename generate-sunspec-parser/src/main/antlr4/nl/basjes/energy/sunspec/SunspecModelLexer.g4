//
// Energy readers and parsers toolkit
// Copyright (C) 2019-2019 Niels Basjes
//
// This work is licensed under the Creative Commons
// Attribution-NonCommercial-NoDerivatives 4.0 International License.
//
// You may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    https://creativecommons.org/licenses/by-nc-nd/4.0/
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an AS IS BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

lexer grammar SunspecModelLexer;

fragment BLANKS: (' '|'\t'|'\r'? '\n')+ ;

COMMENT     :   BLANKS* '<!--' .*? '-->' BLANKS* -> skip;

SKIP_REMAINING_BLANKS :   BLANKS -> skip;

OPENSunSpecModels : '<sunSpecModels' -> pushMode(INSIDE);
OPENModel         : '<model'         -> pushMode(INSIDE);
OPENBlock         : '<block'         -> pushMode(INSIDE);
OPENPoint         : '<point'         -> pushMode(INSIDE);
OPENSymbol        : '<symbol'        -> pushMode(INSIDE);
OPENStrings       : '<strings'       -> pushMode(INSIDE);
OPENLabel         : '<label'         -> pushMode(INSIDE);
OPENDescription   : '<description'   -> pushMode(INSIDE);
OPENNotes         : '<notes'         -> pushMode(INSIDE);

EMPTYLabel         : '<label/>'         ;
EMPTYDescription   : '<description/>'   ;
EMPTYNotes         : '<notes/>'         ;

CLOSESunSpecModels : '</sunSpecModels>' ;
CLOSEModel         : '</model>'         ;
CLOSEBlock         : '</block>'         ;
CLOSEPoint         : '</point>'         ;
CLOSESymbol        : '</symbol>'        ;
CLOSEStrings       : '</strings>'       ;
CLOSELabel         : '</label>'         ;
CLOSEDescription   : '</description>'   ;
CLOSENotes         : '</notes>'         ;

TEXT        :   ~[<&]+ ;        // match any 16 bit char other than < and &

// ----------------- Everything INSIDE of a tag ---------------------
mode INSIDE;

CLOSE       :   '>'          -> popMode ;
SLASH_CLOSE :   '/>'    -> popMode ;
SLASH       :   '/' ;
EQUALS      :   '=' ;

BooleanTypeDefinition:
      '"true"' |
      '"false"' ;

PointTypeDefinition:
      '"int16"' |
      '"uint16"' |
      '"count"' |
      '"acc16"' |
      '"int32"' |
      '"uint32"' |
      '"float32"' |
      '"acc32"' |
      '"int64"' |
      '"uint64"' |
      '"float64"' |
      '"acc64"' |
      '"enum16"' |
      '"enum32"' |
      '"bitfield16"' |
      '"bitfield32"' |
      '"sunssf"' |
      '"string"' |
      '"pad"' |
      '"ipaddr"' |
      '"ipv6addr"' |
      '"eui48"' ;

PointAccessDefinition:
      '"r"' |
      '"rw"' ;

CategoryDefinition:
      '"none"' |
      '"measurement"' |
      '"metered"' |
      '"status"' |
      '"event"' |
      '"setting"' |
      '"control"' ;

BlockTypeDefinition:
      '"fixed"' |
      '"repeating"' ;

LifecycleStatusDefinition:
      '"draft"' |
      '"test"' |
      '"approved"' |
      '"superseded"' ;

NUMBERSTRING  :   '"' [0-9]+ '"'
              |   '\'' [0-9]+ '\''
              ;

STRING      :   '"' ~[<"]* '"'
            |   '\'' ~[<']* '\''
            ;


Version       : 'v'         ;
Id            : 'id'        ;
Name          : 'name'      ;
Len           : 'len'       ;
Offset        : 'offset'    ;
Type          : 'type'      ;
Sf            : 'sf'        ;
Status        : 'status'    ;
Mandatory     : 'mandatory' ;
Access        : 'access'    ;
Locale        : 'locale'    ;
Units         : 'units'     ;
Category      : 'category'  ;

S           :   [ \t\r\n]               -> skip ;

