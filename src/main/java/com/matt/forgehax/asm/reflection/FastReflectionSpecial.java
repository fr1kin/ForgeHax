package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.helper.AsmStackLogger;
import com.matt.forgehax.asm.reflection.type.FastField;
import journeymap.client.cartography.render.BaseRenderer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflectionSpecial {
    interface Fields {
        FastField<Float> BaseRenderer_tweakBrightenDaylightDiff = new FastField<Float>(BaseRenderer.class)
                .mcpName("tweakBrightenDaylightDiff")
                ;

        FastField<Float> BaseRenderer_tweakMoonlightLevel = new FastField<Float>(BaseRenderer.class)
                .mcpName("tweakMoonlightLevel")
                ;

        FastField<Float> BaseRenderer_tweakBrightenLightsourceBlock = new FastField<Float>(BaseRenderer.class)
                .mcpName("tweakBrightenLightsourceBlock")
                ;

        FastField<Integer> BaseRenderer_tweakDarkenWaterColorMultiplier = new FastField<Integer>(BaseRenderer.class)
                .mcpName("tweakDarkenWaterColorMultiplier")
                ;

        FastField<Float> BaseRenderer_tweakWaterColorBlend = new FastField<Float>(BaseRenderer.class)
                .mcpName("tweakWaterColorBlend")
                ;

        FastField<Float> BaseRenderer_tweakMinimumDarkenNightWater = new FastField<Float>(BaseRenderer.class)
                .mcpName("tweakMinimumDarkenNightWater")
                ;
    }
}
