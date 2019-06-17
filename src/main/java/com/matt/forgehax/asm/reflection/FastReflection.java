package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.reflection.util.fasttype.FastField;
import com.matt.forgehax.asm.reflection.util.fasttype.FastMethod;
import com.matt.forgehax.asm.reflection.util.fasttype.builder.FastFieldBuilder;
import com.matt.forgehax.asm.reflection.util.fasttype.builder.FastMethodBuilder;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.storage.ChunkLoader;

/** Created on 5/8/2017 by fr1kin */
public interface FastReflection extends ASMCommon {
  // ****************************************
  // FIELDS
  // ****************************************
  interface Fields {
    /** CPacketPlayer */
    FastField<Float> CPacketPlayer_pitch =
        FastFieldBuilder.create()
            .setInsideClass(CPlayerPacket.class)
            .setName("pitch")
            .setSrgName("field_149473_f")
            //.autoAssign()
            .build();

    FastField<Float> CPacketPlayer_yaw =
        FastFieldBuilder.create()
            .setInsideClass(CPlayerPacket.class)
            .setName("yaw")
            .setSrgName("field_149476_e")
            //.autoAssign()
            .build();
    FastField<Boolean> CPacketPlayer_rotating =
        FastFieldBuilder.create()
            .setInsideClass(CPlayerPacket.class)
            .setName("rotating")
            .setSrgName("field_149481_i")
            //.autoAssign()
            .build();
    FastField<Boolean> CPacketPlayer_onGround =
        FastFieldBuilder.create()
            .setInsideClass(CPlayerPacket.class)
            .setName("onGround")
            .setSrgName("field_149474_g")
            //.autoAssign()
            .build();
    FastField<Double> CPacketPlayer_Y =
        FastFieldBuilder.create()
            .setInsideClass(CPlayerPacket.class)
            .setName("y")
            .setSrgName("field_149477_b")
            //.autoAssign()
            .build();
    /** CPacketVehicleMove */
    FastField<Float> CPacketVehicleMove_yaw =
        FastFieldBuilder.create()
            .setInsideClass(CMoveVehiclePacket.class)
            .setName("yaw")
            .setSrgName("field_187010_d")
            //.autoAssign()
            .build();

    /** CPacketCloseWindow */
    FastField<Integer> CPacketCloseWindow_windowId =
        FastFieldBuilder.create()
            .setInsideClass(CCloseWindowPacket.class)
            .setName("windowId")
            .setSrgName("field_149556_a")
            //.autoAssign()
            .build();

    /** CPacketEntityAction */
    FastField<Integer> CPacketEntityAction_entityID =
        FastFieldBuilder.create()
            .setInsideClass(CEntityActionPacket.class)
            .setName("entityId")
            .setSrgName("field_149517_a")
            //.autoAssign()
            .build();

    /** SPacketPlayerPosLook */
    FastField<Float> SPacketPlayer_pitch =
        FastFieldBuilder.create()
            .setInsideClass(SPlayerPositionLookPacket.class)
            .setName("pitch")
            .setSrgName("field_148937_e")
            //.autoAssign()
            .build();

    FastField<Float> SPacketPlayer_yaw =
        FastFieldBuilder.create()
            .setInsideClass(SPlayerPositionLookPacket.class)
            .setName("yaw")
            .setSrgName("field_148936_d")
            //.autoAssign()
            .build();

    /** Entity */
    FastField<EntityDataManager> Entity_dataManager =
        FastFieldBuilder.create()
            .setInsideClass(Entity.class)
            .setName("dataManager")
            .setSrgName("field_70180_af")
            //.autoAssign()
            .build();

    FastField<Boolean> Entity_inPortal =
        FastFieldBuilder.create()
            .setInsideClass(Entity.class)
            .setName("inPortal")
            .setSrgName("field_71087_bX")
            //.autoAssign()
            .build();

    /** EntityPigZombie */
    FastField<Integer> EntityPigZombie_angerLevel =
        FastFieldBuilder.create()
            .setInsideClass(ZombiePigmanEntity.class)
            .setName("angerLevel")
            .setSrgName("field_70837_d")
            //.autoAssign()
            .build();

    /** PlayerEntity */
    FastField<Integer> EntityPlayer_sleepTimer =
        FastFieldBuilder.create()
            .setInsideClass(PlayerEntity.class)
            .setName("sleepTimer")
            .setSrgName("field_71076_b")
            //.autoAssign()
            .build();

    /** EntityPlayerSP */
    FastField<Float> EntityPlayerSP_horseJumpPower =
        FastFieldBuilder.create()
            .setInsideClass(ClientPlayerEntity.class)
            .setName("horseJumpPower")
            .setSrgName("field_110321_bQ")
            //.autoAssign()
            .build();

    /** GuiConnecting */
    FastField<NetworkManager> GuiConnecting_networkManager =
        FastFieldBuilder.create()
            .setInsideClass(ConnectingScreen.class)
            .setName("networkManager")
            .setSrgName("field_146371_g")
            //.autoAssign()
            .build();

    /** GuiDisconnected */
    FastField<Screen> GuiDisconnected_parentScreen =
        FastFieldBuilder.create()
            .setInsideClass(DisconnectedScreen.class)
            .setName("field_146307_h")//.setName("parentScreen")
            .setSrgName("field_146307_h")
            //.autoAssign()
            .build();

    FastField<ITextComponent> GuiDisconnected_message =
        FastFieldBuilder.create()
            .setInsideClass(DisconnectedScreen.class)
            .setName("message")
            .setSrgName("field_146304_f")
            //.autoAssign()
            .build();

    /** Minecraft */
    FastField<Integer> Minecraft_leftClickCounter =
        FastFieldBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("leftClickCounter")
            .setSrgName("field_71429_W")
            //.autoAssign()
            .build();

    FastField<Integer> Minecraft_rightClickDelayTimer =
        FastFieldBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("rightClickDelayTimer")
            .setSrgName("field_71467_ac")
            //.autoAssign()
            .build();
    FastField<Timer> Minecraft_timer =
        FastFieldBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("timer")
            .setSrgName("field_71428_T")
            //.autoAssign()
            .build();

    /** PlayerControllerMP */
    FastField<Integer> PlayerControllerMP_blockHitDelay =
        FastFieldBuilder.create()
            .setInsideClass(PlayerController.class)
            .setName("blockHitDelay")
            .setSrgName("field_78781_i")
            //.autoAssign()
            .build();

    FastField<Float> PlayerControllerMP_curBlockDamageMP =
        FastFieldBuilder.create()
            .setInsideClass(PlayerController.class)
            .setName("curBlockDamageMP")
            .setSrgName("field_78770_f")
            //.autoAssign()
            .build();

    FastField<Integer> PlayerControllerMP_currentPlayerItem =
        FastFieldBuilder.create()
            .setInsideClass(PlayerController.class)
            .setName("currentPlayerItem")
            .setSrgName("field_78777_l")
            //.autoAssign()
            .build();

    /** SPacketEntityVelocity */
    FastField<Integer> SPacketEntityVelocity_motionX =
        FastFieldBuilder.create()
            .setInsideClass(SEntityVelocityPacket.class)
            .setName("motionX")
            .setSrgName("field_149415_b")
            //.autoAssign()
            .build();

    FastField<Integer> SPacketEntityVelocity_motionY =
        FastFieldBuilder.create()
            .setInsideClass(SEntityVelocityPacket.class)
            .setName("motionY")
            .setSrgName("field_149416_c")
            //.autoAssign()
            .build();
    FastField<Integer> SPacketEntityVelocity_motionZ =
        FastFieldBuilder.create()
            .setInsideClass(SEntityVelocityPacket.class)
            .setName("motionZ")
            .setSrgName("field_149414_d")
            //.autoAssign()
            .build();

    /** SPacketExplosion */
    FastField<Float> SPacketExplosion_motionX =
        FastFieldBuilder.create()
            .setInsideClass(SExplosionPacket.class)
            .setName("motionX")
            .setSrgName("field_149152_f")
            //.autoAssign()
            .build();

    FastField<Float> SPacketExplosion_motionY =
        FastFieldBuilder.create()
            .setInsideClass(SExplosionPacket.class)
            .setName("motionY")
            .setSrgName("field_149153_g")
            //.autoAssign()
            .build();
    FastField<Float> SPacketExplosion_motionZ =
        FastFieldBuilder.create()
            .setInsideClass(SExplosionPacket.class)
            .setName("motionZ")
            .setSrgName("field_149159_h")
            //.autoAssign()
            .build();

    /** BufferBuilder */
    FastField<Boolean> BufferBuilder_isDrawing =
        FastFieldBuilder.create()
            .setInsideClass(BufferBuilder.class)
            .setName("isDrawing")
            .setSrgName("field_179010_r")
            //.autoAssign()
            .build();

    /** Session */
    FastField<String> Session_username =
        FastFieldBuilder.create()
            .setInsideClass(Session.class)
            .setName("username")
            .setSrgName("field_74286_b")
            //.autoAssign()
            .build();

    /** TextureManager */
    FastField<Map<ResourceLocation, ITextureObject>> TextureManager_mapTextureObjects =
        FastFieldBuilder.create()
            .setInsideClass(TextureManager.class)
            .setName("mapTextureObjects")
            .setSrgName("field_110585_a")
            //.autoAssign()
            .build();

    /** EntityRenderer */
    FastField<ItemStack> EntityRenderer_itemActivationItem =
        FastFieldBuilder.create()
            .setInsideClass(GameRenderer.class)
            .setName("itemActivationItem")
            .setSrgName("field_190566_ab")
            //.autoAssign()
            .build();

    /** AbstractHorse */
    FastField<IAttribute> AbstractHorse_JUMP_STRENGTH =
        FastFieldBuilder.create()
            .setInsideClass(AbstractHorseEntity.class)
            .setName("JUMP_STRENGTH")
            .setSrgName("field_110271_bv")
            //.autoAssign()
            .build();
    /** SharedMonsterAttributes */
    FastField<IAttribute> SharedMonsterAttributes_MOVEMENT_SPEED =
        FastFieldBuilder.create()
            .setInsideClass(SharedMonsterAttributes.class)
            .setName("MOVEMENT_SPEED")
            .setSrgName("field_111263_d")
            //.autoAssign()
            .build();
    /** GuiEditSign */
    FastField<SignTileEntity> GuiEditSign_tileSign =
        FastFieldBuilder.create()
            .setInsideClass(EditSignScreen.class)
            .setName("field_146848_f")//.setName("tileSign")
            .setSrgName("field_146848_f")
            //.autoAssign()
            .build();
    /** Timer */
    FastField<Float> Timer_tickLength =
        FastFieldBuilder.create()
            .setInsideClass(Timer.class)
            .setName("tickLength")
            .setSrgName("field_194149_e")
            //.autoAssign()
            .build();
    /** KeyBinding */
    FastField<Integer> Binding_pressTime =
        FastFieldBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("pressTime")
            .setSrgName("field_151474_i")
            //.autoAssign()
            .build();
    FastField<Boolean> Binding_pressed =
        FastFieldBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("pressed")
            .setSrgName("field_74513_e")
            //.autoAssign()
            .build();
    FastField<Set<String>> Binding_KEYBIND_SET =
        FastFieldBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("KEYBIND_SET")
            .setSrgName("field_151473_c")
            //.autoAssign()
            .build();

    /** ItemSword */
    // 1.14
    FastField<Float> ItemSword_attackDamage =
        FastFieldBuilder.create()
            .setInsideClass(SwordItem.class)
            .setName("attackDamage")
            .setSrgName("field_150934_a")
            //.autoAssign()
            .build();

    /** ItemTool */
    // 1.14
    FastField<Float> ItemTool_attackDamage =
        FastFieldBuilder.create()
            .setInsideClass(ToolItem.class)
            .setName("attackDamage")
            .setSrgName("field_77865_bY")
            //.autoAssign()
            .build();

    // 1.14
    FastField<Float> ItemTool_attackSpeed =
        FastFieldBuilder.create()
            .setInsideClass(ToolItem.class)
            .setName("attackSpeed")
            .setSrgName("field_185065_c")
            //.autoAssign()
            .build();


    /** Chunk */
    FastField<ChunkSection[]> Chunk_sections = // 1.14: renamed to 'sections' from 'storageArrays'
        FastFieldBuilder.create()
            .setInsideClass(Chunk.class)
            .setName("sections")
            .setSrgName("field_76652_q")
            //.autoAssign()
            .build();
    FastField<Boolean> Chunk_loaded =
        FastFieldBuilder.create()
            .setInsideClass(Chunk.class)
            .setName("loaded")
            .setSrgName("field_76636_d")
            //.autoAssign()
            .build();

    /** NativeImage */
    FastField<Long> NativeImage_imagePointer =
        FastFieldBuilder.create()
            .setInsideClass(NativeImage.class)
            .setName("imagePointer")
            .setSrgName("field_195722_d")
            //.autoAssign()
            .build();
  }

  // ****************************************
  // METHODS
  // ****************************************

  interface Methods {
    /** Block */
    FastMethod<Boolean> Block_onBlockActivated =
        FastMethodBuilder.create()
            .setInsideClass(Block.class)
            .setName("onBlockActivated")
            .setSrgName("func_220051_a")
            .setParameters(
                BlockState.class,
                World.class,
                BlockPos.class,
                PlayerEntity.class,
                Hand.class,
                BlockRayTraceResult.class
                )
            .setReturnType(boolean.class)
            //.autoAssign()
            .build();

    /** Entity */
    FastMethod<Boolean> Entity_getFlag =
            FastMethodBuilder.create()
            .setInsideClass(Entity.class)
            .setName("getFlag")
            .setSrgName("func_70083_f")
            .setParameters(int.class)
            .setReturnType(boolean.class)
            //.autoAssign()
            .build();

    FastMethod<Void> Entity_setFlag =
        FastMethodBuilder.create()
            .setInsideClass(Entity.class)
            .setName("setFlag")
            .setSrgName("func_70052_a")
            .setParameters(int.class, boolean.class)
            .setReturnType(void.class)
            //.autoAssign()
            .build();

    /** EntityLivingBase */
    FastMethod<Void> EntityLivingBase_resetPotionEffectMetadata =
        FastMethodBuilder.create()
            .setInsideClass(LivingEntity.class)
            .setName("resetPotionEffectMetadata")
            .setSrgName("func_175133_bi")
            .setParameters()
            .setReturnType(void.class)
            //.autoAssign()
            .build();

    /** Minecraft */
    FastMethod<Void> Minecraft_clickMouse =
        FastMethodBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("clickMouse")
            .setSrgName("func_147116_af")
            .setParameters()
            .setReturnType(void.class)
            //.autoAssign()
            .build();

    FastMethod<Void> Minecraft_rightClickMouse =
        FastMethodBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("rightClickMouse")
            .setSrgName("func_147121_ag")
            .setParameters()
            .setReturnType(void.class)
            //.autoAssign()
            .build();

    /** KeyBinding */
    FastMethod<Void> KeyBinding_unPress =
        FastMethodBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("unpressKey")
            .setSrgName("func_74505_d")
            .setParameters()
            .setReturnType(void.class)
            //.autoAssign()
            .build();

    /** IChunkLoader */
    FastMethod<ChunkLoader> ChunkLoader_writeChunk =
        FastMethodBuilder.create()
            .setInsideClass(ChunkLoader.class)
            .setName("writeChunk")
            .setSrgName("")
            .setParameters(ChunkPos.class, CompoundNBT.class)
            .setReturnType(void.class)
            //.autoAssign()
            .build();

    /** WorldRenderer */
    FastMethod<Void> WorldRenderer_renderBlockLayer =
        FastMethodBuilder.create()
            .setInsideClass(WorldRenderer.class)
            .setName("renderBlockLayer")
            .setSrgName("func_174982_a")
            .setParameters()
            .setReturnType(void.class)
            //.autoAssign()
            .build();
  }
}
