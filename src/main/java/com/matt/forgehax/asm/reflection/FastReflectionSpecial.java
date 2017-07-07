package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.utils.fasttype.FastClass;
import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastMethod;
import com.matt.forgehax.asm.utils.fasttype.FastTypeBuilder;
import journeymap.client.cartography.render.BaseRenderer;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflectionSpecial {
    interface Classes {
        FastClass BaseRenderer = FastTypeBuilder.create()
                .setName("journeymap/client/cartography/render/BaseRenderer")
                .asClass();

        FastClass Stratum = FastTypeBuilder.create()
                .setName("journeymap/client/cartography/Stratum")
                .asClass();

        FastClass BlockMD = FastTypeBuilder.create()
                .setName("journeymap/client/model/BlockMD")
                .asClass();
    }

    interface Methods {
        FastMethod<Float[]> BaseRenderer_getAmbientColor = FastTypeBuilder.create()
                .setInsideClass(Classes.BaseRenderer)
                .setName("getAmbientColor")
                .setParameters()
                .asMethod();

        FastMethod<Boolean> Stratum_isUninitialized = FastTypeBuilder.create()
                .setInsideClass(Classes.Stratum)
                .setName("isUninitialized")
                .setParameters()
                .asMethod();

        FastMethod<Integer> Stratum_getLightLevel = FastTypeBuilder.create()
                .setInsideClass(Classes.Stratum)
                .setName("getLightLevel")
                .setParameters()
                .asMethod();

        FastMethod<Boolean> Stratum_isWater = FastTypeBuilder.create()
                .setInsideClass(Classes.Stratum)
                .setName("isWater")
                .setParameters()
                .asMethod();

        FastMethod<Object> Stratum_getChunkMd = FastTypeBuilder.create()
                .setInsideClass(Classes.Stratum)
                .setName("getChunkMd")
                .setParameters()
                .asMethod();
    }

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
