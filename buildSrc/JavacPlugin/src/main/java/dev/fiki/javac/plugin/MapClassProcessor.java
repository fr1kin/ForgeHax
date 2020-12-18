package dev.fiki.javac.plugin;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.javac.plugin.map.ClassInfo;
import dev.fiki.javac.plugin.map.Mappings;
import dev.fiki.javac.plugin.type.TypeUtil;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.stream.Collectors;

import static com.sun.tools.javac.tree.JCTree.*;
import static lombok.javac.handlers.JavacHandlerUtil.genTypeRef;

//@MetaInfServices(JavacAnnotationHandler.class)
@HandlerPriority(65536)
public class MapClassProcessor extends JavacAnnotationHandler<MapClass> {
  public static final String RT_MAP_CLASS = "dev.fiki.forgehax.api.asm.runtime.RtMapClass";
  public static final String RT_MAP_CLASS_ALT = "dev.fiki.forgehax.api.asm.runtime.RtMapClass.Alternative";

  @Override
  public void handle(AnnotationValues<MapClass> values, JCAnnotation ast, JavacNode node) {
    JavacNode top = node.up();

    // extract the full class name from the annotation
    Element classElement = findClassElement(ast, node.getContext());

    if (classElement == null) {
      node.addError("MapClass requires that value(), classType(), or className() exist!");
      return;
    }

    if (!Util.insertAnnotation(top, RT_MAP_CLASS,
        createMapClassAnnotationArguments(top, TypeUtil.getInternalClassName(classElement)))) {
      node.addError(top.getKind() + " is not a supported type for MapClass");
    }
  }

  public static List<JCExpression> createMapClassAnnotationArguments(JavacNode node, String internalClassName) {
    final Mappings map = Mappings.getInstance();
    final JavacTreeMaker maker = node.getTreeMaker().at(node.get().pos);
    final Context ctx = node.getContext();
    final Names names = Names.instance(ctx);
    final JavacElements elements = JavacElements.instance(ctx);

    // the only required field, which specifies the className in internal format
    JCAssign memberClassName = maker.Assign(
        maker.Ident(names.fromString("className")),
        maker.Literal(internalClassName)
    );

    final ClassInfo classInfo = map.getClassMapping(internalClassName, Format.NORMAL);

    if (classInfo != null) {
      final JCExpression rtClassAnnotationType = Util.getAnnotationType(node, RT_MAP_CLASS_ALT);

      List<JCExpression> array = List.nil();
      for (ClassInfo.ClassEntry e : classInfo.getFormats()
          .filter(e -> !e.name.equals(internalClassName))
          .distinct()
          .collect(Collectors.toList())) {
        JCAssign altClassName = maker.Assign(
            maker.Ident(names.fromString("className")),
            maker.Literal(e.name)
        );

        JCAssign format = maker.Assign(
            maker.Ident(names.fromString("format")),
            maker.Select(genTypeRef(node, Util.FORMAT), names.fromString(e.format.name()))
        );

        JCAnnotation alternative = maker.Annotation(
            rtClassAnnotationType,
            List.of(altClassName, format)
        );

        array = array.append(alternative);
      }

      if (!array.isEmpty()) {
        // this is a lazy way of acquiring a full spec symbol for the alternatives() method
        Symbol.MethodSymbol symbol = Util.resolveEnclosedSymbol(elements,
            RT_MAP_CLASS, "alternatives", Symbol.MethodSymbol.class);

        JCIdent id = maker.Ident(symbol.getSimpleName());
        id.sym = symbol;
        id.type = symbol.type;

        // should NOT providing an element type, it will try to create a new array instance
        JCNewArray na = maker.NewArray(null, List.nil(), array);

        return List.of(memberClassName, maker.Assign(id, na));
      }
    }
    return List.of(memberClassName);
  }

  public static ClassSymbol getParentClassFromAnnotation(JCAnnotation ast, final Context ctx) {
    return Util.getKeyFromAnnotation(ast, "parentClass")
        .map(JCAssign::getExpression)
        .filter(JCFieldAccess.class::isInstance)
        .map(JCFieldAccess.class::cast)
        .map(o -> o.sym.owner)
        .filter(ClassSymbol.class::isInstance)
        .map(ClassSymbol.class::cast)
        .orElseGet(() -> Util.getKeyFromAnnotation(ast, "parent")
            .map(JCAssign::getExpression)
            .filter(JCAnnotation.class::isInstance)
            .map(JCAnnotation.class::cast)
            .map(an -> findClassElement(an, ctx))
            .orElse(null));
  }

  public static ClassSymbol findClassElement(JCAnnotation ast, final Context ctx) {
    final Elements elements = JavacElements.instance(ctx);

    // extract the full class name from the annotation
    ClassSymbol classElement = Util.getKeysFromAnnotation(ast, "value", "classType")
        .map(JCAssign::getExpression)
        .filter(JCFieldAccess.class::isInstance)
        .map(JCFieldAccess.class::cast)
        // symbol owner will be the generic type inside Class<?>
        .map(o -> o.sym.owner)
        .filter(ClassSymbol.class::isInstance)
        .map(ClassSymbol.class::cast)
        // flat structure class name
        .findAny()
        // if that fails, we get the alternative className value
        .orElseGet(() -> Util.getKeyFromAnnotation(ast, "className")
            .map(JCAssign::getExpression)
            .filter(JCLiteral.class::isInstance)
            .map(JCLiteral.class::cast)
            .map(JCLiteral::getValue)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(elements::getTypeElement)
            .filter(ClassSymbol.class::isInstance)
            .map(ClassSymbol.class::cast)
            .orElse(null));

    // we must be provided at least one of these
    if (classElement == null) {
      return null;
    }

    // resolve the inner class structure
    String innerClassName = Util.getKeyFromAnnotation(ast, "innerClassName")
        .map(JCAssign::getExpression)
        .filter(JCLiteral.class::isInstance)
        .map(JCLiteral.class::cast)
        .map(JCLiteral::getValue)
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(TypeUtil::toInternalClassName)
        .orElse(null);

    if (innerClassName != null && !innerClassName.isEmpty()) {
      classElement = resolveEnclosedClass(classElement, innerClassName);
    } else {
      String[] innerNames = Util.getKeyFromAnnotation(ast, "innerClassNames")
          .map(JCAssign::getExpression)
          .filter(JCNewArray.class::isInstance)
          .map(JCNewArray.class::cast)
          .map(o -> o.elems.stream()
              .filter(JCLiteral.class::isInstance)
              .map(JCLiteral.class::cast)
              .map(JCLiteral::getValue)
              .filter(String.class::isInstance)
              .map(String.class::cast)
              .toArray(String[]::new))
          .orElse(null);

      if (innerNames != null && innerNames.length > 0) {
        classElement = resolveEnclosedClass(classElement, innerNames);
      }
    }

    return classElement;
  }

  public static ClassSymbol getInheritedClassElement(JavacNode node) {
    final String originalSource = node.getFileName();

    while (node != null && originalSource.equals(node.getFileName())) {
      List<JCAnnotation> annotations = null;

      switch (node.getKind()) {
        case TYPE:
          annotations = ((JCClassDecl) node.get()).mods.annotations;
          break;
        case ARGUMENT:
        case FIELD:
          annotations = ((JCVariableDecl) node.get()).mods.annotations;
          break;
        case METHOD:
          annotations = ((JCMethodDecl) node.get()).mods.annotations;
          break;
      }

      if (annotations != null) {

        for (JCAnnotation an : annotations) {
          String typeClassName = Util.getAnnotationNameFromType(an.getAnnotationType());
          if (typeClassName != null) {
            if ("dev.fiki.forgehax.api.asm.MapClass".equals(typeClassName)) {
              ClassSymbol classSymbol = findClassElement(an, node.getContext());

              if (classSymbol != null) {
                return classSymbol;
              }
            }
          }
        }
      }

      node = node.up();
    }
    return null;
  }

  private static ClassSymbol resolveEnclosedClass(Element element, String... names) {
    l0:
    for (String name : names) {
      for (Element e : element.getEnclosedElements()) {
        if (e instanceof ClassSymbol && name.contentEquals(e.getSimpleName())) {
          element = e;
          continue l0;
        }
      }
      System.err.println("No inner classes found");
      return null;
    }
    return (ClassSymbol) element;
  }
}
