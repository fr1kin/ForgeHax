package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.api.entity.EntityUtils;
import dev.fiki.forgehax.api.entity.LocalPlayerInventory;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;
import static net.minecraft.util.math.RayTraceResult.Type;

/**
 * Created by Babbaj on 9/4/2017. TODO: check all 4 collision box corners
 */
@RegisterMod(
    name = "AutoBucket",
    description = "Automatically place bucket to reset fall damage",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AutoBucketFallMod extends ToggleMod {
  private final ReflectionTools reflection;

  public final DoubleSetting preHeight = newDoubleSetting()
      .name("PreHeight")
      .description("how far below to check before preparing")
      .defaultTo(10D)
      .build();
  public final DoubleSetting settingFallHeight = newDoubleSetting()
      .name("height")
      .description("minimum fall distance to work")
      .defaultTo(15D)
      .build();

  private final ItemStack WATER_BUCKET = new ItemStack(Items.WATER_BUCKET);

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (getLocalPlayer() == null
        || getLocalPlayer().fallDistance < settingFallHeight.getValue()
        || !getLocalPlayer().inventory.hasItemStack(WATER_BUCKET)
        || EntityUtils.isInWater(getLocalPlayer())
        || EntityUtils.isAboveWater(getLocalPlayer())) {
      return;
    }

    Vector3d playerPos = getLocalPlayer().getPositionVec();
    Vector3d rayTraceBucket = new Vector3d(playerPos.x, playerPos.y - 5, playerPos.z);
    Vector3d rayTracePre =
        new Vector3d(
            playerPos.x,
            playerPos.y - preHeight.getValue(),
            playerPos.z); // find the ground before the player is ready to water bucket

    RayTraceContext ctx = new RayTraceContext(playerPos, rayTraceBucket,
        RayTraceContext.BlockMode.COLLIDER,
        RayTraceContext.FluidMode.NONE,
        getLocalPlayer());

    BlockRayTraceResult result = getWorld().rayTraceBlocks(ctx);

    ctx = new RayTraceContext(playerPos, rayTracePre,
        RayTraceContext.BlockMode.COLLIDER,
        RayTraceContext.FluidMode.NONE,
        getLocalPlayer());

    BlockRayTraceResult resultPre = getWorld().rayTraceBlocks(ctx);

    if (Type.BLOCK.equals(resultPre.getType())
        && !getWorld().getBlockState(resultPre.getPos()).getMaterial().isLiquid()) {
      // set the pitch early to not get cucked by ncp
      getLocalPlayer().prevRotationPitch = 90f;
      getLocalPlayer().rotationPitch = 90f;

      int bucketSlot = findBucketHotbar();
      if (bucketSlot == -1) {
        bucketSlot = findBucketInv();
      }
      if (bucketSlot > 8) {
        swap(bucketSlot, getLocalPlayer().inventory.currentItem); // move bucket from inventory to hotbar
      } else {
        getLocalPlayer().inventory.currentItem = bucketSlot;
      }
    }

    if (Type.BLOCK.equals(result.getType())
        && !getWorld().getBlockState(result.getPos()).getMaterial().isLiquid()) {
      sendNetworkPacket(new CPlayerPacket.RotationPacket(
          getLocalPlayer().rotationYaw,
          90,
          reflection.Entity_onGround.get(getLocalPlayer())));

      // probably unnecessary but doing it anyways
      getLocalPlayer().prevRotationPitch = 90f;
      getLocalPlayer().rotationPitch = 90f;

      // printMessage("Attempted to place water bucket");
      getPlayerController().processRightClick(getLocalPlayer(), getWorld(), Hand.MAIN_HAND);
    }
  }

  private int findBucketInv() {
    return getLocalPlayer().inventory.getSlotFor(WATER_BUCKET); // find bucket in entire inventory
  }

  private int findBucketHotbar() {
    for (int i = 0; i < 9; i++) // iterate through hotbar slots
    {
      if (getLocalPlayer().inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
        return i;
      }
    }
    return -1;
  }

  private void swap(final int slot, final int hotbarNum) {
    getPlayerController().windowClick(LocalPlayerInventory.getContainer().windowId,
        slot, hotbarNum, ClickType.SWAP, getLocalPlayer());
  }
}
