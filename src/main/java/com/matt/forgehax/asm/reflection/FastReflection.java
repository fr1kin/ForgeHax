package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastMethod;
import com.matt.forgehax.asm.utils.fasttype.builder.FastFieldBuilder;
import com.matt.forgehax.asm.utils.fasttype.builder.FastMethodBuilder;
import com.matt.forgehax.asm.utils.fasttype.builder.FastTypeBuilder;
import java.nio.FloatBuffer;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiConnecting;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.NativeImage;
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
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

/** Created on 5/8/2017 by fr1kin */
public interface FastReflection extends ASMCommon {
  // ****************************************
  // FIELDS
  // ****************************************
  interface Fields {
    /** ActiveRenderInfo */
    FastField<FloatBuffer> ActiveRenderInfo_MODELVIEW =
        FastFieldBuilder.create()
            .setInsideClass(ActiveRenderInfo.class)
            .setName("MODELVIEW")
            .autoAssign()
            .build();

    // TODO: this doesnt exist anymore
    @Deprecated
    FastField<FloatBuffer> ActiveRenderInfo_PROJECTION =
        FastFieldBuilder.create()
            .setInsideClass(ActiveRenderInfo.class)
            .setName("PROJECTION")
            .autoAssign()
            .build();
    FastField<Vec3d> ActiveRenderInfo_position =
        FastFieldBuilder.create()
            .setInsideClass(ActiveRenderInfo.class)
            .setName("position")
            .autoAssign()
            .build();

    /** CPacketPlayer */
    FastField<Float> CPacketPlayer_pitch =
        FastFieldBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("field_149473_f") // pitch
            .autoAssign()
            .build();

    FastField<Float> CPacketPlayer_yaw =
        FastFieldBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("field_149476_e") // yaw
            .autoAssign()
            .build();
    FastField<Boolean> CPacketPlayer_rotating =
        FastFieldBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("field_149481_i") // rotating
            .autoAssign()
            .build();
    FastField<Boolean> CPacketPlayer_onGround =
        FastFieldBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("field_149474_g") // onGround
            .autoAssign()
            .build();
    FastField<Double> CPacketPlayer_Y =
        FastFieldBuilder.create()
            .setInsideClass(CPacketPlayer.class)
            .setName("field_149477_b") // Y
            .autoAssign()
            .build();
    /** CPacketVehicleMove */
    FastField<Float> CPacketVehicleMove_yaw =
        FastFieldBuilder.create()
            .setInsideClass(CPacketVehicleMove.class)
            .setName("field_187010_d") // yaw
            .autoAssign()
            .build();

    /** CPacketCloseWindow */
    FastField<Integer> CPacketCloseWindow_windowId =
        FastFieldBuilder.create()
            .setInsideClass(CPacketCloseWindow.class)
            .setName("field_149556_a") // windowId
            .autoAssign()
            .build();

    /** CPacketEntityAction */
    FastField<Integer> CPacketEntityAction_entityID =
        FastFieldBuilder.create()
            .setInsideClass(CPacketEntityAction.class)
            .setName("field_149517_a") // entityId
            .autoAssign()
            .build();

    /** SPacketPlayerPosLook */
    FastField<Float> SPacketPlayer_pitch =
        FastFieldBuilder.create()
            .setInsideClass(SPacketPlayerPosLook.class)
            .setName("field_148937_e") // pitch
            .autoAssign()
            .build();

    FastField<Float> SPacketPlayer_yaw =
        FastFieldBuilder.create()
            .setInsideClass(SPacketPlayerPosLook.class)
            .setName("field_148936_d") // yaw
            .autoAssign()
            .build();

    /** Entity */
    FastField<EntityDataManager> Entity_dataManager =
        FastFieldBuilder.create()
            .setInsideClass(Entity.class)
            .setName("field_70180_af") // dataManager
            .autoAssign()
            .build();

    FastField<Boolean> Entity_inPortal =
        FastFieldBuilder.create()
            .setInsideClass(Entity.class)
            .setName("field_71087_bX") // inPortal
            .autoAssign()
            .build();

    /** EntityPigZombie */
    FastField<Integer> EntityPigZombie_angerLevel =
        FastFieldBuilder.create()
            .setInsideClass(EntityPigZombie.class)
            .setName("field_70837_d") // angerLevel
            .autoAssign()
            .build();

    /** EntityPlayer */
    FastField<Boolean> EntityPlayer_sleeping =
        FastFieldBuilder.create()
            .setInsideClass(EntityPlayer.class)
            .setName("field_71083_bS") // sleeping
            .autoAssign()
            .build();

    FastField<Integer> EntityPlayer_sleepTimer =
        FastFieldBuilder.create()
            .setInsideClass(EntityPlayer.class)
            .setName("field_71076_b") // sleepTimer
            .autoAssign()
            .build();

    /** EntityPlayerSP */
    FastField<Float> EntityPlayerSP_horseJumpPower =
        FastFieldBuilder.create()
            .setInsideClass(EntityPlayerSP.class)
            .setName("field_110321_bQ") // horseJumpPower
            .autoAssign()
            .build();

    /** GuiConnecting */
    FastField<NetworkManager> GuiConnecting_networkManager =
        FastFieldBuilder.create()
            .setInsideClass(GuiConnecting.class)
            .setName("field_146371_g") // networkManager
            .autoAssign()
            .build();

    /** GuiDisconnected */
    FastField<GuiScreen> GuiDisconnected_parentScreen =
        FastFieldBuilder.create()
            .setInsideClass(GuiDisconnected.class)
            .setName("field_146307_h") // parentScreen
            .autoAssign()
            .build();

    FastField<ITextComponent> GuiDisconnected_message =
        FastFieldBuilder.create()
            .setInsideClass(GuiDisconnected.class)
            .setName("field_146304_f") // message
            .autoAssign()
            .build();
    FastField<String> GuiDisconnected_reason =
        FastFieldBuilder.create()
            .setInsideClass(GuiDisconnected.class)
            .setName("field_146306_a") // reason
            .autoAssign()
            .build();

    /** Minecraft */
    FastField<Integer> Minecraft_leftClickCounter =
        FastFieldBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("field_71429_W") // leftClickCounter
            .autoAssign()
            .build();

    FastField<Integer> Minecraft_rightClickDelayTimer =
        FastFieldBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("field_71467_ac") // rightClickDelayTImer
            .autoAssign()
            .build();
    FastField<Timer> Minecraft_timer =
        FastFieldBuilder.create()
            .setInsideClass(Minecraft.class)
            .setName("field_71428_T") // timer
            .autoAssign()
            .build();

    /** PlayerControllerMP */
    FastField<Integer> PlayerControllerMP_blockHitDelay =
        FastFieldBuilder.create()
            .setInsideClass(PlayerControllerMP.class)
            .setName("field_78781_i") // blockHitDelay
            .autoAssign()
            .build();

    FastField<Float> PlayerControllerMP_curBlockDamageMP =
        FastFieldBuilder.create()
            .setInsideClass(PlayerControllerMP.class)
            .setName("field_78770_f") // curBlockDamageMP
            .autoAssign()
            .build();

    FastField<Integer> PlayerControllerMP_currentPlayerItem =
        FastFieldBuilder.create()
            .setInsideClass(PlayerControllerMP.class)
            .setName("field_78777_l") // currentPlayerItem
            .autoAssign()
            .build();

    /** SPacketEntityVelocity */
    FastField<Integer> SPacketEntityVelocity_motionX =
        FastFieldBuilder.create()
            .setInsideClass(SPacketEntityVelocity.class)
            .setName("field_149415_b") // motionX
            .autoAssign()
            .build();

    FastField<Integer> SPacketEntityVelocity_motionY =
        FastFieldBuilder.create()
            .setInsideClass(SPacketEntityVelocity.class)
            .setName("field_149416_c") // motionY
            .autoAssign()
            .build();
    FastField<Integer> SPacketEntityVelocity_motionZ =
        FastFieldBuilder.create()
            .setInsideClass(SPacketEntityVelocity.class)
            .setName("field_149414_d") // motionZ
            .autoAssign()
            .build();

    /** SPacketExplosion */
    FastField<Float> SPacketExplosion_motionX =
        FastFieldBuilder.create()
            .setInsideClass(SPacketExplosion.class)
            .setName("field_149152_f") // motionX
            .autoAssign()
            .build();

    FastField<Float> SPacketExplosion_motionY =
        FastFieldBuilder.create()
            .setInsideClass(SPacketExplosion.class)
            .setName("field_149153_g") // motionY
            .autoAssign()
            .build();
    FastField<Float> SPacketExplosion_motionZ =
        FastFieldBuilder.create()
            .setInsideClass(SPacketExplosion.class)
            .setName("field_149159_h") // motionZ
            .autoAssign()
            .build();

    /** BufferBuilder */
    FastField<Boolean> BufferBuilder_isDrawing =
        FastFieldBuilder.create()
            .setInsideClass(BufferBuilder.class)
            .setName("field_179010_r") // isDrawing
            .autoAssign()
            .build();

    /** Session */
    FastField<String> Session_username =
        FastFieldBuilder.create()
            .setInsideClass(Session.class)
            .setName("field_74286_b") // username
            .autoAssign()
            .build();

    /** TextureManager */
    FastField<Map<ResourceLocation, ITextureObject>> TextureManager_mapTextureObjects =
        FastFieldBuilder.create()
            .setInsideClass(TextureManager.class)
            .setName("field_110585_a") // mapTextureObjects
            .autoAssign()
            .build();

    /** EntityRenderer */
    FastField<ItemStack> EntityRenderer_itemActivationItem =
        FastFieldBuilder.create()
            .setInsideClass(GameRenderer.class)
            .setName("field_190566_ab") // itemActivationItem
            .autoAssign()
            .build();

    /** AbstractHorse */
    FastField<IAttribute> AbstractHorse_JUMP_STRENGTH =
        FastFieldBuilder.create()
            .setInsideClass(AbstractHorse.class)
            .setName("field_110271_bv") // JUMP_STRENGTH
            .autoAssign()
            .build();
    /** SharedMonsterAttributes */
    FastField<IAttribute> SharedMonsterAttributes_MOVEMENT_SPEED =
        FastFieldBuilder.create()
            .setInsideClass(SharedMonsterAttributes.class)
            .setName("field_111263_d") // MOVEMENT_SPEED
            .autoAssign()
            .build();
    /** GuiEditSign */
    FastField<TileEntitySign> GuiEditSign_tileSign =
        FastFieldBuilder.create()
            .setInsideClass(GuiEditSign.class)
            .setName("field_146848_f") // tileSign
            .autoAssign()
            .build();
    /** Timer */
    FastField<Float> Timer_tickLength =
        FastFieldBuilder.create()
            .setInsideClass(Timer.class)
            .setName("field_194149_e") // tickLength
            .autoAssign()
            .build();
    /** KeyBinding */
    FastField<Integer> Binding_pressTime =
        FastFieldBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("field_151474_i") // pressTime
            .autoAssign()
            .build();

    FastField<Boolean> Binding_pressed =
        FastFieldBuilder.create()
            .setInsideClass(KeyBinding.class)
            .setName("field_74513_e") // pressed
            .autoAssign()
            .build();

    /** ItemSword */
    FastField<Float> ItemSword_attackDamage =
        FastFieldBuilder.create()
            .setInsideClass(ItemSword.class)
            .setName("field_150934_a") // attackDamage
            .autoAssign()
            .build();

    /** ItemTool */
    FastField<Float> ItemTool_attackDamage =
        FastFieldBuilder.create()
            .setInsideClass(ItemTool.class)
            .setName("field_77865_bY") // attackDamage
            .autoAssign()
            .build();

    FastField<Float> ItemTool_attackSpeed =
        FastFieldBuilder.create()
            .setInsideClass(ItemTool.class)
            .setName("field_185065_c") // attackSpeed
            .autoAssign()
            .build();

    /** ItemFood */
    FastField<PotionEffect> ItemFood_potionId =
        FastFieldBuilder.create()
            .setInsideClass(ItemFood.class)
            .setName("field_77851_ca") // potionId
            .autoAssign()
            .build();

    /** Chunk */
    FastField<ChunkSection[]> Chunk_storageArrays =
        FastFieldBuilder.create()
            .setInsideClass(Chunk.class)
            .setName("field_76652_q") // storageArrays
            .autoAssign()
            .build();

    /** NativeImage */
    FastField<Long> NativeImage_imagePointer =
            FastFieldBuilder.create()
                    .setInsideClass(NativeImage.class)
                    .setName("imagePointer")
                    .autoAssign()
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
            .setSrgName("func_180639_a")
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
            .autoAssign()
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
            .setInsideClass(EntityLivingBase.class)
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
    FastMethod<AnvilChunkLoader> AnvilChunkLoader_writeChunkToNBT =
        FastMethodBuilder.create()
            .setInsideClass(AnvilChunkLoader.class)
            .setName("writeChunkToNBT")
            .setSrgName("func_75820_a")
            .setParameters(Chunk.class, World.class, NBTTagCompound.class)
            .setReturnType(void.class)
            //.autoAssign()
            .build();
  }
}
