//
// Energy readers and parsers toolkit
// Copyright (C) 2019-2023 Niels Basjes
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

parser grammar SunspecModelParser;

options { tokenVocab=SunspecModelLexer; }


anyString : BooleanTypeDefinition
          | PointTypeDefinition
          | PointAccessDefinition
          | CategoryDefinition
          | BlockTypeDefinition
          | LifecycleStatusDefinition
          | STRING
          | NUMBERSTRING
          ;

versionAttribute        : Version   EQUALS value=anyString;
numberIdAttribute       : Id        EQUALS value=NUMBERSTRING;
stringIdAttribute       : Id        EQUALS value=anyString;
nameAttribute           : Name      EQUALS value=anyString;
offsetAttribute         : Offset    EQUALS value=NUMBERSTRING;
statusAttribute         : Status    EQUALS value=LifecycleStatusDefinition;
lenAttribute            : Len       EQUALS value=NUMBERSTRING;
mandatoryAttribute      : Mandatory EQUALS value=BooleanTypeDefinition;
localeAttribute         : Locale    EQUALS value=anyString;
unitsAttribute          : Units     EQUALS value=anyString;
sfAttribute             : Sf        EQUALS value=anyString;

blockTypeAttribute      : Type      EQUALS value=BlockTypeDefinition;

pointTypeAttribute      : Type      EQUALS value=PointTypeDefinition;
pointAccessAttribute    : Access    EQUALS value=PointAccessDefinition;
pointCategoryAttribute  : Category  EQUALS value=CategoryDefinition;

sunSpecModels
    : OPENSunSpecModels versionAttribute CLOSE
        modelgroup
      CLOSESunSpecModels
    ;

modelgroup
    :   modelDefinition? stringsDefinition*
    ;

modelDefinition
    : OPENModel (numberIdAttribute|lenAttribute|nameAttribute|statusAttribute)* CLOSE
        blockDefinition blockDefinition?
      CLOSEModel
    ;

blockDefinition
    : OPENBlock (lenAttribute|blockTypeAttribute|nameAttribute)* CLOSE
        pointDefinition+
      CLOSEBlock
    ;

pointDefinition
    : OPENPoint  (stringIdAttribute|lenAttribute|offsetAttribute|pointTypeAttribute|sfAttribute|unitsAttribute|pointAccessAttribute|mandatoryAttribute|pointCategoryAttribute)* SLASH_CLOSE
    | OPENPoint  (stringIdAttribute|lenAttribute|offsetAttribute|pointTypeAttribute|sfAttribute|unitsAttribute|pointAccessAttribute|mandatoryAttribute|pointCategoryAttribute)* CLOSE
        symbolDefinition+
      CLOSEPoint
    ;

symbolDefinition
    : OPENSymbol stringIdAttribute CLOSE
        value=TEXT
      CLOSESymbol
    ;

stringsDefinition
    : OPENStrings (numberIdAttribute|localeAttribute)* CLOSE
        stringsModelDefinition? stringsPointDefinition*
      CLOSEStrings
    ;

stringsModelDefinition
    : OPENModel stringIdAttribute? CLOSE
        (label|description|notes)*
      CLOSEModel
    ;

stringsPointDefinition
    : OPENPoint stringIdAttribute CLOSE
        (label|description|notes|stringsSymbolDefinition)+
      CLOSEPoint
    ;

stringsSymbolDefinition
    : OPENSymbol stringIdAttribute CLOSE
        (label|description|notes)+
      CLOSESymbol
    ;

label
    :  OPENLabel       CLOSE         value=TEXT    CLOSELabel                #filledLabel
    | (OPENLabel       CLOSE                       CLOSELabel
      |EMPTYLabel                                   )   #emptyLabel
    ;

description
    :  OPENDescription CLOSE         value=TEXT    CLOSEDescription           #filledDescription
    | (OPENDescription CLOSE                       CLOSEDescription
      |EMPTYDescription  )   #emptyDescription
    ;

notes
    :  OPENNotes       CLOSE         value=TEXT    CLOSENotes             #filledNotes
    | (OPENNotes       CLOSE                       CLOSENotes
      |EMPTYNotes        )   #emptyNotes
    ;

