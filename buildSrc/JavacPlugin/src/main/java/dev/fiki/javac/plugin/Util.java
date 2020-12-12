package dev.fiki.javac.plugin;

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import lombok.core.AST;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import javax.lang.model.util.Elements;
import java.util.Optional;
import java.util.stream.Stream;

import static com.sun.tools.javac.tree.JCTree.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

class Util {
  public static final String FORMAT = "dev.fiki.forgehax.api.asm.runtime.Format";

  public static JavacNode getTopLevelType(JavacNode node) {
    while (node != null && node.getKind() != AST.Kind.TYPE) {
      node = node.up();
    }
    return node;
  }

  public static JCExpression getAnnotationType(JavacNode node, String annotationTypeFqn) {
    boolean isJavaLangBased;
    String simpleName;

    {
      int idx = annotationTypeFqn.lastIndexOf('.');
      simpleName = idx == -1 ? annotationTypeFqn : annotationTypeFqn.substring(idx + 1);
      isJavaLangBased = idx == 9 && annotationTypeFqn.regionMatches(0, "java.lang.", 0, 10);
    }

    return isJavaLangBased ? genJavaLangTypeRef(node, simpleName) : chainDotsString(node, annotationTypeFqn);
  }

  public static void addAnnotation(JCModifiers mods, JavacNode node, int pos,
      JCTree source, Context context, String annotationTypeFqn, List<JCExpression> args) {
    boolean isJavaLangBased;
    String simpleName;
    {
      int idx = annotationTypeFqn.lastIndexOf('.');
      simpleName = idx == -1 ? annotationTypeFqn : annotationTypeFqn.substring(idx + 1);

      isJavaLangBased = idx == 9 && annotationTypeFqn.regionMatches(0, "java.lang.", 0, 10);
    }

    for (JCAnnotation ann : mods.annotations) {
      JCTree annType = ann.getAnnotationType();
      if (annType instanceof JCIdent) {
        Name lastPart = ((JCIdent) annType).name;
        if (lastPart.contentEquals(simpleName)) return;
      }

      if (annType instanceof JCFieldAccess) {
        if (annType.toString().equals(annotationTypeFqn)) return;
      }
    }

    JavacTreeMaker maker = node.getTreeMaker();
    JCExpression annType = isJavaLangBased ? genJavaLangTypeRef(node, simpleName) : chainDotsString(node, annotationTypeFqn);
    annType.pos = pos;
    if (args != null) {
      for (JCExpression arg : args) {
        arg.pos = pos;
        if (arg instanceof JCAssign) {
          ((JCAssign) arg).lhs.pos = pos;
          ((JCAssign) arg).rhs.pos = pos;
        }
      }
    }
    List<JCExpression> argList = args != null ? args : List.nil();
    JCAnnotation annotation = recursiveSetGeneratedBy(maker.Annotation(annType, argList), source, context);
    annotation.pos = pos;
    mods.annotations = mods.annotations.append(annotation);
  }

  public static boolean insertAnnotation(JavacNode top, String annotationClassName, List<JCExpression> args) {
    switch (top.getKind()) {
      case TYPE: {
        JCClassDecl src = (JCClassDecl) top.get();
        addAnnotation(src.mods, top, src.pos, src, top.getContext(), annotationClassName, args);
        return true;
      }
      case ARGUMENT:
      case FIELD: {
        JCVariableDecl src = (JCVariableDecl) top.get();
        addAnnotation(src.mods, top, src.pos, src, top.getContext(), annotationClassName, args);
        return true;
      }
      case METHOD: {
        JCMethodDecl src = (JCMethodDecl) top.get();
        addAnnotation(src.mods, top, src.pos, src, top.getContext(), annotationClassName, args);
        return true;
      }
      default:
        return false;
    }
  }

  public static Stream<JCAssign> getKeysFromAnnotation(JCAnnotation ast, String... values) {
    return ast.getArguments().stream()
        .filter(JCAssign.class::isInstance)
        .map(JCAssign.class::cast)
        .filter(o -> o.getVariable() instanceof JCIdent)
        .filter(o -> Stream.of(values).anyMatch(n -> n.contentEquals(((JCIdent) o.getVariable()).getName())));
  }

  public static Optional<JCAssign> getKeyFromAnnotation(JCAnnotation ast, String value) {
    return getKeysFromAnnotation(ast, value).findAny();
  }

  public static String getAnnotationNameFromType(JCTree node) {
    if (node instanceof JCIdent) {
      return ((JCIdent) node).sym.flatName().toString();
    }
    return null;
  }

  public static Type getGenericType(Type type) {
    List<Type> generics = type.getTypeArguments();
    if (generics != null && !generics.isEmpty()) {
      return generics.get(0);
    }
    return null;
  }

  public static Stream<Type> getNewArrayTypes(JCNewArray array) {
    return array.elems.stream()
        .filter(JCFieldAccess.class::isInstance)
        .map(JCFieldAccess.class::cast)
        .map(JCFieldAccess::getExpression)
        .map(o -> o.type);
  }

  public static <T extends Symbol> T resolveEnclosedSymbol(Elements elements,
      String className, String target, Class<T> targetType) {
    return elements.getTypeElement(className).getEnclosedElements().stream()
        .filter(targetType::isInstance)
        .map(targetType::cast)
        .filter(sym -> target.contentEquals(sym.getSimpleName()))
        .findAny()
        .orElseThrow(() -> new Error("Could not find target" + target + " in " + className));
  }

  public static boolean typesEqual(Type[] a, List<Type> b) {
    if (a.length != b.size()) {
      return false;
    } else {
      for (int i = 0; i < a.length; i++) {
        if (!a[i].tsym.equals(b.get(i).tsym)) {
          return false;
        }
      }
      return true;
    }
  }

  public static boolean containsMethod(Symbol.ClassSymbol symbol,
      String methodName, Type returnType, Type... argumentTypes) {
    Scope members = symbol.members();
    for (Scope.Entry e = members.elems; e != null; e = e.sibling) {
      if (e.sym instanceof Symbol.MethodSymbol) {
        Symbol.MethodSymbol ms = (Symbol.MethodSymbol) e.sym;
        if (methodName.contentEquals(ms.getSimpleName())
            && (returnType == null || returnType.tsym.equals(ms.getReturnType().tsym))
            && (argumentTypes == null || argumentTypes.length < 1 || Util.typesEqual(argumentTypes, ms.type.getParameterTypes()))) {
          return true;
        }
      }
    }
    return false;
  }
}
