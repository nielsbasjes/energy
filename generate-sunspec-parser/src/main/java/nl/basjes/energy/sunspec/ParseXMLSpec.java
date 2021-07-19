/*
 * Energy readers and parsers toolkit
 * Copyright (C) 2019-2021 Niels Basjes
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package nl.basjes.energy.sunspec;

import nl.basjes.energy.sunspec.types.Block;
import nl.basjes.energy.sunspec.types.BlockType;
import nl.basjes.energy.sunspec.types.Model;
import nl.basjes.energy.sunspec.types.Point;
import nl.basjes.energy.sunspec.types.PointAccess;
import nl.basjes.energy.sunspec.types.PointType;
import nl.basjes.energy.sunspec.types.SunSpec;
import nl.basjes.energy.sunspec.types.SunSpecType;
import nl.basjes.energy.sunspec.types.Symbol;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.basjes.energy.sunspec.SunspecModelParser.BlockDefinitionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.BlockTypeAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.EmptyDescriptionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.EmptyLabelContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.EmptyNotesContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.FilledDescriptionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.FilledLabelContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.FilledNotesContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.LenAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.LocaleAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.MandatoryAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.ModelDefinitionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.NameAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.NumberIdAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.OffsetAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.PointAccessAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.PointCategoryAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.PointDefinitionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.PointTypeAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.SfAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.StatusAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.StringIdAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.StringsDefinitionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.StringsPointDefinitionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.StringsSymbolDefinitionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.SunSpecModelsContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.SymbolDefinitionContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.UnitsAttributeContext;
import static nl.basjes.energy.sunspec.SunspecModelParser.VersionAttributeContext;

public class ParseXMLSpec extends SunspecModelParserBaseVisitor<Void, SunSpecType> {

    private static final Logger LOG = LoggerFactory.getLogger(ParseXMLSpec.class);

    public static SunSpec parse(SunSpec tree, String content) {
        CodePointCharStream             input  = CharStreams.fromString(content);
        Lexer                           lexer  = new SunspecModelLexer(input);
        CommonTokenStream               tokens = new CommonTokenStream(lexer);
        SunspecModelParser<SunSpecType> parser = new SunspecModelParser<>(tokens);

        SunSpecModelsContext<SunSpecType> parseResult = parser.sunSpecModels();
        new ParseXMLSpec().visit(parseResult, tree);
        return tree;
    }

    private static final String ATTR_Version           ="Version";
    private static final String ATTR_StringId          ="StringId";
    private static final String ATTR_NumberId          ="NumberId";
    private static final String ATTR_Name              ="Name";
    private static final String ATTR_Offset            ="Offset";
    private static final String ATTR_Status            ="Status";
    private static final String ATTR_BlockType         ="BlockType";
    private static final String ATTR_PointType         ="PointType";
    private static final String ATTR_PointAccess       ="PointAccess";
    private static final String ATTR_Len               ="Len";
    private static final String ATTR_Mandatory         ="Mandatory";
    private static final String ATTR_Locale            ="Locale";
    private static final String ATTR_Units             ="Units";
    private static final String ATTR_Sf                ="Sf";
    private static final String ATTR_PointCategory     ="PointCategory";

    private void setAttribute(SunSpecType sunSpecTree, String attribute, String text) {
        String strippedText = text;

        if (text.startsWith("\"")) {
            strippedText = text.substring(1, text.length()-1);
        }
        if (sunSpecTree instanceof SunSpec) { setAttribute((SunSpec)sunSpecTree, attribute, strippedText); return; }
        if (sunSpecTree instanceof Model  ) { setAttribute((Model)sunSpecTree,   attribute, strippedText); return; }
        if (sunSpecTree instanceof Block  ) { setAttribute((Block)sunSpecTree,   attribute, strippedText); return; }
        if (sunSpecTree instanceof Point  ) { setAttribute((Point)sunSpecTree,   attribute, strippedText); return; }
        if (sunSpecTree instanceof Symbol ) { setAttribute((Symbol)sunSpecTree,  attribute, strippedText); return; }
        throw new IllegalStateException("We should never get called on SunSpecType : attribute " + attribute + " with value \"" + text + "\" for a " + sunSpecTree.getClass().getSimpleName() + " which is not allowed.");
    }

    private void setAttribute(SunSpec sunSpecTree, String attribute, String text) {
        if (ATTR_Version.equals(attribute)) {
            sunSpecTree.version = text;
        } else {
            throw new IllegalStateException("Received attribute " + attribute + " with value \"" + text + "\" for a " + sunSpecTree.getClass().getSimpleName() + " which is not allowed.");
        }
    }

    private void setAttribute(Model sunSpecTree, String attribute, String text) {
        switch(attribute) {
            case ATTR_NumberId:      sunSpecTree.id = Integer.parseInt(text); break;
            case ATTR_Name:          sunSpecTree.name = text; break;
            case ATTR_Status:        sunSpecTree.status = nl.basjes.energy.sunspec.types.Status.of(text); break;
            case ATTR_Len:           sunSpecTree.len = Integer.parseInt(text); break;

            case ATTR_Label:         sunSpecTree.label = text; break;
            case ATTR_Description:   sunSpecTree.description = text; break;
            case ATTR_Notes:         sunSpecTree.notes = text; break;

            default: throw new IllegalStateException("Received attribute " + attribute + " with value \"" + text + "\" for a " + sunSpecTree.getClass().getSimpleName() + " which is not allowed.");
        }
    }

    private void setAttribute(Block sunSpecTree, String attribute, String text) {
        switch(attribute) {
            case ATTR_Name:          sunSpecTree.name = text; break;
            case ATTR_BlockType:     sunSpecTree.type = BlockType.of(text); break;
            case ATTR_Len:           sunSpecTree.len = Integer.parseInt(text); break;

            default: throw new IllegalStateException("Received attribute " + attribute + " with value \"" + text + "\" for a " + sunSpecTree.getClass().getSimpleName() + " which is not allowed.");
        }
    }

    private void setAttribute(Point sunSpecTree, String attribute, String text) {
        switch(attribute) {
            case ATTR_StringId:      sunSpecTree.id = text; break;
            case ATTR_Offset:        sunSpecTree.offset = Integer.parseInt(text); break;
            case ATTR_PointType:     sunSpecTree.type = PointType.of(text); break;
            case ATTR_PointAccess:   sunSpecTree.access = PointAccess.of(text); break;
            case ATTR_Len:           sunSpecTree.len = Integer.parseInt(text); break;
            case ATTR_Mandatory:     sunSpecTree.mandatory = Boolean.valueOf(text); break;
            case ATTR_Units:         sunSpecTree.units = text; break;
            case ATTR_Sf:            sunSpecTree.sf = text; break;
            case ATTR_PointCategory: sunSpecTree.category = nl.basjes.energy.sunspec.types.Category.of(text); break;

            case ATTR_Label:         sunSpecTree.label = text; break;
            case ATTR_Description:   sunSpecTree.description = text; break;
            case ATTR_Notes:         sunSpecTree.notes = text; break;
            default: throw new IllegalStateException("Received attribute " + attribute + " with value \"" + text + "\" for a " + sunSpecTree.getClass().getSimpleName() + " which is not allowed.");
        }
    }

    private void setAttribute(Symbol sunSpecTree, String attribute, String text) {
        switch(attribute) {
            case ATTR_StringId:      sunSpecTree.id = text; break;

            case ATTR_Label:         sunSpecTree.label = text; break;
            case ATTR_Description:   sunSpecTree.description = text; break;
            case ATTR_Notes:         sunSpecTree.notes = text; break;
            default: throw new IllegalStateException("Received attribute " + attribute + " with value \"" + text + "\" for a " + sunSpecTree.getClass().getSimpleName() + " which is not allowed.");
        }
    }

    @Override public Void visitVersionAttribute(        VersionAttributeContext        <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Version       , ctx.value.getText()); return null; }
    @Override public Void visitStringIdAttribute(       StringIdAttributeContext       <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_StringId      , ctx.value.getText()); return null; }
    @Override public Void visitNumberIdAttribute(       NumberIdAttributeContext       <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_NumberId      , ctx.value.getText()); return null; }
    @Override public Void visitNameAttribute(           NameAttributeContext           <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Name          , ctx.value.getText()); return null; }
    @Override public Void visitOffsetAttribute(         OffsetAttributeContext         <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Offset        , ctx.value.getText()); return null; }
    @Override public Void visitStatusAttribute(         StatusAttributeContext         <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Status        , ctx.value.getText()); return null; }
    @Override public Void visitBlockTypeAttribute(      BlockTypeAttributeContext      <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_BlockType     , ctx.value.getText()); return null; }
    @Override public Void visitPointTypeAttribute(      PointTypeAttributeContext      <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_PointType     , ctx.value.getText()); return null; }
    @Override public Void visitPointAccessAttribute(    PointAccessAttributeContext    <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_PointAccess   , ctx.value.getText()); return null; }
    @Override public Void visitLenAttribute(            LenAttributeContext            <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Len           , ctx.value.getText()); return null; }
    @Override public Void visitMandatoryAttribute(      MandatoryAttributeContext      <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Mandatory     , ctx.value.getText()); return null; }
    @Override public Void visitLocaleAttribute(         LocaleAttributeContext         <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Locale        , ctx.value.getText()); return null; }
    @Override public Void visitUnitsAttribute(          UnitsAttributeContext          <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Units         , ctx.value.getText()); return null; }
    @Override public Void visitSfAttribute(             SfAttributeContext             <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Sf            , ctx.value.getText()); return null; }
    @Override public Void visitPointCategoryAttribute(  PointCategoryAttributeContext  <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_PointCategory , ctx.value.getText()); return null; }

//    @Override public Void visitSunSpecModels(           SunSpecModelsContext           <SunSpecType> ctx, SunSpecType sunSpecTree) {
//        LOG.info("{}SunSpecModels              : {}", spaces, ctx.getText()); String os=spaces; spaces+="  ";
//        super.visitSunSpecModels(ctx, sunSpecTree);
//        spaces = os;
//        return null;
//    }

    @Override public Void visitModelDefinition(         ModelDefinitionContext         <SunSpecType> ctx, SunSpecType sunSpecTree) {
        SunSpec parent = ((SunSpec)sunSpecTree);
        Model model = new Model();
        parent.models.add(model);
        model.parent = parent;
        super.visitModelDefinition(ctx, model);
        parent.modelLookup.put(model.id, model);
        return null;
    }

    @Override public Void visitBlockDefinition(         BlockDefinitionContext         <SunSpecType> ctx, SunSpecType sunSpecTree) {
        Model parent = (Model) sunSpecTree;
        Block block = new Block();
        parent.blocks.add(block);
        block.parent = parent;
        return super.visitBlockDefinition(ctx, block);
    }

    @Override public Void visitPointDefinition(         PointDefinitionContext         <SunSpecType> ctx, SunSpecType sunSpecTree) {
        Block parent = (Block) sunSpecTree;
        Point point = new Point();
        parent.points.add(point);
        point.parent = parent;
        super.visitPointDefinition(ctx, point);
        parent.pointLookup.put(point.id, point);
        return null;
    }

    @Override public Void visitSymbolDefinition(        SymbolDefinitionContext        <SunSpecType> ctx, SunSpecType sunSpecTree) {
        Point parent = (Point) sunSpecTree;
        Symbol symbol = new Symbol();
        parent.symbols.add(symbol);
        symbol.parent = parent;
        super.visitSymbolDefinition(ctx, symbol);
        parent.symbolLookup.put(symbol.id, symbol);
        symbol.value = ctx.value.getText();
        return null;
    }

    @Override public Void visitStringsDefinition(       StringsDefinitionContext       <SunSpecType> ctx, SunSpecType sunSpecTree) {
        SunSpec sunSpec = (SunSpec) sunSpecTree;
        String modelId = ctx.numberIdAttribute().get(0).value.getText();
        Model model = sunSpec.modelLookup.get(Integer.parseInt(modelId.substring(1, modelId.length()-1)));
        if (model == null) {
            LOG.error("Unable to find model {}", modelId);
            return null;
        }

        visit(ctx.stringsModelDefinition(), model);

        for(Block block: model.blocks){
            ctx.stringsPointDefinition(). forEach(sp -> visit(sp, block));
        }
        return null;
    }

//    @Override public Void visitStringsModelDefinition(  StringsModelDefinitionContext  <SunSpecType> ctx, SunSpecType sunSpecTree) {
//        LOG.info("{}StringsModelDefinition     : {}", spaces, ctx.getText()); String os=spaces; spaces+="  ";
////        Model model = (Model) sunSpecTree;
//
//        super.visitStringsModelDefinition(ctx, sunSpecTree);
//
//        spaces = os;
//        return null;
//    }

    @Override public Void visitStringsPointDefinition(  StringsPointDefinitionContext  <SunSpecType> ctx, SunSpecType sunSpecTree) {
        String id       = ctx.stringIdAttribute().value.getText();
        id = id.substring(1, id.length()-1);

        Block block = (Block) sunSpecTree;

        Point point = block.pointLookup.get(id);

        if (point == null) {
//            LOG.error("Unable to find point {}", id);
            return null;
        }
        return super.visitStringsPointDefinition(ctx, point);
    }

    @Override public Void visitStringsSymbolDefinition( StringsSymbolDefinitionContext <SunSpecType> ctx, SunSpecType sunSpecTree) {
        Point point = (Point) sunSpecTree;

        String id       = ctx.stringIdAttribute().value.getText();
        id = id.substring(1, id.length()-1);

        Symbol symbol = point.symbolLookup.get(id);
        if (symbol == null) {
//            LOG.error("Unable to find symbol {}", id);
            return null;
        }
        return super.visitStringsSymbolDefinition(ctx, symbol);
    }

    private static final String ATTR_Label = "Label";
    private static final String ATTR_Description = "Description";
    private static final String ATTR_Notes = "Notes";

    @Override public Void visitFilledLabel(             FilledLabelContext             <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Label, ctx.value.getText()); return null; }
    @Override public Void visitEmptyLabel(              EmptyLabelContext              <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Label, ""); return null; }
    @Override public Void visitFilledDescription(       FilledDescriptionContext       <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Description, ctx.value.getText()); return null; }
    @Override public Void visitEmptyDescription(        EmptyDescriptionContext        <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Description, ""); return null; }
    @Override public Void visitFilledNotes(             FilledNotesContext             <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Notes, ctx.value.getText()); return null; }
    @Override public Void visitEmptyNotes(              EmptyNotesContext              <SunSpecType> ctx, SunSpecType sunSpecTree) { setAttribute(sunSpecTree, ATTR_Notes, ""); return null; }

}
