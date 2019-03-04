package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.events.RenderTabNameEvent;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.AsmPattern;
import com.matt.forgehax.asm.utils.InsnPattern;
import java.util.Objects;
import java.util.Set;

import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/** Created by Babbaj on 8/9/2017. thanks 086 :3 */
public class PlayerTabOverlayPatch  {

  @RegisterTransformer
  public static class RenderPlayerlist_renderIcon implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerTabOverlay_renderPlayerList);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
          .opcodes(
              ALOAD,
              ICONST_0,
              ALOAD,
              INVOKEINTERFACE,
              BIPUSH,
              INVOKESTATIC,
              INVOKEINTERFACE,
              ASTORE
          )
          .build().test(main);

      Objects.requireNonNull(node, "Find pattern failed for node");

      Objects.requireNonNull(node, "Find pattern failed for subListPost");

      LabelNode jump = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_doIncreaseTabListSize));
      insnList.add(new JumpInsnNode(IFNE, jump));

      main.instructions.insertBefore(node.getFirst(), insnList);
      main.instructions.insert(node.getLast(), jump);
      return main;
    }
  }

  @RegisterTransformer
  public static class RenderPlayerList_renderName implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerTabOverlay_renderPlayerList);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
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
      return main;
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
