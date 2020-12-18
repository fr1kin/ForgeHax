package dev.fiki.javac.plugin;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.javac.plugin.map.Mappings;
import dev.fiki.javac.plugin.map.MethodInfo;
import dev.fiki.javac.plugin.type.TypeRef;
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
public class MapMethodProcessor extends JavacAnnotationHandler<MapMethod> {
  public static final String RT_MAP_METHOD = "dev.fiki.forgehax.api.asm.runtime.RtMapMethod";
  public static final String RT_MAP_METHOD_ALT = "dev.fiki.forgehax.api.asm.runtime.RtMapMethod.Alternative";

  @Override
  public void handle(AnnotationValues<MapMethod> values, JCAnnotation ast, JavacNode node) {
    final JavacNode up = node.up();
    final Context ctx = node.getContext();
    final boolean allowInvalid = values.getAsBoolean("allowInvalid");

    // try to resolve the parent method
    Symbol.ClassSymbol classSymbol = MapClassProcessor.getParentClassFromAnnotation(ast, ctx);

    if (classSymbol == null) {
      // we need to try and get the top level class annotation
      classSymbol = MapClassProcessor.getInheritedClassElement(node);

      // if its still null there is nothing to inherit
      if (classSymbol == null) {
        up.addError("MapMethod has no parent class assigned or inherited!");
        return;
      }
    }

    String parentClassName = TypeUtil.getInternalClassName(classSymbol);
    String methodName;
    TypeRef methodDesc;

    if (!allowInvalid) {
      Symbol.MethodSymbol symbol = lookupMethodSymbol(node, classSymbol,
          getMethodName(ast), getArgumentTypes(ast), getReturnType(ast, ctx));

      if (symbol == null) {
        // no need to print an error message, it has already been done
        return;
      }

      methodName = TypeUtil.getMethodName(symbol);
      methodDesc = TypeUtil.getDescriptor(symbol);
    } else {
      Type[] _args = getArgumentTypes(ast);
      Type _ret = getReturnType(ast, ctx);

      if (_args == null || _ret == null) {
        node.addError("Must provide method arguments and return type if allowInvalid=true");
        return;
      }

      methodName = getMethodName(ast);
      methodDesc = TypeUtil.getDescriptor(_ret, _args);
    }

    final Mappings map = Mappings.getInstance();
    final JavacTreeMaker maker = up.getTreeMaker().at(ast.pos);
    final Names names = Names.instance(up.getContext());
    final JavacElements elements = JavacElements.instance(up.getContext());

    List<JCExpression> args = List.of(
        maker.Assign(
            maker.Ident(names.fromString("parent")),
            maker.Annotation(
                Util.getAnnotationType(up, MapClassProcessor.RT_MAP_CLASS),
                MapClassProcessor.createMapClassAnnotationArguments(up, parentClassName)
            )
        ),
        maker.Assign(maker.Ident(names.fromString("name")), maker.Literal(methodName)),
        maker.Assign(maker.Ident(names.fromString("descriptor")), maker.Literal(methodDesc.getDescriptor()))
    );

    MethodInfo methodInfo = map.getMethodMapping(parentClassName, methodName, methodDesc, Format.NORMAL);
    if (methodInfo != null) {
      final JCExpression rtMethodAnnotationType = Util.getAnnotationType(node, RT_MAP_METHOD_ALT);

      List<JCExpression> alts = List.nil();
      // get every unique method format, exclude the normal format
      for (MethodInfo.MethodEntry e : methodInfo.getFormats()
          .filter(me -> !methodName.equals(me.name) || !methodDesc.equals(me.descriptor))
          .distinct()
          .collect(Collectors.toList())) {
        JCAssign name = maker.Assign(
            maker.Ident(names.fromString("name")),
            maker.Literal(e.name)
        );

        JCAssign descriptor = maker.Assign(
            maker.Ident(names.fromString("descriptor")),
            maker.Literal(e.descriptor.getDescriptor())
        );

        JCAssign format = maker.Assign(
            maker.Ident(names.fromString("format")),
            maker.Select(genTypeRef(node, Util.FORMAT), names.fromString(e.format.name()))
        );

        alts = alts.append(maker.Annotation(rtMethodAnnotationType, List.of(name, descriptor, format)));
      }

      // if the array isn't empty, we have alternative names
      if (!alts.isEmpty()) {
        // this is a lazy way of acquiring a full spec symbol for the alternatives() method
        Symbol.MethodSymbol alternativesSymbol = Util.resolveEnclosedSymbol(elements,
            RT_MAP_METHOD, "alternatives", Symbol.MethodSymbol.class);

        JCIdent id = maker.Ident(alternativesSymbol.getSimpleName());
        id.sym = alternativesSymbol;
        id.type = alternativesSymbol.type;

        // should NOT providing an element type, it will try to create a new array instance
        JCNewArray na = maker.NewArray(null, List.nil(), alts);

        // append to the end of the list
        args = args.append(maker.Assign(id, na));
      }
    }

    if (!Util.insertAnnotation(up, RT_MAP_METHOD, args)) {
      up.addError("Failed to insert annotation");
    }
  }

  private static Symbol.MethodSymbol lookupMethodSymbol(JavacNode node, Symbol.ClassSymbol classSymbol, final String methodName,
      final Type[] argumentTypes, final Type returnType) {
    java.util.List<Symbol.MethodSymbol> symbols = classSymbol.getEnclosedElements().stream()
        .filter(Symbol.MethodSymbol.class::isInstance)
        .map(Symbol.MethodSymbol.class::cast)
        .filter(sym -> methodName == null || methodName.contentEquals(sym.getSimpleName()))
        .filter(sym -> argumentTypes == null || Util.typesEqual(argumentTypes, sym.type.getParameterTypes()))
        .filter(sym -> returnType == null || returnType.tsym.equals(sym.getReturnType().tsym))
        .collect(Collectors.toList());

    if (symbols.isEmpty()) {
      node.addError("Could not find any method from the given parameters.");
      return null;
    } else if (symbols.size() > 1) {
      node.addError("Must be more verbose about the target method. Found " +
          symbols.stream()
              .map(sym -> TypeUtil.getMethodName(sym) + TypeUtil.getDescriptor(sym))
              .collect(Collectors.joining(", ")));
      return null;
    } else {
      return symbols.get(0);
    }
  }

  public static String getMethodName(JCAnnotation ast) {
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

  public static Type[] getArgumentTypes(JCAnnotation ast) {
    return Util.getKeyFromAnnotation(ast, "argTypes")
        .map(JCAssign::getExpression)
        .filter(JCNewArray.class::isInstance)
        .map(JCNewArray.class::cast)
        .map(array -> Util.getNewArrayTypes(array).toArray(Type[]::new))
        .orElseGet(() -> Util.getKeyFromAnnotation(ast, "args")
            .map(JCAssign::getExpression)
            .filter(JCNewArray.class::isInstance)
            .map(JCNewArray.class::cast)
            .map(array -> Util.getNewArrayTypes(array).toArray(Type[]::new))
            .orElse(null));
  }

  public static Type getReturnType(JCAnnotation ast, Context ctx) {
    return Util.getKeyFromAnnotation(ast, "retType")
        .map(JCAssign::getExpression)
        .filter(JCFieldAccess.class::isInstance)
        .map(JCFieldAccess.class::cast)
        .map(o -> o.sym.owner.type)
        .orElseGet(() -> Util.getKeyFromAnnotation(ast, "ret")
            .map(JCAssign::getExpression)
            .filter(JCAnnotation.class::isInstance)
            .map(JCAnnotation.class::cast)
            .map(an -> MapClassProcessor.findClassElement(an, ctx))
            .map(Symbol::asType)
            .orElse(null));
  }
}
