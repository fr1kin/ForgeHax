package dev.fiki.forgehax.api.mock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

public class MockPlayerInventory extends PlayerInventory {
  private final PlayerInventory mocking;

  public MockPlayerInventory(PlayerEntity player, PlayerInventory mocking) {
    super(player);
    this.mocking = mocking;
  }

  public MockPlayerInventory(PlayerEntity player) {
    this(player, player.inventory);
  }

  @Override
  public ItemStack getSelected() {
    return mocking.getSelected();
  }

  @Override
  public int getFreeSlot() {
    return mocking.getFreeSlot();
  }

  @Override
  public void setPickedItem(ItemStack p_184434_1_) {
    mocking.setPickedItem(p_184434_1_);
  }

  @Override
  public void pickSlot(int p_184430_1_) {
    mocking.pickSlot(p_184430_1_);
  }

  @Override
  public int findSlotMatchingItem(ItemStack p_184429_1_) {
    return mocking.findSlotMatchingItem(p_184429_1_);
  }

  @Override
  public int findSlotMatchingUnusedItem(ItemStack p_194014_1_) {
    return mocking.findSlotMatchingUnusedItem(p_194014_1_);
  }

  @Override
  public int getSuitableHotbarSlot() {
    return mocking.getSuitableHotbarSlot();
  }

  @Override
  public void swapPaint(double p_195409_1_) {
    mocking.swapPaint(p_195409_1_);
  }

  @Override
  public int clearOrCountMatchingItems(Predicate<ItemStack> p_234564_1_, int p_234564_2_, IInventory p_234564_3_) {
    return mocking.clearOrCountMatchingItems(p_234564_1_, p_234564_2_, p_234564_3_);
  }

  @Override
  public int getSlotWithRemainingSpace(ItemStack p_70432_1_) {
    return mocking.getSlotWithRemainingSpace(p_70432_1_);
  }

  @Override
  public void tick() {
    mocking.tick();
  }

  @Override
  public boolean add(ItemStack p_70441_1_) {
    return mocking.add(p_70441_1_);
  }

  @Override
  public boolean add(int p_191971_1_, ItemStack p_191971_2_) {
    return mocking.add(p_191971_1_, p_191971_2_);
  }

  @Override
  public void placeItemBackInInventory(World p_191975_1_, ItemStack p_191975_2_) {
    mocking.placeItemBackInInventory(p_191975_1_, p_191975_2_);
  }

  @Override
  public ItemStack removeItem(int p_70298_1_, int p_70298_2_) {
    return mocking.removeItem(p_70298_1_, p_70298_2_);
  }

  @Override
  public void removeItem(ItemStack p_184437_1_) {
    mocking.removeItem(p_184437_1_);
  }

  @Override
  public ItemStack removeItemNoUpdate(int p_70304_1_) {
    return mocking.removeItemNoUpdate(p_70304_1_);
  }

  @Override
  public void setItem(int p_70299_1_, ItemStack p_70299_2_) {
    mocking.setItem(p_70299_1_, p_70299_2_);
  }

  @Override
  public float getDestroySpeed(BlockState p_184438_1_) {
    return mocking.getDestroySpeed(p_184438_1_);
  }

  @Override
  public ListNBT save(ListNBT p_70442_1_) {
    return mocking.save(p_70442_1_);
  }

  @Override
  public void load(ListNBT p_70443_1_) {
    mocking.load(p_70443_1_);
  }

  @Override
  public int getContainerSize() {
    return mocking.getContainerSize();
  }

  @Override
  public boolean isEmpty() {
    return mocking.isEmpty();
  }

  @Override
  public ItemStack getItem(int p_70301_1_) {
    return mocking.getItem(p_70301_1_);
  }

  @Override
  public ITextComponent getName() {
    return mocking.getName();
  }

  @Override
  public ItemStack getArmor(int p_70440_1_) {
    return mocking.getArmor(p_70440_1_);
  }

  @Override
  public void hurtArmor(DamageSource p_234563_1_, float p_234563_2_) {
    mocking.hurtArmor(p_234563_1_, p_234563_2_);
  }

  @Override
  public void dropAll() {
    mocking.dropAll();
  }

  @Override
  public void setChanged() {
    mocking.setChanged();
  }

  @Override
  public int getTimesChanged() {
    return mocking.getTimesChanged();
  }

  @Override
  public void setCarried(ItemStack p_70437_1_) {
    mocking.setCarried(p_70437_1_);
  }

  @Override
  public ItemStack getCarried() {
    return mocking.getCarried();
  }

  @Override
  public boolean stillValid(PlayerEntity p_70300_1_) {
    return mocking.stillValid(p_70300_1_);
  }

  @Override
  public boolean contains(ItemStack p_70431_1_) {
    return mocking.contains(p_70431_1_);
  }

  @Override
  public boolean contains(ITag<Item> p_199712_1_) {
    return mocking.contains(p_199712_1_);
  }

  @Override
  public void replaceWith(PlayerInventory p_70455_1_) {
    mocking.replaceWith(p_70455_1_);
  }

  @Override
  public void clearContent() {
    mocking.clearContent();
  }

  @Override
  public void fillStackedContents(RecipeItemHelper p_201571_1_) {
    mocking.fillStackedContents(p_201571_1_);
  }

  @Override
  public int getMaxStackSize() {
    return mocking.getMaxStackSize();
  }

  @Override
  public void startOpen(PlayerEntity p_174889_1_) {
    mocking.startOpen(p_174889_1_);
  }

  @Override
  public void stopOpen(PlayerEntity p_174886_1_) {
    mocking.stopOpen(p_174886_1_);
  }

  @Override
  public boolean canPlaceItem(int p_94041_1_, ItemStack p_94041_2_) {
    return mocking.canPlaceItem(p_94041_1_, p_94041_2_);
  }

  @Override
  public int countItem(Item p_213901_1_) {
    return mocking.countItem(p_213901_1_);
  }

  @Override
  public boolean hasAnyOf(Set<Item> p_213902_1_) {
    return mocking.hasAnyOf(p_213902_1_);
  }

  @Override
  public boolean hasCustomName() {
    return mocking.hasCustomName();
  }

  @Override
  public ITextComponent getDisplayName() {
    return mocking.getDisplayName();
  }

  @Nullable
  @Override
  public ITextComponent getCustomName() {
    return mocking.getCustomName();
  }
}
