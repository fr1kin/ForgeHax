package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastTypeBuilder;
import journeymap.client.cartography.render.BaseRenderer;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflectionSpecial {
    interface Fields {
        FastField<Float> BaseRenderer_tweakBrightenDaylightDiff = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setName("tweakBrightenDaylightDiff")
                .asField();
        FastField<Float> BaseRenderer_tweakMoonlightLevel = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setName("tweakMoonlightLevel")
                .asField();
        FastField<Float> BaseRenderer_tweakBrightenLightsourceBlock = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setName("tweakBrightenLightsourceBlock")
                .asField();
        FastField<Integer> BaseRenderer_tweakDarkenWaterColorMultiplier = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setName("tweakDarkenWaterColorMultiplier")
                .asField();
        FastField<Float> BaseRenderer_tweakWaterColorBlend = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setName("tweakWaterColorBlend")
                .asField();
        FastField<Float> BaseRenderer_tweakMinimumDarkenNightWater = FastTypeBuilder.create()
                .setInsideClass(BaseRenderer.class)
                .setName("tweakMinimumDarkenNightWater")
                .asField();
    }
}
