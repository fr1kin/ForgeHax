package dev.fiki.javac.plugin;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.javac.plugin.map.FieldInfo;
import dev.fiki.javac.plugin.map.Mappings;
import dev.fiki.javac.plugin.type.TypeUtil;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import java.util.stream.Collectors;

import static com.sun.tools.javac.tree.JCTree.*;
import static lombok.javac.handlers.JavacHandlerUtil.genTypeRef;

//@MetaInfServices(JavacAnnotationHandler.class)
@HandlerPriority(65536)
public class MapFieldProcessor extends JavacAnnotationHandler<MapField> {
  public static final String RT_MAP_FIELD = "dev.fiki.forgehax.api.asm.runtime.RtMapField";
  public static final String RT_MAP_FIELD_ALT = "dev.fiki.forgehax.api.asm.runtime.RtMapField.Alternative";

  @Override
  public void handle(AnnotationValues<MapField> values, JCAnnotation ast, JavacNode node) {
    final JavacNode up = node.up();
    final Context ctx = node.getContext();
    final boolean allowInvalid = values.getAsBoolean("allowInvalid");

    // try to resolve the parent method
    ClassSymbol parentSymbol = MapClassProcessor.getParentClassFromAnnotation(ast, ctx);

    if (parentSymbol == null) {
      // we need to try and get the top level class annotation
      parentSymbol = MapClassProcessor.getInheritedClassElement(node);

      // if its still null there is nothing to inherit
      if (parentSymbol == null) {
        node.addError("MapField is missing a parent class!");
        return;
      }
    }

    String parentClassName = TypeUtil.getInternalClassName(parentSymbol);
    String fieldName;
    String fieldDescriptor;

    if (!allowInvalid) {
      Symbol.VarSymbol symbol = lookupFieldSymbol(node, parentSymbol, getFieldName(ast), getFieldType(ast, ctx));

      // lookupFieldSymbol adds the error message to the compiler, so we can just quit here
      if (symbol == null) {
        return;
      }

      fieldName = TypeUtil.getFieldName(symbol);
      fieldDescriptor = TypeUtil.getInternalName(symbol.asType());
    } else {
      String fn = getFieldName(ast);
      Type ft = getFieldType(ast, ctx);

      if (fn == null) {
        node.addError("Field name must be specified if allowInvalid=true");
        return;
      }

      if (ft == null) {
        ft = Symtab.instance(ctx).objectType;
        node.addWarning("Using allowInvalid=true for field " + fn + ", but did not specify a type. Defaulting to Ljava/lang/Object;...");
      }

      fieldName = fn;
      fieldDescriptor = TypeUtil.getInternalName(ft);
    }

    final Mappings map = Mappings.getInstance();
    final JavacTreeMaker maker = up.getTreeMaker().at(ast.pos);
    final Names names = Names.instance(up.getContext());

    List<JCExpression> args = List.of(
        maker.Assign(
            maker.Ident(names.fromString("parent")),
            maker.Annotation(
                Util.getAnnotationType(up, MapClassProcessor.RT_MAP_CLASS),
                MapClassProcessor.createMapClassAnnotationArguments(up, parentClassName)
            )
        ),
        maker.Assign(
            maker.Ident(names.fromString("name")),
            maker.Literal(fieldName)
        ),
        maker.Assign(
            maker.Ident(names.fromString("typeDescriptor")),
            maker.Literal(fieldDescriptor)
        )
    );

    FieldInfo fieldInfo = map.getFieldMapping(parentClassName, fieldName, Format.NORMAL);
    if (fieldInfo != null) {
      final JCExpression rtMethodAnnotationType = Util.getAnnotationType(node, RT_MAP_FIELD_ALT);

      List<JCExpression> alts = List.nil();
      // get every unique field format, exclude the normal format
      for (FieldInfo.FieldEntry e : fieldInfo.getFormats()
          .filter(fe -> !fieldName.equals(fe.name))
          .distinct()
          .collect(Collectors.toList())) {
        JCAssign name = maker.Assign(
            maker.Ident(names.fromString("name")),
            maker.Literal(e.name)
        );

        JCAssign format = maker.Assign(
            maker.Ident(names.fromString("format")),
            maker.Select(genTypeRef(node, Util.FORMAT), names.fromString(e.format.name()))
        );

        alts = alts.append(maker.Annotation(rtMethodAnnotationType, List.of(name, format)));
      }

      // if the array isn't empty, we have alternative names
      if (!alts.isEmpty()) {
        // this is a lazy way of acquiring a full spec symbol for the alternatives() method
        Symbol.MethodSymbol alternativesSymbol = Util.resolveEnclosedSymbol(JavacElements.instance(node.getContext()),
            RT_MAP_FIELD, "alternatives", Symbol.MethodSymbol.class);

        JCIdent id = maker.Ident(alternativesSymbol.getSimpleName());
        id.sym = alternativesSymbol;
        id.type = alternativesSymbol.type;

        // should NOT providing an element type, it will try to create a new array instance
        JCNewArray na = maker.NewArray(null, List.nil(), alts);

        // append to the end of the list
        args = args.append(maker.Assign(id, na));
      }
    }

    if (!Util.insertAnnotation(up, RT_MAP_FIELD, args)) {
      node.addError("Unable to insert annotation here");
    }
  }

  private static Symbol.VarSymbol lookupFieldSymbol(JavacNode node, ClassSymbol classSymbol,
      final String fieldName, final Type fieldType) {
    java.util.List<Symbol.VarSymbol> symbols = classSymbol.getEnclosedElements().stream()
        .filter(Symbol.VarSymbol.class::isInstance)
        .map(Symbol.VarSymbol.class::cast)
        .filter(sym -> fieldName == null || fieldName.contentEquals(sym.getSimpleName()))
        .filter(sym -> fieldType == null || fieldType.equals(sym.type))
        .collect(Collectors.toList());

    if (symbols.isEmpty()) {
      node.addError("Could not find any field from the given parameters.");
      return null;
    } else if (symbols.size() > 1) {
      node.addError("Must be more verbose about the target field. Found " +
          symbols.stream()
              .map(Symbol::toString)
              .collect(Collectors.joining(", ")));
      return null;
    } else {
      return symbols.get(0);
    }
  }

  public static String getFieldName(JCAnnotation ast) {
    return Util.getKeysFromAnnotation(ast, "value", "name")
        .map(JCAssign::getExpression)
        .filter(JCLiteral.class::isInstance)
        .map(JCLiteral.class::cast)
        .map(JCLiteral::getValue)
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .findAny()
        .orElse(null);
  }

  public static Type getFieldType(JCAnnotation ast, Context ctx) {
    return Util.getKeyFromAnnotation(ast, "typeClass")
        .map(JCAssign::getExpression)
        .filter(JCFieldAccess.class::isInstance)
        .map(JCFieldAccess.class::cast)
        .map(o -> o.sym.owner.type)
        .orElseGet(() -> Util.getKeyFromAnnotation(ast, "type")
            .map(JCAssign::getExpression)
            .filter(JCAnnotation.class::isInstance)
            .map(JCAnnotation.class::cast)
            .map(an -> MapClassProcessor.findClassElement(an, ctx))
            .map(Symbol::asType)
            .orElse(null));
  }
}
