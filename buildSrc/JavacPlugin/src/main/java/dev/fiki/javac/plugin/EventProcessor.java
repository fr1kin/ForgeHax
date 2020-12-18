package dev.fiki.javac.plugin;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import dev.fiki.forgehax.api.event.Cancelable;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Objects;

import static com.sun.tools.javac.tree.JCTree.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

//@MetaInfServices(JavacASTVisitor.class)
public class EventProcessor extends JavacASTAdapter {
  private static final String LISTENER_LIST_FIELD_NAME = "LISTENER_LIST";
  private static final String GET_LISTENER_LIST_METHOD_NAME = "getListenerList";
  private static final String LISTENER_LIST_METHOD_NAME = "listenerList";

  private static final String CANCELED_FIELD_NAME = "eventCanceled";
  private static final String SET_CANCELED_METHOD_NAME = "setCanceled";
  private static final String IS_CANCELED_METHOD_NAME = "isCanceled";

  @Override
  public void visitType(JavacNode node, JCClassDecl classDecl) {
    final JavacElements elements = JavacElements.instance(node.getContext());
    final Type eventType = elements.getTypeElement("dev.fiki.forgehax.api.event.Event").asType();

    if (extendsClass(eventType, classDecl)) {
      generateListenerList(node, classDecl);
      generateDefaultConstructor(node, classDecl);

      if (node.hasAnnotation(Cancelable.class)) {
        generateCancelableEvent(node, classDecl);
      }
    }
  }

  public static void generateListenerList(JavacNode node, JCClassDecl classDecl) {
    final JavacTreeMaker maker = node.getTreeMaker().at(node.getStartPos());

    JCExpression classRef = maker.Ident(classDecl.getSimpleName());
    JCExpression typeRef = genTypeRef(node, "dev.fiki.forgehax.api.event.ListenerList");

    if (fieldExists(LISTENER_LIST_FIELD_NAME, node) == MemberExistsResult.NOT_EXISTS) {
      JCExpression typeInit = maker.NewClass(
          null,
          List.nil(),
          typeRef,
          List.of(maker.Select(classRef, node.toName("class"))),
          null
      );

      JCVariableDecl fieldDecl = recursiveSetGeneratedBy(maker.VarDef(
          maker.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL),
          node.toName(LISTENER_LIST_FIELD_NAME),
          typeRef, typeInit
      ), classDecl, node.getContext());

      injectFieldAndMarkGenerated(node, fieldDecl);
    }

    if (methodExists(GET_LISTENER_LIST_METHOD_NAME, node, 0) == MemberExistsResult.NOT_EXISTS) {
      JCAnnotation overrideAn = maker.Annotation(genTypeRef(node, "java.lang.Override"), List.nil());

      JCBlock body = maker.Block(0, List.of(maker.Return(maker.Select(
          classRef,
          node.toName(LISTENER_LIST_FIELD_NAME)
      ))));

      JCMethodDecl methodDecl = recursiveSetGeneratedBy(maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC, List.of(overrideAn)),
          node.toName(GET_LISTENER_LIST_METHOD_NAME),
          typeRef, List.nil(), List.nil(), List.nil(), body, null
      ), classDecl, node.getContext());

      injectMethod(node, methodDecl);
    }

    if (methodExists(LISTENER_LIST_METHOD_NAME, node, 0) == MemberExistsResult.NOT_EXISTS) {
      JCBlock body = maker.Block(0, List.of(maker.Return(maker.Select(
          classRef,
          node.toName(LISTENER_LIST_FIELD_NAME)
      ))));

      JCMethodDecl methodDecl = recursiveSetGeneratedBy(maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC | Flags.STATIC, List.nil()),
          node.toName(LISTENER_LIST_METHOD_NAME),
          typeRef, List.nil(), List.nil(), List.nil(), body, null
      ), classDecl, node.getContext());

      injectMethod(node, methodDecl);
    }
  }

  public static void generateDefaultConstructor(JavacNode node, JCClassDecl classDecl) {
    if (hasDefaultConstructor(classDecl)) {
//      node.addWarning("Event class " + node.getName() + " already has a default constructor!");
      return;
    }

    final JavacTreeMaker maker = node.getTreeMaker().at(node.getStartPos());

    // call super default constructor (it should always be present)
    List<JCStatement> statements = List.of(maker.Exec(maker.Apply(
        List.nil(),
        maker.Ident(node.toName("super")),
        List.nil()
    )));

    // we need to initialize all the fields in our generated constructor
    // or the compiler will complain
    for (JCTree tree : classDecl.defs) {
      if (tree instanceof JCVariableDecl) {
        JCVariableDecl field = (JCVariableDecl) tree;
        // ignore static fields
        // only set fields without initializers
        if ((field.mods.flags & Flags.STATIC) == 0 && field.init == null) {
          JCLiteral literal = maker.Literal(JavacTreeMaker.TypeTag.typeTag("BOT"), null);

          if (field.vartype instanceof JCPrimitiveTypeTree) {
            TypeTag tag = ((JCPrimitiveTypeTree) field.vartype).typetag;
            switch (tag) {
              case INT:
              case BYTE:
              case LONG:
              case FLOAT:
              case SHORT:
              case DOUBLE:
                literal = maker.Literal(0);
                break;
              case CHAR:
                literal = maker.Literal('\0');
                break;
              case BOOLEAN:
                literal = maker.Literal(false);
                break;
            }
          }

          statements = statements.append(maker.Exec(maker.Assign(
              maker.Select(maker.Ident(node.toName("this")), field.name),
              literal
          )));
        }
      }
    }

    JCBlock body = maker.Block(0, statements);

    JCMethodDecl methodDecl = recursiveSetGeneratedBy(maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC, List.nil()),
        node.toName("<init>"),
        null,
        List.nil(), List.nil(), List.nil(), body, null
    ), classDecl, node.getContext());

    injectMethod(node, methodDecl);
  }

  private static boolean hasDefaultConstructor(JCClassDecl classDecl) {
    return classDecl.defs.stream()
        .filter(JCMethodDecl.class::isInstance)
        .map(JCMethodDecl.class::cast)
        .anyMatch(decl -> "<init>".contentEquals(decl.name) && decl.params.isEmpty());
  }

  public static void generateCancelableEvent(JavacNode node, JCClassDecl classDecl) {
    if (inheritedAnnotatedBy(node, classDecl, Cancelable.class)) {
      node.addWarning("Event class " + classDecl.getSimpleName() + " is already annotated with @Cancelable!");
      return;
    }

    final JavacTreeMaker maker = node.getTreeMaker().at(node.getStartPos());
    final Symtab symtab = node.getSymbolTable();

    // generate the field
    if (fieldExists(CANCELED_FIELD_NAME, node) == MemberExistsResult.NOT_EXISTS) {
      JCVariableDecl fieldDecl = recursiveSetGeneratedBy(maker.VarDef(
          maker.Modifiers(Flags.PRIVATE),
          node.toName(CANCELED_FIELD_NAME),
          maker.Type(symtab.booleanType),
          maker.Literal(false)
      ), classDecl, node.getContext());
      injectFieldAndMarkGenerated(node, fieldDecl);
    }

    // generate the setCanceled method
    if (methodExists(SET_CANCELED_METHOD_NAME, node, 1) == MemberExistsResult.NOT_EXISTS) {
      JCAnnotation overrideAn = maker.Annotation(genTypeRef(node, "java.lang.Override"), List.nil());
      Name arg0 = node.toName("arg0");

      JCBlock body = maker.Block(0, List.of(maker.Exec(maker.Assign(
          maker.Select(
              maker.Ident(node.toName("this")),
              node.toName(CANCELED_FIELD_NAME)
          ),
          maker.Ident(arg0)
      ))));

      JCMethodDecl methodDecl = recursiveSetGeneratedBy(maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC | Flags.FINAL, List.of(overrideAn)),
          node.toName(SET_CANCELED_METHOD_NAME),
          maker.Type(symtab.voidType),
          List.nil(),
          List.of(maker.VarDef(
              maker.Modifiers(Flags.PARAMETER | Flags.FINAL),
              arg0,
              maker.Type(symtab.booleanType),
              null
          )),
          List.nil(), body, null
      ), classDecl, node.getContext());

      injectMethod(node, methodDecl);
    }

    // generate isCanceled
    if (methodExists(IS_CANCELED_METHOD_NAME, node, 0) == MemberExistsResult.NOT_EXISTS) {
      JCAnnotation overrideAn = maker.Annotation(genTypeRef(node, "java.lang.Override"), List.nil());

      JCBlock body = maker.Block(0, List.of(maker.Return(maker.Select(
          maker.Ident(node.toName("this")), node.toName(CANCELED_FIELD_NAME)
      ))));

      JCMethodDecl methodDecl = recursiveSetGeneratedBy(maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC | Flags.FINAL, List.of(overrideAn)),
          node.toName(IS_CANCELED_METHOD_NAME),
          maker.Type(symtab.booleanType),
          List.nil(), List.nil(), List.nil(), body, null
      ), classDecl, node.getContext());

      injectMethod(node, methodDecl);
    }
  }

  private static boolean inheritedAnnotatedBy(JavacNode node, JCClassDecl classDecl,
      Class<? extends Annotation> annotationClass) {
    if (classDecl.extending != null) {
      final Elements elements = JavacElements.instance(node.getContext());
      final Element annotationElement = elements.getTypeElement(annotationClass.getCanonicalName());

      if (classDecl.extending.type instanceof Type.ClassType) {
        Type.ClassType type = (Type.ClassType) classDecl.extending.type;
        while (type != null) {
          if (type.tsym instanceof Symbol.ClassSymbol) {
            Symbol.ClassSymbol tsym = (Symbol.ClassSymbol) type.tsym;
            SymbolMetadata md = tsym.getMetadata();
            if (md != null && md.getDeclarationAttributes().stream()
                .map(c -> c.type)
                .filter(Objects::nonNull)
                .map(t -> t.tsym)
                .filter(Objects::nonNull)
                .anyMatch(annotationElement::equals)) {
              return true;
            }
          }

          // only care about class extensions
          type = getClassSuperType(type);
        }
      }
    }

    return false;
  }

  private static boolean extendsClass(final Type eventClass, JCClassDecl classDecl) {
    return classDecl.extending != null && extendsClass(eventClass, classDecl.extending.type);
  }

  private static boolean extendsClass(final Type eventClass, Type type) {
    if (type instanceof Type.ClassType) {
      Type.ClassType ct = (Type.ClassType) type;
      return eventClass.tsym.equals(ct.tsym) || extendsClass(eventClass, getClassSuperType(ct));
    }
    return false;
  }

  private static Type.ClassType getClassSuperType(Type type) {
    if (type.tsym != null && type.tsym.type instanceof Type.ClassType) {
      Type.ClassType ct = (Type.ClassType) type.tsym.type;
      if (ct.supertype_field instanceof Type.ClassType) {
        return (Type.ClassType) ct.supertype_field;
      }
    }
    return null;
  }
}
