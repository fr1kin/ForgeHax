package dev.fiki.forgehax.common.asmtype;

import java.util.*;

import lombok.*;
import org.objectweb.asm.Type;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ASMMethod {
  private ASMClass parent;

  private String mcp;
  private String srg;

  @Getter(AccessLevel.PACKAGE)
  private ASMType returnType;
  @Getter(AccessLevel.PACKAGE)
  private ASMType[] arguments;

  public String getMcp() {
    return mcp == null ? srg : srg;
  }

  public String getName() {
    return getMcp();
  }

  public String getMcpDescriptor() {
    return Type.getMethodDescriptor(returnType.getMcp(),
        Arrays.stream(arguments)
            .map(ASMType::getMcp)
            .toArray(Type[]::new));
  }

  public String getSrgDescriptor() {
    return Type.getMethodDescriptor(returnType.getSrg(),
        Arrays.stream(arguments)
            .map(ASMType::getSrg)
            .toArray(Type[]::new));
  }

  public static class ASMMethodBuilder {
    private ASMType returnType;
    private List<ASMType> args = new ArrayList<>();

    public ASMMethodBuilder name(String name) {
      return srg(name).mcp(name);
    }

    private ASMMethodBuilder returnType(ASMType type) {
      return this; // exclude from builder
    }

    private ASMMethodBuilder arguments(ASMType[] types) {
      return this; // exclude from builder
    }

    public ASMMethodBuilder returns(ASMType type) {
      this.returnType = type;
      return this;
    }

    public ASMMethodBuilder returns(Type type) {
      return returns(new ASMType.ObjectWebType(type));
    }

    public ASMMethodBuilder returns(ASMClass clazz) {
      return returns(new ASMType.ASMClassType(clazz));
    }

    public ASMMethodBuilder returns(Class<?> clazz) {
      return returns(Type.getType(clazz));
    }

    public ASMMethodBuilder returns(String internalName) {
      return returns(Type.getObjectType(internalName.replace('.', '/')));
    }

    public ASMMethodBuilder returnsVoid() {
      return returns(Type.VOID_TYPE);
    }

    public ASMMethodBuilder argument(ASMType type) {
      this.args.add(type);
      return this;
    }

    public ASMMethodBuilder argument(Type type) {
      return argument(new ASMType.ObjectWebType(type));
    }

    public ASMMethodBuilder argument(ASMClass clazz) {
      return argument(new ASMType.ASMClassType(clazz));
    }

    public ASMMethodBuilder argument(Class<?> clazz) {
      return argument(Type.getType(clazz));
    }

    public ASMMethodBuilder argument(String internalName) {
      return argument(Type.getObjectType(internalName.replace('.', '/')));
    }

    public ASMMethodBuilder noArguments() {
      return this;
    }

    public ASMMethod build() {
      Objects.requireNonNull(srg, "Missing srg name");
      Objects.requireNonNull(returnType, "Missing method return type");
      return new ASMMethod(parent, mcp, srg, returnType, args.toArray(new ASMType[0]));
    }
  }
}
