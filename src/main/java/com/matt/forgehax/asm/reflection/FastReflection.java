package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.helper.AsmStackLogger;
import com.matt.forgehax.asm.reflection.type.FastField;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflection extends ASMCommon {
    // ****************************************
    // FIELDS
    // ****************************************
    interface Fields {
        FastField<Float> EntityPlayerSP_horseJumpPower = new FastField<Float>(EntityPlayerSP.class)
                .mcpName("horseJumpPower")
                .srgName("field_110321_bQ")
                ;

        FastField<Boolean> VertexBuffer_isDrawing = new FastField<Boolean>(VertexBuffer.class)
                .mcpName("isDrawing")
                .srgName("field_179010_r")
                ;
    }
}
