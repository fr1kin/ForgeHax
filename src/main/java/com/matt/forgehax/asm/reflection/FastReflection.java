package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.helper.AsmStackLogger;
import com.matt.forgehax.asm.reflection.type.FastField;
import com.matt.forgehax.asm.reflection.type.FastTypeBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflection extends ASMCommon {
    // ****************************************
    // FIELDS
    // ****************************************
    interface Fields {
        /**
         * ActiveRenderInfo
         */
        FastField<FloatBuffer> ActiveRenderInfo_MODELVIEW = FastTypeBuilder.create()
                .setInsideClass(ActiveRenderInfo.class)
                .setNames("MODELVIEW", "field_178812_b")
                .asField();
        FastField<FloatBuffer> ActiveRenderInfo_PROJECTION = FastTypeBuilder.create()
                .setInsideClass(ActiveRenderInfo.class)
                .setNames("PROJECTION", "field_178813_c")
                .asField();
        FastField<Vec3d> ActiveRenderInfo_position = FastTypeBuilder.create()
                .setInsideClass(ActiveRenderInfo.class)
                .setNames("position", "field_178811_e")
                .asField();

        /**
         * CPacketPlayer
         */
        FastField<Float> CPacketPlayer_pitch = FastTypeBuilder.create()
                .setInsideClass(CPacketPlayer.class)
                .setNames("pitch", "field_149473_f")
                .asField();
        FastField<Float> CPacketPlayer_yaw = FastTypeBuilder.create()
                .setInsideClass(CPacketPlayer.class)
                .setNames("yaw", "field_149476_e")
                .asField();
        FastField<Boolean> CPacketPlayer_rotating = FastTypeBuilder.create()
                .setInsideClass(CPacketPlayer.class)
                .setNames("rotating", "field_149481_i")
                .asField();
        FastField<Boolean> CPacketPlayer_onGround = FastTypeBuilder.create()
                .setInsideClass(CPacketPlayer.class)
                .setNames("onGround", "field_149474_g")
                .asField();

        /**
         * Entity
         */
        FastField<EntityDataManager> Entity_dataManager = FastTypeBuilder.create()
                .setInsideClass(Entity.class)
                .setNames("dataManager", "field_70180_af")
                .asField();

        /**
         * EntityPigZombie
         */
        FastField<Integer> EntityPigZombie_angerLevel = FastTypeBuilder.create()
                .setInsideClass(EntityPigZombie.class)
                .setNames("angerLevel", "field_70837_d")
                .asField();

        /**
         * EntityPlayer
         */
        FastField<Boolean> EntityPlayer_sleeping = FastTypeBuilder.create()
                .setInsideClass(EntityPlayer.class)
                .setNames("sleeping", "field_71083_bS")
                .asField();
        FastField<Boolean> EntityPlayer_sleepTimer = FastTypeBuilder.create()
                .setInsideClass(EntityPlayer.class)
                .setNames("sleepTimer", "field_71076_b")
                .asField();

        /**
         * EntityPlayerSP
         */
        FastField<Float> EntityPlayerSP_horseJumpPower = FastTypeBuilder.create()
                .setInsideClass(EntityPlayerSP.class)
                .setNames("horseJumpPower", "field_110321_bQ")
                .asField();

        /**
         * GuiDisconnected
         */
        FastField<GuiScreen> GuiDisconnected_parentScreen = FastTypeBuilder.create()
                .setInsideClass(GuiDisconnected.class)
                .setNames("parentScreen", "field_146307_h")
                .asField();
        FastField<ITextComponent> GuiDisconnected_message = FastTypeBuilder.create()
                .setInsideClass(GuiDisconnected.class)
                .setNames("message", "field_146304_f")
                .asField();
        FastField<String> GuiDisconnected_reason = FastTypeBuilder.create()
                .setInsideClass(GuiDisconnected.class)
                .setNames("reason", "field_146306_a")
                .asField();

        /**
         * Minecraft
         */
        FastField<Integer> Minecraft_leftClickCounter = FastTypeBuilder.create()
                .setInsideClass(Minecraft.class)
                .setNames("leftClickCounter", "field_71429_W")
                .asField();
        FastField<Integer> Minecraft_rightClickDelayTimer = FastTypeBuilder.create()
                .setInsideClass(Minecraft.class)
                .setNames("rightClickDelayTimer", "field_71467_ac")
                .asField();

        /**
         * PlayerControllerMP
         */
        FastField<Integer> PlayerControllerMP_blockHitDelay = FastTypeBuilder.create()
                .setInsideClass(PlayerControllerMP.class)
                .setNames("blockHitDelay", "field_78781_i")
                .asField();

        /**
         * SPacketEntityVelocity
         */
        FastField<Integer> SPacketEntityVelocity_motionX = FastTypeBuilder.create()
                .setInsideClass(SPacketEntityVelocity.class)
                .setNames("motionX", "field_149415_b")
                .asField();
        FastField<Integer> SPacketEntityVelocity_motionY = FastTypeBuilder.create()
                .setInsideClass(SPacketEntityVelocity.class)
                .setNames("motionY", "field_149416_c")
                .asField();
        FastField<Integer> SPacketEntityVelocity_motionZ = FastTypeBuilder.create()
                .setInsideClass(SPacketEntityVelocity.class)
                .setNames("motionZ", "field_149414_d")
                .asField();

        /**
         * SPacketExplosion
         */
        FastField<Float> SPacketExplosion_motionX = FastTypeBuilder.create()
                .setInsideClass(SPacketExplosion.class)
                .setNames("motionX", "field_149152_f")
                .asField();
        FastField<Float> SPacketExplosion_motionY = FastTypeBuilder.create()
                .setInsideClass(SPacketExplosion.class)
                .setNames("motionY", "field_149153_g")
                .asField();
        FastField<Float> SPacketExplosion_motionZ = FastTypeBuilder.create()
                .setInsideClass(SPacketExplosion.class)
                .setNames("motionZ", "field_149159_h")
                .asField();

        /**
         * VertexBuffer
         */
        FastField<Boolean> VertexBuffer_isDrawing = FastTypeBuilder.create()
                .setInsideClass(VertexBuffer.class)
                .setNames("isDrawing", "field_179010_r")
                .asField();
    }
}
