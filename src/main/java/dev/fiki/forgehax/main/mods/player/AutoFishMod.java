package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionMethod;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 9/2/2016 by fr1kin
 */
@RegisterMod(
    name = "AutoFish",
    description = "Auto fish",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AutoFishMod extends ToggleMod {
  @MethodMapping(parentClass = Minecraft.class, value = "rightClickMouse")
  private final ReflectionMethod<Void> Minecraft_rightClickMouse;

  private int ticksCastDelay = 0;
  private int ticksHookDeployed = 0;

  private boolean previouslyHadRodEquipped = false;

  public final IntegerSetting castingDelay = newIntegerSetting()
      .name("casting-delay")
      .description("Number of ticks to wait after casting the rod to attempt a recast")
      .min(0)
      .defaultTo(20)
      .build();

  public final DoubleSetting maxSoundDistance = newDoubleSetting()
      .name("max-sound-distance")
      .description("Maximum distance between the splash sound and hook entity allowed"
          + " (set to 0 to disable this feature)")
      .min(0)
      .defaultTo(2.D)
      .build();

  public final IntegerSetting recastDelay = newIntegerSetting()
      .name("recast-delay")
      .description("Maximum amount of time (in ticks) allowed until the hook is pulled in"
          + "(set to 0 to disable this feature)")
      .min(0)
      .defaultTo(600)
      .build();

  private boolean isCorrectSplashPacket(SPlaySoundEffectPacket packet) {
    ClientPlayerEntity me = getLocalPlayer();
    return packet.getSound().equals(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH)
        && (me != null
        && me.fishingBobber != null
        && (maxSoundDistance.getValue() == 0 // disables this check
        || (me.fishingBobber.getPositionVec()
        .distanceTo(new Vector3d(packet.getX(), packet.getY(), packet.getZ())) <= maxSoundDistance.getValue())));
  }

  private void rightClick() {
    if (ticksCastDelay <= 0) { // to prevent the fishing rod from being spammed when in hand
      Minecraft_rightClickMouse.invoke(MC);
      ticksCastDelay = castingDelay.getValue();
      ticksHookDeployed = 0;
    }
  }

  private void resetLocals() {
    ticksCastDelay = 0;
    ticksHookDeployed = 0;
    previouslyHadRodEquipped = false;
  }

  @Override
  public void onEnabled() {
    resetLocals();
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    ClientPlayerEntity me = getLocalPlayer();
    ItemStack heldStack = me.getHeldItemMainhand();

    // update tick delay if hook is deployed
    if (ticksCastDelay > castingDelay.getValue()) {
      ticksCastDelay = castingDelay.getValue(); // greater than current delay, set to the current delay
    } else if (ticksCastDelay > 0) {
      --ticksCastDelay;
    }

    // check if player is holding a fishing rod
    if (Items.FISHING_ROD.equals(heldStack.getItem())) { // item being held is a fishing rod {
      if (!previouslyHadRodEquipped) {
        ticksCastDelay = castingDelay.getValue();
        previouslyHadRodEquipped = true;
      } else if (me.fishingBobber == null) { // no hook is deployed
        // cast hook
        rightClick();
      } else { // hook is deployed and rod was not previously equipped
        // increment the number of ticks that the hook entity has existed
        ++ticksHookDeployed;

        FishingBobberEntity bobber = me.fishingBobber;
        boolean notInWater = false;

        // check if the bobber is not moving at all
        if (bobber.getMotion().subtract(0, bobber.getMotion().getY(), 0).lengthSquared() == 0) {
          notInWater = !getWorld().getBlockState(bobber.getPosition()).getMaterial().isLiquid();
        }

        if (notInWater || (recastDelay.getValue() != 0 && (ticksHookDeployed > recastDelay.getValue()))) {
          rightClick(); // reel in hook if the fail safe time has passed
        }
      }
    } else {
      resetLocals();
    }
  }

  @SubscribeEvent
  public void onMouseEvent(InputEvent.MouseInputEvent event) {
    if (getGameSettings().keyBindUseItem.isKeyDown() && ticksHookDeployed > 0) {
      ticksCastDelay = castingDelay.getValue();
    }
  }

  @SubscribeEvent
  public void onPacketIncoming(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlaySoundEffectPacket) {
      SPlaySoundEffectPacket packet = (SPlaySoundEffectPacket) event.getPacket();
      if (isCorrectSplashPacket(packet)) {
        rightClick();
      }
    }
  }
}
