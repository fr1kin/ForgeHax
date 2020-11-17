package dev.fiki.forgehax.api.mock;

import dev.fiki.forgehax.api.reflection.ReflectionHelper;
import lombok.SneakyThrows;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.GameType;

import static dev.fiki.forgehax.main.Common.MC;


public class MockClientEntityPlayer extends RemoteClientPlayerEntity {
  private final ClientPlayerEntity mocking;
  private GameType gameType = null;

  public MockClientEntityPlayer(ClientPlayerEntity player) {
    super(player.worldClient, player.getGameProfile());
    this.mocking = player; //new GameProfile(UUID.randomUUID(), player.getGameProfile().getName())
    this.gameType = MC.getConnection().getPlayerInfo(player.getGameProfile().getId()).getGameType();
  }
  
  @SneakyThrows
  public void mockFields() {
    // copy all the fields that are not final
    ReflectionHelper.copyOf(mocking, this,
        AbstractClientPlayerEntity.class, Entity.class,
        ReflectionHelper::isFinal);
  }
  
  public void mockInventory() {
    // copy inventory from host into mocked player
    ListNBT nbt = new ListNBT();
    mocking.inventory.write(nbt);
    this.inventory.read(nbt);
    this.inventory.currentItem = mocking.inventory.currentItem;

    // copy inventory
    this.container.setAll(mocking.container.getInventory());
  }

  public void disableSwing() {
    limbSwing = 0.f;
    limbSwingAmount = 0.f;
  }
  
  public void disableInterpolation() {
    prevCameraYaw = cameraYaw;
    prevChasingPosX = chasingPosX;
    prevChasingPosY = chasingPosY;
    prevChasingPosZ = chasingPosZ;
    prevDistanceWalkedModified = distanceWalkedModified;
    prevLimbSwingAmount = limbSwingAmount;
    prevPosX = getPosX();
    prevPosY = getPosY();
    prevPosZ = getPosZ();
    prevRenderYawOffset = renderYawOffset;
    prevRotationPitch = rotationPitch;
    prevRotationYaw = rotationYaw;
    prevRotationYawHead = rotationYawHead;
    prevSwingProgress = swingProgress;
  }

  @Override
  public boolean isSpectator() {
    return GameType.SPECTATOR.equals(gameType);
  }

  @Override
  public boolean isCreative() {
    return GameType.CREATIVE.equals(gameType);
  }
}
