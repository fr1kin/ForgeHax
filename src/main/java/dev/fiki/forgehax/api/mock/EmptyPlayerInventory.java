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

public class EmptyPlayerInventory extends PlayerInventory {
  public EmptyPlayerInventory() {
    super(null);
  }

  @Override
  public ItemStack getSelected() {
    return ItemStack.EMPTY;
  }

  @Override
  public int getFreeSlot() {
    return -1;
  }

  @Override
  public void setPickedItem(ItemStack p_184434_1_) {
  }

  @Override
  public void pickSlot(int p_184430_1_) {
  }

  @Override
  public int findSlotMatchingItem(ItemStack p_184429_1_) {
    return -1;
  }

  @Override
  public int findSlotMatchingUnusedItem(ItemStack p_194014_1_) {
    return -1;
  }

  @Override
  public int getSuitableHotbarSlot() {
    return -1;
  }

  @Override
  public void swapPaint(double p_195409_1_) {
  }

  @Override
  public int clearOrCountMatchingItems(Predicate<ItemStack> p_234564_1_, int p_234564_2_, IInventory p_234564_3_) {
    return -1;
  }

  @Override
  public int getSlotWithRemainingSpace(ItemStack p_70432_1_) {
    return -1;
  }

  @Override
  public void tick() {
  }

  @Override
  public boolean add(ItemStack p_70441_1_) {
    return false;
  }

  @Override
  public boolean add(int p_191971_1_, ItemStack p_191971_2_) {
    return false;
  }

  @Override
  public void placeItemBackInInventory(World p_191975_1_, ItemStack p_191975_2_) {
  }

  @Override
  public ItemStack removeItem(int p_70298_1_, int p_70298_2_) {
    return ItemStack.EMPTY;
  }

  @Override
  public void removeItem(ItemStack p_184437_1_) {
  }

  @Override
  public ItemStack removeItemNoUpdate(int p_70304_1_) {
    return ItemStack.EMPTY;
  }

  @Override
  public void setItem(int p_70299_1_, ItemStack p_70299_2_) {
  }

  @Override
  public float getDestroySpeed(BlockState p_184438_1_) {
    return 0.f;
  }

  @Override
  public ListNBT save(ListNBT p_70442_1_) {
    return new ListNBT();
  }

  @Override
  public void load(ListNBT p_70443_1_) {
  }

  @Override
  public int getContainerSize() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public ItemStack getItem(int p_70301_1_) {
    return ItemStack.EMPTY;
  }

  @Override
  public ITextComponent getName() {
    return ItemStack.EMPTY.getDisplayName();
  }

  @Override
  public ItemStack getArmor(int p_70440_1_) {
    return ItemStack.EMPTY;
  }

  @Override
  public void hurtArmor(DamageSource p_234563_1_, float p_234563_2_) {
  }

  @Override
  public void dropAll() {
  }

  @Override
  public void setChanged() {
  }

  @Override
  public int getTimesChanged() {
    return 0;
  }

  @Override
  public void setCarried(ItemStack p_70437_1_) {
  }

  @Override
  public ItemStack getCarried() {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(PlayerEntity p_70300_1_) {
    return false;
  }

  @Override
  public boolean contains(ItemStack p_70431_1_) {
    return false;
  }

  @Override
  public boolean contains(ITag<Item> p_199712_1_) {
    return false;
  }

  @Override
  public void replaceWith(PlayerInventory p_70455_1_) {
  }

  @Override
  public void clearContent() {
  }

  @Override
  public void fillStackedContents(RecipeItemHelper p_201571_1_) {
  }

  @Override
  public int getMaxStackSize() {
    return 0;
  }

  @Override
  public void startOpen(PlayerEntity p_174889_1_) {
  }

  @Override
  public void stopOpen(PlayerEntity p_174886_1_) {
  }

  @Override
  public boolean canPlaceItem(int p_94041_1_, ItemStack p_94041_2_) {
    return false;
  }

  @Override
  public int countItem(Item p_213901_1_) {
    return 0;
  }

  @Override
  public boolean hasAnyOf(Set<Item> p_213902_1_) {
    return false;
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  public ITextComponent getDisplayName() {
    return ItemStack.EMPTY.getDisplayName();
  }

  @Nullable
  @Override
  public ITextComponent getCustomName() {
    return ItemStack.EMPTY.getDisplayName();
  }
}
