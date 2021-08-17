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
    super(player.clientLevel, player.getGameProfile());
    this.mocking = player; //new GameProfile(UUID.randomUUID(), player.getGameProfile().getName())
    this.gameType = MC.getConnection().getPlayerInfo(player.getGameProfile().getId()).getGameMode();
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
    mocking.inventory.save(nbt);
    this.inventory.load(nbt);
    this.inventory.selected = mocking.inventory.selected;

    // copy inventory
    this.inventoryMenu.setAll(mocking.inventoryMenu.getItems());
  }

  public void disableSwing() {
//    limbSwing = 0.f;
//    limbSwingAmount = 0.f;
    swingTime = 0;
  }
  
  public void disableInterpolation() {
    xOld = getX();
    yOld = getY();
    zOld = getZ();
    xo = getX();
    yOld = getY();
    zOld = getZ();
    oBob = bob;
    xCloakO = xCloak;
    yCloakO = yCloak;
    zCloakO = zCloak;
    walkDistO = walkDist;
    xRotO = xRot;
    yRotO = yRot;
    yBodyRotO = yBodyRot;
    yHeadRotO = yHeadRot;
    animStepO = animStep;
    oRun = run;
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
