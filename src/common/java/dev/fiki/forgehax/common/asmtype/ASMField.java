package dev.fiki.forgehax.common.asmtype;

import lombok.*;
import org.objectweb.asm.Type;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ASMField {
  private ASMClass parent;

  private String mcp;
  private String srg;

  @Getter(AccessLevel.PACKAGE)
  private ASMType type;

  public String getMcp() {
    return mcp == null ? srg : srg;
  }

  public String getName() {
    return getMcp();
  }

  public Type getMcpType() {
    return type.getMcp();
  }

  public Type getSrgType() {
    return type.getSrg();
  }

  public static class ASMFieldBuilder {
    public ASMFieldBuilder name(String name) {
      return mcp(name).srg(name);
    }

    public ASMFieldBuilder type(ASMType type) {
      this.type = type;
      return this;
    }

    public ASMFieldBuilder type(Type type) {
      return type(new ASMType.ObjectWebType(type));
    }

    public ASMFieldBuilder type(ASMClass clazz) {
      return type(new ASMType.ASMClassType(clazz));
    }

    public ASMFieldBuilder type(Class<?> clazz) {
      return type(Type.getType(clazz));
    }

    public ASMFieldBuilder type(String internalName) {
      return type(Type.getObjectType(internalName.replace('.', '/')));
    }
  }
}
