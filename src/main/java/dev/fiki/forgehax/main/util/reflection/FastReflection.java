package dev.fiki.forgehax.main.util.reflection;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.util.reflection.fasttype.FastField;
import dev.fiki.forgehax.main.util.reflection.fasttype.FastMethod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
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
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ChunkLoader;

import java.util.List;
import java.util.Map;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflection {

  // ****************************************
  // FIELDS
  // ****************************************
  interface Fields {

    /**
     * ActiveRenderInfo
     */

    FastField<Vector3f> ActiveRenderInfo_left =
        FastField.builder()
            .parent(ActiveRenderInfo.class)
            .mcp("left")
            .srg("field_216796_h")
            .build();

    /**
     * SEntityStatusPacket
     */

    FastField<Integer> SEntityStatusPacket_entityId =
        FastField.builder()
            .mcp("entityId")
            .srg("field_149164_a")
            .build();

    /**
     * CPlayerPacket
     */
    FastField<Float> CPacketPlayer_pitch =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("pitch")
            .srg("field_149473_f")
            .build();

    FastField<Float> CPacketPlayer_yaw =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("yaw")
            .srg("field_149476_e")
            .build();

    FastField<Boolean> CPacketPlayer_moving =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("moving")
            .srg("field_149480_h")
            .build();

    FastField<Boolean> CPacketPlayer_rotating =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("rotating")
            .srg("field_149481_i")
            .build();

    FastField<Boolean> CPacketPlayer_onGround =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("onGround")
            .srg("field_149474_g")
            .build();

    FastField<Double> CPacketPlayer_x =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("x")
            .srg("field_149479_a")
            .build();

    FastField<Double> CPacketPlayer_y =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("y")
            .srg("field_149477_b")
            .build();

    FastField<Double> CPacketPlayer_z =
        FastField.builder()
            .parent(CPlayerPacket.class)
            .mcp("z")
            .srg("field_149478_c")
            .build();
    /**
     * CMoveVehiclePacket
     */
    FastField<Float> CMoveVehiclePacket_yaw =
        FastField.builder()
            .parent(CMoveVehiclePacket.class)
            .mcp("yaw")
            .srg("field_187010_d")
            .build();

    /**
     * CPacketCloseWindow
     */
    FastField<Integer> CCloseWindowPacket_windowId =
        FastField.builder()
            .parent(CCloseWindowPacket.class)
            .mcp("windowId")
            .srg("field_149556_a")
            .build();

    /**
     * CPacketEntityAction
     */
    FastField<Integer> CEntityActionPacket_entityID =
        FastField.builder()
            .parent(CEntityActionPacket.class)
            .mcp("entityID")
            .srg("field_149517_a")
            .build();

    /**
     * SPacketPlayerPosLook
     */
    FastField<Float> SPlayerPositionLookPacket_pitch =
        FastField.builder()
            .parent(SPlayerPositionLookPacket.class)
            .mcp("pitch")
            .srg("field_148937_e")
            .build();

    FastField<Float> SPlayerPositionLookPacket_yaw =
        FastField.builder()
            .parent(SPlayerPositionLookPacket.class)
            .mcp("yaw")
            .srg("field_148936_d")
            .build();

    FastField<Double> SPlayerPositionLookPacket_x =
        FastField.builder()
            .parent(SPlayerPositionLookPacket.class)
            .mcp("x")
            .srg("field_148940_a")
            .build();

    FastField<Double> SPlayerPositionLookPacket_y =
        FastField.builder()
            .parent(SPlayerPositionLookPacket.class)
            .mcp("y")
            .srg("field_148938_b")
            .build();

    FastField<Double> SPlayerPositionLookPacket_z =
        FastField.builder()
            .parent(SPlayerPositionLookPacket.class)
            .mcp("z")
            .srg("field_148939_c")
            .build();

    /**
     * Entity
     */
    FastField<EntityDataManager> Entity_dataManager =
        FastField.builder()
            .parent(Entity.class)
            .mcp("dataManager")
            .srg("field_70180_af")
            .build();

    FastField<Boolean> Entity_inPortal =
        FastField.builder()
            .parent(Entity.class)
            .mcp("inPortal")
            .srg("field_71087_bX")
            .build();

    /**
     * EntityPigZombie
     */
    FastField<Integer> ZombiePigmanEntity_angerLevel =
        FastField.builder()
            .parent(ZombiePigmanEntity.class)
            .mcp("angerLevel")
            .srg("field_70837_d")
            .build();

    /**
     * EntityPlayer
     */
    @Deprecated
    FastField<Boolean> PlayerEntity_sleeping =
        FastField.builder()
            .parent(PlayerEntity.class)
            .mcp("sleeping")
            .build();

    FastField<Integer> PlayerEntity_sleepTimer =
        FastField.builder()
            .parent(PlayerEntity.class)
            .mcp("sleepTimer")
            .srg("field_71076_b")
            .build();

    /**
     * ClientPlayerEntity
     */

    FastField<Float> ClientPlayerEntity_horseJumpPower =
        FastField.builder()
            .parent(ClientPlayerEntity.class)
            .mcp("horseJumpPower")
            .srg("field_110321_bQ")
            .build();

    /**
     * GuiConnecting
     */

    FastField<NetworkManager> ConnectingScreen_networkManager =
        FastField.builder()
            .parent(ConnectingScreen.class)
            .mcp("networkManager")
            .srg("field_146371_g")
            .build();

    /**
     * net.minecraft.client.network.play.NetworkPlayerInfo
     */

    FastField<GameType> NetworkPlayerInfo_gameType =
        FastField.builder()
            .parent(NetworkPlayerInfo.class)
            .mcp("gameType")
            .srg("field_178866_b")
            .build();

    /**
     * GuiDisconnected
     */
    @Deprecated
    FastField<Screen> DisconnectedScreen_parentScreen =
        FastField.builder()
            .parent(DisconnectedScreen.class)
            .mcp("parentScreen")
            .build();

    FastField<ITextComponent> DisconnectedScreen_message =
        FastField.builder()
            .parent(DisconnectedScreen.class)
            .mcp("message")
            .srg("field_146304_f")
            .build();

    FastField<List<String>> DisconnectedScreen_multilineMessage =
        FastField.builder()
            .parent(DisconnectedScreen.class)
            .mcp("multilineMessage")
            .srg("field_146305_g")
            .build();

    /**
     * Minecraft
     */

    FastField<Integer> Minecraft_leftClickCounter =
        FastField.builder()
            .parent(Minecraft.class)
            .mcp("leftClickCounter")
            .srg("field_71429_W")
            .build();

    FastField<Integer> Minecraft_rightClickDelayTimer =
        FastField.builder()
            .parent(Minecraft.class)
            .mcp("rightClickDelayTimer")
            .srg("field_71467_ac")
            .build();

    FastField<Timer> Minecraft_timer =
        FastField.builder()
            .parent(Minecraft.class)
            .mcp("timer")
            .srg("field_71428_T")
            .build();

    /**
     * PlayerControllerMP
     */

    FastField<Integer> PlayerController_blockHitDelay =
        FastField.builder()
            .parent(PlayerController.class)
            .mcp("blockHitDelay")
            .srg("field_78781_i")
            .build();

    FastField<Float> PlayerController_curBlockDamageMP =
        FastField.builder()
            .parent(PlayerController.class)
            .mcp("curBlockDamageMP")
            .srg("field_78770_f")
            .build();

    FastField<Integer> PlayerController_currentPlayerItem =
        FastField.builder()
            .parent(PlayerController.class)
            .mcp("currentPlayerItem")
            .srg("field_78777_l")
            .build();

    /**
     * SPacketEntityVelocity
     */

    FastField<Integer> SEntityVelocityPacket_motionX =
        FastField.builder()
            .parent(SEntityVelocityPacket.class)
            .mcp("motionX")
            .srg("field_149415_b")
            .build();

    FastField<Integer> SEntityVelocityPacket_motionY =
        FastField.builder()
            .parent(SEntityVelocityPacket.class)
            .mcp("motionY")
            .srg("field_149416_c")
            .build();

    FastField<Integer> SEntityVelocityPacket_motionZ =
        FastField.builder()
            .parent(SEntityVelocityPacket.class)
            .mcp("motionZ")
            .srg("field_149414_d")
            .build();

    /**
     * SPacketExplosion
     */

    FastField<Float> SExplosionPacket_motionX =
        FastField.builder()
            .parent(SExplosionPacket.class)
            .mcp("motionX")
            .srg("field_149152_f")
            .build();

    FastField<Float> SExplosionPacket_motionY =
        FastField.builder()
            .parent(SExplosionPacket.class)
            .mcp("motionY")
            .srg("field_149153_g")
            .build();

    FastField<Float> SExplosionPacket_motionZ =
        FastField.builder()
            .parent(SExplosionPacket.class)
            .mcp("motionZ")
            .srg("field_149159_h")
            .build();

    /**
     * BufferBuilder
     */

    FastField<Integer> BufferBuilder_drawMode =
        FastField.builder()
            .parent(BufferBuilder.class)
            .mcp("drawMode")
            .srg("field_179006_k")
            .build();

    /**
     * Session
     */

    FastField<String> Session_username =
        FastField.builder()
            .parent(Session.class)
            .mcp("username")
            .srg("field_74286_b")
            .build();

    /**
     * TextureManager
     */

    FastField<Map<ResourceLocation, Texture>> TextureManager_mapTextureObjects =
        FastField.builder()
            .parent(TextureManager.class)
            .mcp("mapTextureObjects")
            .srg("field_110585_a")
            .build();

    /**
     * GameRenderer
     */

    FastField<ItemStack> GameRenderer_itemActivationItem =
        FastField.builder()
            .parent(GameRenderer.class)
            .mcp("itemActivationItem")
            .srg("field_190566_ab")
            .build();

    /**
     * AbstractHorse
     */

    FastField<IAttribute> AbstractHorse_JUMP_STRENGTH =
        FastField.builder()
            .parent(AbstractHorseEntity.class)
            .mcp("JUMP_STRENGTH")
            .srg("field_110271_bv")
            .build();

    /**
     * SharedMonsterAttributes
     */

    FastField<IAttribute> SharedMonsterAttributes_MOVEMENT_SPEED =
        FastField.builder()
            .parent(SharedMonsterAttributes.class)
            .mcp("MOVEMENT_SPEED")
            .srg("field_111263_d")
            .build();

    /**
     * GuiEditSign
     */

    FastField<SignTileEntity> GuiEditSign_tileSign =
        FastField.builder()
            .parent(EditSignScreen.class)
            .mcp("tileSign")
            .srg("field_146848_f")
            .build();

    /**
     * Timer
     */

    FastField<Float> Timer_tickLength =
        FastField.builder()
            .parent(Timer.class)
            .mcp("tickLength")
            .srg("field_194149_e")
            .build();

    /**
     * KeyBinding
     */

    FastField<Integer> KeyBinding_pressTime =
        FastField.builder()
            .parent(KeyBinding.class)
            .mcp("pressTime")
            .srg("field_151474_i")
            .build();

    FastField<Boolean> KeyBinding_pressed =
        FastField.builder()
            .parent(KeyBinding.class)
            .mcp("pressed")
            .srg("field_74513_e")
            .build();

    /**
     * ItemSword
     */

    FastField<Float> SwordItem_attackDamage =
        FastField.builder()
            .parent(SwordItem.class)
            .mcp("attackDamage")
            .srg("field_150934_a")
            .build();

    /**
     * ItemTool
     */
    FastField<Float> ToolItem_damageVsEntity =
        FastField.builder()
            .parent(ToolItem.class)
            .mcp("damageVsEntity")
            .srg("field_77865_bY")
            .build();

    FastField<Float> ToolItem_attackSpeed =
        FastField.builder()
            .parent(ToolItem.class)
            .mcp("attackSpeed")
            .srg("field_185065_c")
            .build();

    /**
     * InputMappings
     */

    FastField<Map<String, InputMappings.Input>> InputMappings_REGISTRY =
        FastField.builder()
            .parent(InputMappings.Input.class)
            .mcp("REGISTRY")
            .srg("field_199875_d")
            .build();
  }

  // ****************************************
  // METHODS
  // ****************************************

  interface Methods {

    /**
     * Block
     */
    FastMethod<Boolean> Block_onBlockActivated =
        FastMethod.builder()
            .parent(Block.class)
            .mcp("onBlockActivated")
            .srg("func_225533_a_")
            .argument(BlockState.class)
            .argument(World.class)
            .argument(BlockPos.class)
            .argument(BlockState.class)
            .argument(boolean.class)
            .build();

    /**
     * EntityLivingBase
     */
    FastMethod<Void> LivingEntity_resetPotionEffectMetadata =
        FastMethod.builder()
            .parent(LivingEntity.class)
            .mcp("resetPotionEffectMetadata")
            .srg("func_175133_bi")
            .noArguments()
            .build();

    /**
     * GameRenderer
     */

    FastMethod<Double> GameRenderer_getFOVModifier =
        FastMethod.builder()
            .parent(GameRenderer.class)
            .mcp("getFOVModifier")
            .srg("func_215311_a")
            .argument(ActiveRenderInfo.class)
            .argument(float.class)
            .argument(boolean.class)
            .build();

    FastMethod<Void> GameRenderer_hurtCameraEffect =
        FastMethod.builder()
            .parent(GameRenderer.class)
            .mcp("hurtCameraEffect")
            .srg("func_228380_a_")
            .argument(MatrixStack.class)
            .argument(float.class)
            .build();

    /**
     * Minecraft
     */
    FastMethod<Void> Minecraft_clickMouse =
        FastMethod.builder()
            .parent(Minecraft.class)
            .mcp("clickMouse")
            .srg("func_147116_af")
            .noArguments()
            .build();

    FastMethod<Void> Minecraft_rightClickMouse =
        FastMethod.builder()
            .parent(Minecraft.class)
            .mcp("rightClickMouse")
            .srg("func_147121_ag")
            .noArguments()
            .build();

    /**
     * KeyBinding
     */
    FastMethod<Void> KeyBinding_unPress =
        FastMethod.builder()
            .parent(KeyBinding.class)
            .mcp("unpressKey")
            .srg("func_74505_d")
            .noArguments()
            .build();

    /**
     * IChunkLoader
     */
    FastMethod<Void> AnvilChunkLoader_writeChunkToNBT =
        FastMethod.builder()
            .parent(ChunkLoader.class)
            .mcp("writeChunkToNBT")
            .srg("func_219100_a")
            .argument(ChunkPos.class)
            .argument(CompoundNBT.class)
            .build();
  }
}
