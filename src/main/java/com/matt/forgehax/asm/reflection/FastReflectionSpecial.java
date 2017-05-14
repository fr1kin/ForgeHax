package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.helper.AsmStackLogger;
import journeymap.client.cartography.render.BaseRenderer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created on 5/8/2017 by fr1kin
 */
public class FastReflectionSpecial {
    public static class JMBaseRenderer {
        private static Field FIELD_tweakBrightenDaylightDiff = null;
        private static Field FIELD_tweakMoonlightLevel = null;
        private static Field FIELD_tweakBrightenLightsourceBlock = null;
        private static Field FIELD_tweakDarkenWaterColorMultiplier = null;
        private static Field FIELD_tweakWaterColorBlend = null;
        private static Field FIELD_tweakMinimumDarkenNightWater = null;

        public static float getTweakBrightenDaylightDiff(BaseRenderer baseRenderer) {
            try {
                if(FIELD_tweakBrightenDaylightDiff == null) FIELD_tweakBrightenDaylightDiff = ReflectionHelper.findField(BaseRenderer.class, "tweakBrightenDaylightDiff");
                return FIELD_tweakBrightenDaylightDiff.getFloat(baseRenderer);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
                return 0;
            }
        }

        public static float getTweakMoonlightLevel(BaseRenderer baseRenderer) {
            try {
                if(FIELD_tweakMoonlightLevel == null) FIELD_tweakMoonlightLevel = ReflectionHelper.findField(BaseRenderer.class, "tweakMoonlightLevel");
                return FIELD_tweakMoonlightLevel.getFloat(baseRenderer);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
                return 0;
            }
        }

        public static float getTweakBrightenLightsourceBlock(BaseRenderer baseRenderer) {
            try {
                if(FIELD_tweakBrightenLightsourceBlock == null) FIELD_tweakBrightenLightsourceBlock = ReflectionHelper.findField(BaseRenderer.class, "tweakBrightenLightsourceBlock");
                return FIELD_tweakBrightenLightsourceBlock.getFloat(baseRenderer);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
                return 0;
            }
        }

        public static int getTweakDarkenWaterColorMultiplier(BaseRenderer baseRenderer) {
            try {
                if(FIELD_tweakDarkenWaterColorMultiplier == null) FIELD_tweakDarkenWaterColorMultiplier = ReflectionHelper.findField(BaseRenderer.class, "tweakDarkenWaterColorMultiplier");
                return FIELD_tweakDarkenWaterColorMultiplier.getInt(baseRenderer);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
                return 0;
            }
        }

        public static float getTweakWaterColorBlend(BaseRenderer baseRenderer) {
            try {
                if(FIELD_tweakWaterColorBlend == null) FIELD_tweakWaterColorBlend = ReflectionHelper.findField(BaseRenderer.class, "tweakWaterColorBlend");
                return FIELD_tweakWaterColorBlend.getFloat(baseRenderer);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
                return 0;
            }
        }

        public static float getTweakMinimumDarkenNightWater(BaseRenderer baseRenderer) {
            try {
                if(FIELD_tweakMinimumDarkenNightWater == null) FIELD_tweakMinimumDarkenNightWater = ReflectionHelper.findField(BaseRenderer.class, "tweakMinimumDarkenNightWater");
                return FIELD_tweakMinimumDarkenNightWater.getFloat(baseRenderer);
            } catch (Exception e) {
                AsmStackLogger.printStackTrace(e);
                return 0;
            }
        }
    }
}
