package dev.fiki.forgehax.main.mods;

import static dev.fiki.forgehax.main.util.entity.EntityUtils.isAboveWater;
import static net.minecraft.util.math.RayTraceResult.Type;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Babbaj on 9/4/2017. TODO: check all 4 collision box corners
 */
@RegisterMod
public class AutoBucketFallMod extends ToggleMod {

  public AutoBucketFallMod() {
    super(Category.PLAYER, "AutoBucket", false, "Automatically place bucket to reset fall damage");
  }

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

  private ItemStack WATER_BUCKET = new ItemStack(Items.WATER_BUCKET);

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (Common.getLocalPlayer() == null
        || Common.getLocalPlayer().fallDistance < settingFallHeight.getValue()
        || !Common.getLocalPlayer().inventory.hasItemStack(WATER_BUCKET)
        || EntityUtils.isInWater(Common.getLocalPlayer())
        || EntityUtils.isAboveWater(Common.getLocalPlayer())) {
      return;
    }

    Vec3d playerPos = Common.getLocalPlayer().getPositionVector();
    Vec3d rayTraceBucket = new Vec3d(playerPos.x, playerPos.y - 5, playerPos.z);
    Vec3d rayTracePre =
        new Vec3d(
            playerPos.x,
            playerPos.y - preHeight.getValue(),
            playerPos.z); // find the ground before the player is ready to water bucket

    RayTraceContext ctx = new RayTraceContext(playerPos, rayTraceBucket,
        RayTraceContext.BlockMode.COLLIDER,
        RayTraceContext.FluidMode.NONE,
        Common.getLocalPlayer());

    BlockRayTraceResult result = Common.getWorld().rayTraceBlocks(ctx);

    ctx = new RayTraceContext(playerPos, rayTracePre,
        RayTraceContext.BlockMode.COLLIDER,
        RayTraceContext.FluidMode.NONE,
        Common.getLocalPlayer());

    BlockRayTraceResult resultPre = Common.getWorld().rayTraceBlocks(ctx);

    if (Type.BLOCK.equals(resultPre.getType())
        && !Common.getWorld().getBlockState(resultPre.getPos()).getMaterial().isLiquid()) {
      // set the pitch early to not get cucked by ncp
      Common.getLocalPlayer().prevRotationPitch = 90f;
      Common.getLocalPlayer().rotationPitch = 90f;

      int bucketSlot = findBucketHotbar();
      if (bucketSlot == -1) {
        bucketSlot = findBucketInv();
      }
      if (bucketSlot > 8) {
        swap(bucketSlot, Common.getLocalPlayer().inventory.currentItem); // move bucket from inventory to hotbar
      } else {
        Common.getLocalPlayer().inventory.currentItem = bucketSlot;
      }
    }

    if (Type.BLOCK.equals(result.getType())
        && !Common.getWorld().getBlockState(result.getPos()).getMaterial().isLiquid()) {
      Common.sendNetworkPacket(new CPlayerPacket.RotationPacket(
          Common.getLocalPlayer().rotationYaw,
          90,
          Common.getLocalPlayer().onGround));

      // probably unnecessary but doing it anyways
      Common.getLocalPlayer().prevRotationPitch = 90f;
      Common.getLocalPlayer().rotationPitch = 90f;

      // printMessage("Attempted to place water bucket");
      Common.getPlayerController().processRightClick(Common.getLocalPlayer(), Common.getWorld(), Hand.MAIN_HAND);
    }
  }

  private int findBucketInv() {
    return Common.getLocalPlayer().inventory.getSlotFor(WATER_BUCKET); // find bucket in entire inventory
  }

  private int findBucketHotbar() {
    for (int i = 0; i < 9; i++) // iterate through hotbar slots
    {
      if (Common.getLocalPlayer().inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
        return i;
      }
    }
    return -1;
  }

  private void swap(final int slot, final int hotbarNum) {
    Common.getPlayerController().windowClick(LocalPlayerInventory.getContainer().windowId,
        slot, hotbarNum, ClickType.SWAP, Common.getLocalPlayer());
  }
}
