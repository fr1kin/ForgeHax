package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.helper.AsmStackLogger;
import journeymap.client.cartography.render.BaseRenderer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created on 5/8/2017 by fr1kin
 */
public class FastReflection implements ASMCommon {
    public static class ClassRenderChunk {
        private static Field FIELD_region;

        // will not work with optifine
        public static ChunkCache getRegion(RenderChunk renderChunk) {
            try {
                if(FIELD_region == null) FIELD_region = ReflectionHelper.findField(RenderChunk.class, "region", "field_189564_r", "r");
                return (ChunkCache)FIELD_region.get(renderChunk);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
            }
            return null;
        }
    }

    public static class ClassRenderGlobal {
        private static Field FIELD_viewFrustum;
        private static Field FIELD_renderDispatcher;

        public static ViewFrustum getViewFrustum(RenderGlobal renderGlobal) {
            try {
                if(FIELD_viewFrustum == null) FIELD_viewFrustum = ReflectionHelper.findField(RenderGlobal.class, "viewFrustum", "field_175008_n");
                return (ViewFrustum)FIELD_viewFrustum.get(renderGlobal);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
            }
            return null;
        }

        public static ChunkRenderDispatcher getRenderDispatcher(RenderGlobal renderGlobal) {
            try {
                if(FIELD_renderDispatcher == null) FIELD_renderDispatcher = ReflectionHelper.findField(RenderGlobal.class, "renderDispatcher", "field_174995_M");
                return (ChunkRenderDispatcher)FIELD_renderDispatcher.get(renderGlobal);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
            }
            return null;
        }
    }

    public static class ClassEntityPlayerSP {
        private static Field FIELD_horseJumpPower;

        public static void setHorseJumpPower(EntityPlayerSP entityPlayerSP, float power) {
            try {
                if(FIELD_horseJumpPower == null) FIELD_horseJumpPower = ReflectionHelper.findField(EntityPlayerSP.class, "horseJumpPower", "field_110321_bQ");
                FIELD_horseJumpPower.set(entityPlayerSP, power);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
            }
        }
    }
}
