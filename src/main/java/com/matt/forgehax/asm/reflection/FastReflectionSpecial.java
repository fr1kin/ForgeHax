package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.helper.AsmStackLogger;
import com.matt.forgehax.asm.reflection.type.FastField;
import com.matt.forgehax.asm.reflection.type.FastTypeBuilder;
import journeymap.client.cartography.render.BaseRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflectionSpecial {
    interface Fields {
        FastField<Float> BaseRenderer_tweakBrightenDaylightDiff = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setNames("tweakBrightenDaylightDiff")
                .asField();
        FastField<Float> BaseRenderer_tweakMoonlightLevel = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setNames("tweakMoonlightLevel")
                .asField();
        FastField<Float> BaseRenderer_tweakBrightenLightsourceBlock = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setNames("tweakBrightenLightsourceBlock")
                .asField();
        FastField<Integer> BaseRenderer_tweakDarkenWaterColorMultiplier = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setNames("tweakDarkenWaterColorMultiplier")
                .asField();
        FastField<Float> BaseRenderer_tweakWaterColorBlend = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setNames("tweakWaterColorBlend")
                .asField();
        FastField<Float> BaseRenderer_tweakMinimumDarkenNightWater = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setNames("tweakMinimumDarkenNightWater")
                .asField();
    }
}
