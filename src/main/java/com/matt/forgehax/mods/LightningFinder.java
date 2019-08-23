package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.printInform;
import static com.matt.forgehax.Helper.printWarning;

@RegisterMod
public class LightningFinder extends ToggleMod {
  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> warning =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("warning")
          .description("warn about the patch")
          .defaultTo(true)
          .changed(change -> {
            if (isEnabled() && change.getTo()) MC.addScheduledTask(this::doWarning);
          })
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Integer> minDistance =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("min-distance")
          .description("how far the strike has to be from you")
          .min(0)
          .defaultTo(32)
          .build();

  public LightningFinder() {
    super(Category.MISC, "LightningFinder", false, "Logs positions of lightning strikes to chat");
  }

  private void doWarning() {
    printWarning(
        "Warning: Spigot (and forks) have patched this lightning exploit and don't provide absolute coordinates." +
            " This is still safe to use on Vanilla and Forge servers."
    );

    warning.set(false);
  }

  @Override
  protected void onEnabled() {
    super.onEnabled();

    if (warning.get()) MC.addScheduledTask(this::doWarning);
  }

  @SubscribeEvent
  public void onPacketRecieving(PacketEvent.Incoming.Pre event) {
    if (!(event.getPacket() instanceof SPacketSoundEffect)) return;

    SPacketSoundEffect packet = event.getPacket();

    // in the SPacketSpawnGlobalEntity constructor, this is only set to 1 if it's a lightning bolt
    if (packet.getSound() != SoundEvents.ENTITY_LIGHTNING_THUNDER) return;

    BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
    EntityPlayer player = getLocalPlayer();

    if (player.getDistanceSqToCenter(pos) >= Math.pow(minDistance.get(), 2))
      MC.addScheduledTask(() -> printInform(
          "Lightning just struck @ [x:%d, y:%d, z:%d]",
          pos.getX(),
          pos.getY(),
          pos.getZ()
      ));
  }

  @Override
  public String getDebugDisplayText() {
    return String.format(
        "%s[>%d]",
        super.getDisplayText(),
        minDistance.get()
    );
  }
}
