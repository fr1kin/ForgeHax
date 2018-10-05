package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.events.replacementhooks.GuiScreenEvent;
import com.matt.forgehax.asm.events.replacementhooks.RenderWorldLastEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.InsnPattern;
import com.matt.forgehax.mods.services.RenderEventService;
import com.matt.forgehax.util.mod.loader.ModManager;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static com.matt.forgehax.Globals.MC;
import static org.objectweb.asm.Opcodes.*;

@Transformer(EntityRenderer.class)
public class EntityRendererPatch {

    @Inject(name = "hurtCameraEffect", args = {float.class},
    description = "Add hook that allows the method to be canceled"
    )
    public void hurtCameraEffect(MethodNode main) {
        AbstractInsnNode preNode = main.instructions.getFirst();
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                RETURN
        }, "x");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(FLOAD, 1));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onHurtcamEffect));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insertBefore(postNode, endJump);
    }

    @Inject(name = "renderWorldPass", args = {int.class, float.class, long.class})
    public void renderWorldHook(AsmMethod method) {
      // TODO: do this better
      final InsnPattern pattern = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
          SIPUSH, INVOKESTATIC, // clear(256)
          0x00, 0x00,
          ALOAD, FLOAD, ILOAD, INVOKESPECIAL // renderHand
      }, "xx??xxxx");

      Objects.requireNonNull(pattern);
      final int renderglobalIndex = method.method.localVariables.stream()
          .filter(node -> node.desc.equals("Lbuy;") || node.desc.equals("Lnet/minecraft/client/renderer/RenderGlobal;"))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Failed to find local var renderglobal"))
          .index;

      method.setCursor(pattern.getIndex(4)); // aload
      method.visitInsn(new VarInsnNode(ALOAD, renderglobalIndex)); // renderglobal
      method.visitInsn(new VarInsnNode(FLOAD, 2)); // partialTicks
      method.<RenderWorldConsumer>invoke((renderglobal, partialTicks) -> {
        MC.mcProfiler.endStartSection("forgehax_render");
        ForgeHax.EVENT_BUS.post(new RenderWorldLastEvent(renderglobal, partialTicks));
        //ModManager.getInstance().get(RenderEventService.class).ifPresent(mod -> mod.onRenderWorld(new RenderWorldLastEvent(renderglobal, partialTicks)));
      });
    }

    private interface RenderWorldConsumer {
      void accept(RenderGlobal renderglobal, float partialTicks);
    }

    @Inject(name = "updateCameraAndRender", args = {float.class, long.class},
    description = "Add hook for GuiScreenEvent.DrawScreenEvent")
    public void guiDrawScreenHook(AsmMethod method) {
        InsnPattern node = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
            ALOAD, GETFIELD, GETFIELD, ILOAD, ILOAD, ALOAD, GETFIELD, INVOKEVIRTUAL, INVOKEVIRTUAL
        }, "xxxxxxxx?"); // forge if last node is invokestatic, vanilla if invokevirtual
        final AbstractInsnNode post = node.getLast().getNext();
        Objects.requireNonNull(node, "Failed to find pattern for node");
        {
            method.setCursor(node.getFirst());
            method.visitInsn(new VarInsnNode(ILOAD, 8)); // k1 (mouseX)
            method.visitInsn(new VarInsnNode(ILOAD, 9)); // l1 (mouseY)
            method.<IntBiPredicate>invoke((mouseX, mouseY) ->
                    ForgeHax.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(
                            MC.currentScreen,
                            mouseX,
                            mouseY,
                            MC.getTickLength())
                    )
            );
            method.jumpIf(IFNE, node.getLast().getNext());
        }
        {
            method.setCursor(post);
            method.visitInsn(new VarInsnNode(ILOAD, 8)); // k1 (mouseX)
            method.visitInsn(new VarInsnNode(ILOAD, 9)); // l1 (mouseY)
            method.<IntBiConsumer>invoke((mouseX, mouseY) ->
                    ForgeHax.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(
                            MC.currentScreen,
                            mouseX,
                            mouseY,
                            MC.getTickLength()
                            )
                    )
            );
        }
    }

    private interface IntBiPredicate {
        boolean test(int a, int b);
    }

    private interface IntBiConsumer {
        void accept(int a, int b);
    }
}
