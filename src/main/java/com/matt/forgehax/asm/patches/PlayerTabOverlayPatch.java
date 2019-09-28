package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.events.RenderTabNameEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.AsmPattern;
import com.matt.forgehax.asm.utils.InsnPattern;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created by Babbaj on 8/9/2017. thanks 086 :3
 */
public class PlayerTabOverlayPatch extends ClassTransformer {
  
  public PlayerTabOverlayPatch() {
    super(Classes.GuiPlayerTabOverlay);
  }
  
  @RegisterMethodTransformer
  private class RenderPlayerlist_renderIcon extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return Methods.PlayerTabOverlay_renderPlayerList;
    }
    
    @Inject(description = "Add hook to increase the size of the tab list")
    public void inject(MethodNode main) {
      AbstractInsnNode subListNode =
        ASMHelper.findPattern(
          main.instructions.getFirst(),
          new int[]{
            ALOAD,
            ICONST_0,
            ALOAD,
            INVOKEINTERFACE,
            BIPUSH,
            INVOKESTATIC,
            INVOKEINTERFACE,
            ASTORE
          },
          "xxxxxxxx");
      
      AbstractInsnNode astoreNode = subListNode;
      for (int i = 0; i < 7; i++) {
        astoreNode = astoreNode.getNext();
      }
      
      Objects.requireNonNull(subListNode, "Find pattern failed for subList");
      Objects.requireNonNull(astoreNode, "Find pattern failed for subListPost");
      
      LabelNode jump = new LabelNode();
      
      InsnList insnList = new InsnList();
      insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_doIncreaseTabListSize));
      insnList.add(new JumpInsnNode(IFNE, jump));
      
      main.instructions.insertBefore(subListNode, insnList);
      main.instructions.insert(astoreNode, jump);
    }
  }
  
  @RegisterMethodTransformer
  private class RenderPlayerlist_renderName extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return Methods.PlayerTabOverlay_renderPlayerList;
    }
    
    @Inject(description = "Add hook to change color of names in player list")
    public void inject(MethodNode main) { // TODO: do this better
      final LabelNode eventVarStart =
        (LabelNode)
          new AsmPattern.Builder(AsmPattern.IGNORE_FRAMES | AsmPattern.IGNORE_LINENUMBERS)
            .custom(
              insn ->
                insn.getOpcode() == ALOAD
                  && ((VarInsnNode) insn).var == 24) // networkplayerinfo1
            .opcodes(
              INVOKEVIRTUAL,
              GETSTATIC,
              IF_ACMPNE) // if (networkplayerinfo1.getGameType() == GameType.SPECTATOR)
            .build()
            .test(main)
            .getLast()
            .getNext(); // start of the scope of the new local var
      
      final AsmPattern getFontRendererPattern =
        new AsmPattern.Builder(AsmPattern.CODE_ONLY)
          .custom(insn -> insn.getOpcode() == ALOAD && ((VarInsnNode) insn).var == 0) // this
          .opcodes(GETFIELD, GETFIELD)
          .build();
      
      final AsmPattern drawStringPattern =
        new AsmPattern.Builder(AsmPattern.CODE_ONLY)
          .opcodes(ILOAD, I2F, ILOAD, I2F)
          .any() // color
          .opcode(INVOKEVIRTUAL)
          .opcode(POP)
          .build();
      
      final int index_s4 = 26; // TODO: get this dynamically
      
      final InsnPattern renderSpectatorPre = getFontRendererPattern.test(eventVarStart);
      final InsnPattern renderSpectator = drawStringPattern.test(renderSpectatorPre.getLast());
      
      final InsnPattern renderNormalPre = getFontRendererPattern.test(renderSpectator.getLast());
      final InsnPattern renderNormal = drawStringPattern.test(renderSpectator.getLast());
      
      final LabelNode eventVarEnd =
        ((JumpInsnNode) renderSpectator.getLast().getNext()).label; // label from the goto
      
      final int eventVar =
        ASMHelper.addNewLocalVariable(
          main,
          "event",
          Type.getDescriptor(RenderTabNameEvent.class),
          eventVarStart,
          eventVarEnd);
      
      createAndFireEvent(main, renderSpectatorPre.getLast(), eventVar, index_s4);
      createAndFireEvent(main, renderNormalPre.getLast(), eventVar, index_s4);
      
      replaceConstant(main, renderSpectator.getIndex(4), eventVar);
      replaceConstant(main, renderNormal.getIndex(4), eventVar);
    }
    
    // replace constant for color with event.getColor()
    private void replaceConstant(MethodNode method, AbstractInsnNode node, int eventVar) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, eventVar));
      list.add(
        new MethodInsnNode(
          INVOKEVIRTUAL, Type.getInternalName(RenderTabNameEvent.class), "getColor", "()I"));
      
      method.instructions.insert(node, list); // insert at constant
      method.instructions.remove(node); // remove constant
    }
    
    // creates and fires the event, sets the variable for the event and sets name variable
    private void createAndFireEvent(
      MethodNode method, AbstractInsnNode location, int variableIndex, int nameIndex) {
      final InsnList list = new InsnList();
      
      // arguments
      InsnList eventObjectArgs = new InsnList();
      eventObjectArgs.add(new VarInsnNode(ALOAD, nameIndex));
      eventObjectArgs.add(new LdcInsnNode(-1)); // TODO: get original value
      
      list.add(
        ASMHelper.newInstance(
          Type.getInternalName(RenderTabNameEvent.class),
          "(Ljava/lang/String;I)V",
          eventObjectArgs));
      list.add(new InsnNode(DUP)); // for firing event
      list.add(new InsnNode(DUP)); // for getName
      list.add(new VarInsnNode(ASTORE, variableIndex));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_v));
      list.add(
        new MethodInsnNode(
          INVOKEVIRTUAL,
          Type.getInternalName(RenderTabNameEvent.class),
          "getName",
          "()Ljava/lang/String;"));
      list.add(new VarInsnNode(ASTORE, nameIndex));
      
      method.instructions.insert(location, list);
    }
  }
}
