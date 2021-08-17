package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.GeneralEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "Step",
    description = "Step up blocks",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({GeneralEx.class})
public class StepMod extends ToggleMod {
  private static final float DEFAULT_STEP_HEIGHT = 0.6f;

  private final ReflectionTools reflection;

  private final BooleanSetting entityStep = newBooleanSetting()
      .name("entity-step")
      .description("entitystep")
      .defaultTo(false)
      .build();

  private final FloatSetting stepHeight = newFloatSetting()
      .name("height")
      .description("how high you can step")
      .defaultTo(1.2f)
      .min(0f)
      .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
      .changedListener((from, to) -> {
        if (isEnabled()) {
          PlayerEntity player = getLocalPlayer();
          if (player != null) {
            updateStepHeight(player);
          }
        }
      })
      .build();

  private final BooleanSetting unstep = newBooleanSetting()
      .name("unstep")
      .description("step down instead of falling")
      .defaultTo(false)
      .build();

  @Override
  protected void onEnabled() {
    PlayerEntity player = getLocalPlayer();
    if (player != null) {
      wasOnGround = reflection.Entity_onGround.get(player);
    }
  }

  @Override
  public void onDisabled() {
    PlayerEntity player = getLocalPlayer();
    if (player != null) {
      player.maxUpStep = DEFAULT_STEP_HEIGHT;
    }

    if (getMountedEntity() != null) {
      getMountedEntity().maxUpStep = 1;
    }
  }

  private void updateStepHeight(PlayerEntity player) {
    player.maxUpStep = reflection.Entity_onGround.get(player) ? stepHeight.getValue() : DEFAULT_STEP_HEIGHT;
  }

  private boolean wasOnGround = false;

  private void unstep(PlayerEntity player) {
    AxisAlignedBB range = player.getBoundingBox().inflate(0, -stepHeight.getValue(), 0)
        .contract(0, player.getBbHeight(), 0);

    if (!player.level.noCollision(range)) {
      return;
    }

    List<AxisAlignedBB> collisionBoxes = player.level.getBlockCollisions(player, range)
        .map(VoxelShape::bounds)
        .collect(Collectors.toList());
    AtomicReference<Double> newY = new AtomicReference<>(0D);
    collisionBoxes.forEach(box -> newY.set(Math.max(newY.get(), box.maxY)));
    player.moveTo(player.getX(), newY.get(), player.getZ());
  }

  private void updateUnstep(PlayerEntity player) {
    try {
      if (unstep.getValue() && wasOnGround && !reflection.Entity_onGround.get(player) && player.getDeltaMovement().y() <= 0) {
        unstep(player);
      }
    } finally {
      wasOnGround = reflection.Entity_onGround.get(player);
    }
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    PlayerEntity player = event.getPlayer();
    if (player == null) {
      return;
    }

    updateStepHeight(player);
    updateUnstep(player);

    if (getMountedEntity() != null) {
      if (entityStep.getValue()) {
        getMountedEntity().maxUpStep = 256;
      } else {
        getMountedEntity().maxUpStep = 1;
      }
    }
  }

  private CPlayerPacket previousPositionPacket = null;

  @SubscribeListener
  public void onPacketSending(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CPlayerPacket.PositionPacket
        || event.getPacket() instanceof CPlayerPacket.PositionRotationPacket) {
      CPlayerPacket packetPlayer = (CPlayerPacket) event.getPacket();
      if (previousPositionPacket != null) {
        double diffY = packetPlayer.getY(0.f) - previousPositionPacket.getY(0.f);
        // y difference must be positive
        // greater than 1, but less than 1.5
        if (diffY > DEFAULT_STEP_HEIGHT && diffY <= 1.2491870787) {
          List<IPacket> sendList = Lists.newArrayList();
          // if this is true, this must be a step
          // now to send additional packets to get around NCP
          double x = previousPositionPacket.getX(0.D);
          double y = previousPositionPacket.getY(0.D);
          double z = previousPositionPacket.getZ(0.D);
          sendList.add(new CPlayerPacket.PositionPacket(x, y + 0.4199999869D, z, true));
          sendList.add(new CPlayerPacket.PositionPacket(x, y + 0.7531999805D, z, true));
          sendList.add(
              new CPlayerPacket.PositionPacket(
                  packetPlayer.getX(0.f),
                  packetPlayer.getY(0.f),
                  packetPlayer.getZ(0.f),
                  packetPlayer.isOnGround()));
          for (IPacket toSend : sendList) {
            getNetworkManager().dispatchSilentNetworkPacket(toSend);
          }
          event.setCanceled(true);
        }
      }
      previousPositionPacket = (CPlayerPacket) event.getPacket();
    }
  }

  @Override
  public String getDebugDisplayText() {
    return String.format(
        "%s[%s%s]",
        super.getDisplayText(),
        stepHeight.getValue().toString(),
        unstep.getValue() ? "+unstep" : ""
    );
  }
}
