package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * Created on 9/2/2016 by fr1kin
 */
@RegisterMod
public class AutoFishMod extends ToggleMod {
  
  private int ticksCastDelay = 0;
  private int ticksHookDeployed = 0;
  
  private boolean previouslyHadRodEquipped = false;
  
  public final Setting<Integer> casting_delay =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("casting_delay")
          .description("Number of ticks to wait after casting the rod to attempt a recast")
          .defaultTo(20)
          .build();
  
  public final Setting<Double> max_sound_distance =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("max_sound_distance")
          .description(
              "Maximum distance between the splash sound and hook entity allowed (set to 0 to disable this feature)")
          .defaultTo(2.D)
          .build();
  
  public final Setting<Integer> fail_safe_time =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("fail_safe_time")
          .description(
              "Maximum amount of time (in ticks) allowed until the hook is pulled in (set to 0 to disable this feature)")
          .defaultTo(600)
          .build();
  
  public AutoFishMod() {
    super(Category.PLAYER, "AutoFish", false, "Auto fish");
  }
  
  private boolean isCorrectSplashPacket(SPacketSoundEffect packet) {
    EntityPlayerSP me = getLocalPlayer();
    return packet.getSound().equals(SoundEvents.ENTITY_BOBBER_SPLASH)
        && (me != null
        && me.fishEntity != null
        && (max_sound_distance.get() == 0
        || // disables this check
        (me.fishEntity
            .getPositionVector()
            .distanceTo(new Vec3d(packet.getX(), packet.getY(), packet.getZ()))
            <= max_sound_distance.get())));
  }
  
  private void rightClick() {
    if (ticksCastDelay <= 0) { // to prevent the fishing rod from being spammed when in hand
      FastReflection.Methods.Minecraft_rightClickMouse.invoke(MC);
      ticksCastDelay = casting_delay.get();
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
    EntityPlayer me = getLocalPlayer();
    ItemStack heldStack = me.getHeldItemMainhand();
    
    // update tick delay if hook is deployed
    if (ticksCastDelay > casting_delay.get()) {
      ticksCastDelay = casting_delay.get(); // greater than current delay, set to the current delay
    } else if (ticksCastDelay > 0) {
      --ticksCastDelay;
    }
    
    // check if player is holding a fishing rod
    if (heldStack != null
        && // item not null (shouldn't be, but I am being safe)
        heldStack.getItem() instanceof ItemFishingRod // item being held is a fishing rod
    ) {
      if (!previouslyHadRodEquipped) {
        ticksCastDelay = casting_delay.get();
        previouslyHadRodEquipped = true;
      } else if (me.fishEntity == null) { // no hook is deployed
        // cast hook
        rightClick();
      } else { // hook is deployed and rod was not previously equipped
        // increment the number of ticks that the hook entity has existed
        ++ticksHookDeployed;
        
        if (fail_safe_time.get() != 0 && (ticksHookDeployed > fail_safe_time.get())) {
          rightClick(); // reel in hook if the fail safe time has passed
          resetLocals();
        }
      }
    } else {
      resetLocals();
    }
  }
  
  @SubscribeEvent
  public void onMouseEvent(InputEvent.MouseInputEvent event) {
    if (MC.gameSettings.keyBindUseItem.isKeyDown() && ticksHookDeployed > 0) {
      ticksCastDelay = casting_delay.get();
    }
  }
  
  @SubscribeEvent
  public void onPacketIncoming(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketSoundEffect) {
      SPacketSoundEffect packet = event.getPacket();
      if (isCorrectSplashPacket(packet)) {
        rightClick();
      }
    }
  }
}
