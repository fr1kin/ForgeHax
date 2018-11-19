package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastMethod;
import com.matt.forgehax.asm.utils.fasttype.FastTypeBuilder;
import java.nio.FloatBuffer;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

/** Created on 5/8/2017 by fr1kin */
public interface FastReflection extends ASMCommon {
  // ****************************************
  // FIELDS
  // ****************************************
  interface Fields {
    /** ActiveRenderInfo */
    FastField<FloatBuffer> ActiveRenderInfo_MODELVIEW =
        FastTypeBuilder.create()
            .setInsideClass(ActiveRenderInfo.class)
            .setName("MODELVIEW")
            .autoAssign()
            .asField();

    FastField<FloatBuffer> ActiveRenderInfo_PROJECTION =
        FastTypeBuilder.create()
            .setInsideClass(ActiveRenderInfo.class)
            .setName("PROJECTION")
            .autoAssign()
            .asField();
    FastField<Vec3d> ActiveRenderInfo_position =
        FastTypeBuilder.create()
            .setInsideClass(ActiveRenderInfo.class)
            .setName("position")
            .autoAssign()
            .asField();

    /** CPacketPlayer */
    FastField<Float> CPacketPlayer_pitch =
        FastTypeBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("pitch")
            .autoAssign()
            .asField();

    FastField<Float> CPacketPlayer_yaw =
        FastTypeBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("yaw")
            .autoAssign()
            .asField();
    FastField<Boolean> CPacketPlayer_rotating =
        FastTypeBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("rotating")
            .autoAssign()
            .asField();
    FastField<Boolean> CPacketPlayer_onGround =
        FastTypeBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("onGround")
            .autoAssign()
            .asField();
    FastField<Double> CPacketPlayer_Y =
        FastTypeBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("y")
            .autoAssign()
            .asField();
    /** CPacketVehicleMove */
    FastField<Float> CPacketVehicleMove_yaw =
        FastTypeBuilder.create()
            .setInsideClass(CPacketVehicleMove.class)
            .setName("yaw")
            .autoAssign()
            .asField();

    /** CPacketCloseWindow */
    FastField<Integer> CPacketCloseWindow_windowId =
        FastTypeBuilder.create()
            .setInsideClass(CPacketCloseWindow.class)
            .setName("windowId")
            .autoAssign()
            .asField();

    /** CPacketEntityAction */
    FastField<Integer> CPacketEntityAction_entityID =
        FastTypeBuilder.create()
            .setInsideClass(CPacketEntityAction.class)
            .setName("entityID")
            .autoAssign()
            .asField();

    /** SPacketPlayerPosLook */
    FastField<Float> SPacketPlayer_pitch =
        FastTypeBuilder.create()
            .setInsideClass(SPacketPlayerPosLook.class)
            .setName("pitch")
            .autoAssign()
            .asField();

    FastField<Float> SPacketPlayer_yaw =
        FastTypeBuilder.create()
            .setInsideClass(SPacketPlayerPosLook.class)
            .setName("yaw")
            .autoAssign()
            .asField();

    /** Entity */
    FastField<EntityDataManager> Entity_dataManager =
        FastTypeBuilder.create()
            .setInsideClass(Entity.class)
            .setName("dataManager")
            .autoAssign()
            .asField();

    FastField<Boolean> Entity_inPortal =
        FastTypeBuilder.create()
            .setInsideClass(Entity.class)
            .setName("inPortal")
            .autoAssign()
            .asField();

    /** EntityPigZombie */
    FastField<Integer> EntityPigZombie_angerLevel =
        FastTypeBuilder.create()
            .setInsideClass(EntityPigZombie.class)
            .setName("angerLevel")
            .autoAssign()
            .asField();

    /** EntityPlayer */
    FastField<Boolean> EntityPlayer_sleeping =
        FastTypeBuilder.create()
            .setInsideClass(EntityPlayer.class)
            .setName("sleeping")
            .autoAssign()
            .asField();

    FastField<Integer> EntityPlayer_sleepTimer =
        FastTypeBuilder.create()
            .setInsideClass(EntityPlayer.class)
            .setName("sleepTimer")
            .autoAssign()
            .asField();

    /** EntityPlayerSP */
    FastField<Float> EntityPlayerSP_horseJumpPower =
        FastTypeBuilder.create()
            .setInsideClass(EntityPlayerSP.class)
            .setName("horseJumpPower")
            .autoAssign()
            .asField();

    /** GuiConnecting */
    FastField<NetworkManager> GuiConnecting_networkManager =
        FastTypeBuilder.create()
            .setInsideClass(GuiConnecting.class)
            .setName("networkManager")
            .autoAssign()
            .asField();

    /** GuiDisconnected */
    FastField<GuiScreen> GuiDisconnected_parentScreen =
        FastTypeBuilder.create()
            .setInsideClass(GuiDisconnected.class)
            .setName("parentScreen")
            .autoAssign()
            .asField();

    FastField<ITextComponent> GuiDisconnected_message =
        FastTypeBuilder.create()
            .setInsideClass(GuiDisconnected.class)
            .setName("message")
            .autoAssign()
            .asField();
    FastField<String> GuiDisconnected_reason =
        FastTypeBuilder.create()
            .setInsideClass(GuiDisconnected.class)
            .setName("reason")
            .autoAssign()
            .asField();

    /** Minecraft */
    FastField<Integer> Minecraft_leftClickCounter =
        FastTypeBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("leftClickCounter")
            .autoAssign()
            .asField();

    FastField<Integer> Minecraft_rightClickDelayTimer =
        FastTypeBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("rightClickDelayTimer")
            .autoAssign()
            .asField();
    FastField<Timer> Minecraft_timer =
        FastTypeBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("timer")
            .autoAssign()
            .asField();

    /** PlayerControllerMP */
    FastField<Integer> PlayerControllerMP_blockHitDelay =
        FastTypeBuilder.create()
            .setInsideClass(PlayerControllerMP.class)
            .setName("blockHitDelay")
            .autoAssign()
            .asField();

    FastField<Float> PlayerControllerMP_curBlockDamageMP =
        FastTypeBuilder.create()
            .setInsideClass(PlayerControllerMP.class)
            .setName("curBlockDamageMP")
            .autoAssign()
            .asField();

    FastField<Integer> PlayerControllerMP_currentPlayerItem =
        FastTypeBuilder.create()
            .setInsideClass(PlayerControllerMP.class)
            .setName("currentPlayerItem")
            .autoAssign()
            .asField();

    /** SPacketEntityVelocity */
    FastField<Integer> SPacketEntityVelocity_motionX =
        FastTypeBuilder.create()
            .setInsideClass(SPacketEntityVelocity.class)
            .setName("motionX")
            .autoAssign()
            .asField();

    FastField<Integer> SPacketEntityVelocity_motionY =
        FastTypeBuilder.create()
            .setInsideClass(SPacketEntityVelocity.class)
            .setName("motionY")
            .autoAssign()
            .asField();
    FastField<Integer> SPacketEntityVelocity_motionZ =
        FastTypeBuilder.create()
            .setInsideClass(SPacketEntityVelocity.class)
            .setName("motionZ")
            .autoAssign()
            .asField();

    /** SPacketExplosion */
    FastField<Float> SPacketExplosion_motionX =
        FastTypeBuilder.create()
            .setInsideClass(SPacketExplosion.class)
            .setName("motionX")
            .autoAssign()
            .asField();

    FastField<Float> SPacketExplosion_motionY =
        FastTypeBuilder.create()
            .setInsideClass(SPacketExplosion.class)
            .setName("motionY")
            .autoAssign()
            .asField();
    FastField<Float> SPacketExplosion_motionZ =
        FastTypeBuilder.create()
            .setInsideClass(SPacketExplosion.class)
            .setName("motionZ")
            .autoAssign()
            .asField();

    /** BufferBuilder */
    FastField<Boolean> BufferBuilder_isDrawing =
        FastTypeBuilder.create()
            .setInsideClass(BufferBuilder.class)
            .setName("isDrawing")
            .autoAssign()
            .asField();

    /** Session */
    FastField<String> Session_username =
        FastTypeBuilder.create()
            .setInsideClass(Session.class)
            .setName("username")
            .autoAssign()
            .asField();

    /** TextureManager */
    FastField<Map<ResourceLocation, ITextureObject>> TextureManager_mapTextureObjects =
        FastTypeBuilder.create()
            .setInsideClass(TextureManager.class)
            .setName("mapTextureObjects")
            .autoAssign()
            .asField();

    /** EntityRenderer */
    FastField<ItemStack> EntityRenderer_itemActivationItem =
        FastTypeBuilder.create()
            .setInsideClass(EntityRenderer.class)
            .setName("itemActivationItem")
            .autoAssign()
            .asField();

    /** AbstractHorse */
    FastField<IAttribute> AbstractHorse_JUMP_STRENGTH =
        FastTypeBuilder.create()
            .setInsideClass(AbstractHorse.class)
            .setName("JUMP_STRENGTH")
            .autoAssign()
            .asField();
    /** SharedMonsterAttributes */
    FastField<IAttribute> SharedMonsterAttributes_MOVEMENT_SPEED =
        FastTypeBuilder.create()
            .setInsideClass(SharedMonsterAttributes.class)
            .setName("MOVEMENT_SPEED")
            .autoAssign()
            .asField();
    /** GuiEditSign */
    FastField<TileEntitySign> GuiEditSign_tileSign =
        FastTypeBuilder.create()
            .setInsideClass(GuiEditSign.class)
            .setName("tileSign")
            .autoAssign()
            .asField();
    /** NBTTagCompound */
    FastField<Map<String, NBTBase>> NBTTag_tagMap =
        FastTypeBuilder.create()
            .setInsideClass(NBTTagCompound.class)
            .setName("tagMap")
            .autoAssign()
            .asField();
    /** Timer */
    FastField<Float> Timer_tickLength =
        FastTypeBuilder.create()
            .setInsideClass(Timer.class)
            .setName("tickLength")
            .autoAssign()
            .asField();
    /** KeyBinding */
    FastField<Integer> Binding_pressTime =
        FastTypeBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("pressTime")
            .autoAssign()
            .asField();

    FastField<Boolean> Binding_pressed =
        FastTypeBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("pressed")
            .autoAssign()
            .asField();

    /** ItemSword */
    FastField<Float> ItemSword_attackDamage =
        FastTypeBuilder.create()
            .setInsideClass(ItemSword.class)
            .setName("attackDamage")
            .autoAssign()
            .asField();

    /** ItemTool */
    FastField<Float> ItemTool_damageVsEntity =
        FastTypeBuilder.create()
            .setInsideClass(ItemTool.class)
            .setName("damageVsEntity")
            .autoAssign()
            .asField();

    FastField<Float> ItemTool_attackSpeed =
        FastTypeBuilder.create()
            .setInsideClass(ItemTool.class)
            .setName("attackSpeed")
            .autoAssign()
            .asField();

    /** ItemFood */
    FastField<PotionEffect> ItemFood_potionId =
        FastTypeBuilder.create()
            .setInsideClass(ItemFood.class)
            .setName("potionId")
            .autoAssign()
            .asField();
  }

  // ****************************************
  // METHODS
  // ****************************************

  interface Methods {
    /** Block */
    FastMethod<Boolean> Block_onBlockActivated =
        FastTypeBuilder.create()
            .setInsideClass(Block.class)
            .setName("onBlockActivated")
            .setParameters(
                World.class,
                BlockPos.class,
                IBlockState.class,
                EntityPlayer.class,
                EnumHand.class,
                EnumFacing.class,
                float.class,
                float.class,
                float.class)
            .setReturnType(boolean.class)
            .autoAssign()
            .asMethod();

    /** Entity */
    FastMethod<Boolean> Entity_getFlag =
        FastTypeBuilder.create()
            .setInsideClass(Entity.class)
            .setName("getFlag")
            .setParameters(int.class)
            .setReturnType(boolean.class)
            .autoAssign()
            .asMethod();

    FastMethod<Void> Entity_setFlag =
        FastTypeBuilder.create()
            .setInsideClass(Entity.class)
            .setName("setFlag")
            .setParameters(int.class, boolean.class)
            .setReturnType(void.class)
            .autoAssign()
            .asMethod();

    /** EntityLivingBase */
    FastMethod<Void> EntityLivingBase_resetPotionEffectMetadata =
        FastTypeBuilder.create()
            .setInsideClass(EntityLivingBase.class)
            .setName("resetPotionEffectMetadata")
            .setParameters()
            .setReturnType(void.class)
            .autoAssign()
            .asMethod();

    /** Minecraft */
    FastMethod<Void> Minecraft_clickMouse =
        FastTypeBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("clickMouse")
            .setParameters()
            .setReturnType(void.class)
            .autoAssign()
            .asMethod();

    FastMethod<Void> Minecraft_rightClickMouse =
        FastTypeBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("rightClickMouse")
            .setParameters()
            .setReturnType(void.class)
            .autoAssign()
            .asMethod();

    /** KeyBinding */
    FastMethod<Void> KeyBinding_unPress =
        FastTypeBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("unpressKey")
            .setParameters()
            .setReturnType(void.class)
            .autoAssign()
            .asMethod();

    /** IChunkLoader */
    FastMethod<AnvilChunkLoader> AnvilChunkLoader_writeChunkToNBT =
        FastTypeBuilder.create()
            .setInsideClass(AnvilChunkLoader.class)
            .setName("writeChunkToNBT")
            .setParameters(Chunk.class, World.class, NBTTagCompound.class)
            .setReturnType(void.class)
            .autoAssign()
            .asMethod();
  }
}
