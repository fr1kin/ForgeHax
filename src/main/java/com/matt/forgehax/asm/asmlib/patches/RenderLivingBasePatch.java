package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.RenderLivingEvent;
import com.matt.forgehax.asm.events.replacementhooks.RenderNametagEvent;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Transformer(RenderLivingBase.class)
public class RenderLivingBasePatch {

  @Inject(name = "doRender", args = {EntityLivingBase.class, double.class, double.class, double.class, float.class, float.class})
  public void doRenderHook(AsmMethod method) {
    pushDoRenderArgs(method);
    method.<DoRenderPredicate>invoke((entity, x, y, z, partialTicks) ->
        ForgeHax.EVENT_BUS.post(new RenderLivingEvent.Pre(entity, partialTicks, x, y, z))
    );
    method.returnIf(true);

    method.setCursor(method.getLastReturn());
    pushDoRenderArgs(method);
    method.<DoRenderConsumer>invoke((entity, x, y, z, partialTicks) ->
        ForgeHax.EVENT_BUS.post(new RenderLivingEvent.Post(entity, partialTicks, x, y, z))
    );
  }

  private void pushDoRenderArgs(AsmMethod method) {
    method.visitInsn(new VarInsnNode(ALOAD, 1)); // entity
    method.visitInsn(new VarInsnNode(DLOAD, 2)); // x
    method.visitInsn(new VarInsnNode(DLOAD, 4)); // y
    method.visitInsn(new VarInsnNode(DLOAD, 6)); // z
    method.visitInsn(new VarInsnNode(FLOAD, 8)); // partialTicks
  }

  private interface DoRenderPredicate {
    boolean test(EntityLivingBase entity, double x, double y, double z, float partialTicks);
  }

  private interface DoRenderConsumer {
    void accept(EntityLivingBase entity, double x, double y, double z, float partialTicks);
  }

  @Inject(name = "renderName", args = {EntityLivingBase.class, double.class, double.class, double.class})
  public void renderNametagHook(AsmMethod method) {
    method.visitInsn(new VarInsnNode(ALOAD, 1)); // entity
    method.visitInsn(new VarInsnNode(DLOAD, 2)); // x
    method.visitInsn(new VarInsnNode(DLOAD, 4)); // y
    method.visitInsn(new VarInsnNode(DLOAD, 6)); // z
    method.<RenderNametagPredicate>invoke((entity, x, y, z) ->
        ForgeHax.EVENT_BUS.post(new RenderNametagEvent(entity, x, y, z))
    );
    method.returnIf(true);
  }

  private interface RenderNametagPredicate {
    boolean test(EntityLivingBase entity, double x, double y, double z);
  }

}
