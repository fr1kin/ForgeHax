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
  public ItemStack getCurrentItem() {
    return mocking.getCurrentItem();
  }

  @Override
  public int getFirstEmptyStack() {
    return mocking.getFirstEmptyStack();
  }

  @Override
  public void setPickedItemStack(ItemStack p_184434_1_) {
    mocking.setPickedItemStack(p_184434_1_);
  }

  @Override
  public void pickItem(int p_184430_1_) {
    mocking.pickItem(p_184430_1_);
  }

  @Override
  public int getSlotFor(ItemStack p_184429_1_) {
    return mocking.getSlotFor(p_184429_1_);
  }

  @Override
  public int findSlotMatchingUnusedItem(ItemStack p_194014_1_) {
    return mocking.findSlotMatchingUnusedItem(p_194014_1_);
  }

  @Override
  public int getBestHotbarSlot() {
    return mocking.getBestHotbarSlot();
  }

  @Override
  public void changeCurrentItem(double p_195409_1_) {
    mocking.changeCurrentItem(p_195409_1_);
  }

  @Override
  public int func_234564_a_(Predicate<ItemStack> p_234564_1_, int p_234564_2_, IInventory p_234564_3_) {
    return mocking.func_234564_a_(p_234564_1_, p_234564_2_, p_234564_3_);
  }

  @Override
  public int storeItemStack(ItemStack p_70432_1_) {
    return mocking.storeItemStack(p_70432_1_);
  }

  @Override
  public void tick() {
    mocking.tick();
  }

  @Override
  public boolean addItemStackToInventory(ItemStack p_70441_1_) {
    return mocking.addItemStackToInventory(p_70441_1_);
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
  public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
    return mocking.decrStackSize(p_70298_1_, p_70298_2_);
  }

  @Override
  public void deleteStack(ItemStack p_184437_1_) {
    mocking.deleteStack(p_184437_1_);
  }

  @Override
  public ItemStack removeStackFromSlot(int p_70304_1_) {
    return mocking.removeStackFromSlot(p_70304_1_);
  }

  @Override
  public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
    mocking.setInventorySlotContents(p_70299_1_, p_70299_2_);
  }

  @Override
  public int getInventoryStackLimit() {
    return mocking.getInventoryStackLimit();
  }

  @Override
  public float getDestroySpeed(BlockState p_184438_1_) {
    return mocking.getDestroySpeed(p_184438_1_);
  }

  @Override
  public ListNBT write(ListNBT p_70442_1_) {
    return mocking.write(p_70442_1_);
  }

  @Override
  public void read(ListNBT p_70443_1_) {
    mocking.read(p_70443_1_);
  }

  @Override
  public int getSizeInventory() {
    return mocking.getSizeInventory();
  }

  @Override
  public boolean isEmpty() {
    return mocking.isEmpty();
  }

  @Override
  public ItemStack getStackInSlot(int p_70301_1_) {
    return mocking.getStackInSlot(p_70301_1_);
  }

  @Override
  public ITextComponent getName() {
    return mocking.getName();
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

  @Override
  public ItemStack armorItemInSlot(int p_70440_1_) {
    return mocking.armorItemInSlot(p_70440_1_);
  }

  @Override
  public void func_234563_a_(DamageSource p_234563_1_, float p_234563_2_) {
    mocking.func_234563_a_(p_234563_1_, p_234563_2_);
  }

  @Override
  public void dropAllItems() {
    mocking.dropAllItems();
  }

  @Override
  public void markDirty() {
    mocking.markDirty();
  }

  @Override
  public int getTimesChanged() {
    return mocking.getTimesChanged();
  }

  @Override
  public void setItemStack(ItemStack p_70437_1_) {
    mocking.setItemStack(p_70437_1_);
  }

  @Override
  public ItemStack getItemStack() {
    return mocking.getItemStack();
  }

  @Override
  public boolean isUsableByPlayer(PlayerEntity p_70300_1_) {
    return mocking.isUsableByPlayer(p_70300_1_);
  }

  @Override
  public void openInventory(PlayerEntity p_174889_1_) {
    mocking.openInventory(p_174889_1_);
  }

  @Override
  public void closeInventory(PlayerEntity p_174886_1_) {
    mocking.closeInventory(p_174886_1_);
  }

  @Override
  public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
    return mocking.isItemValidForSlot(p_94041_1_, p_94041_2_);
  }

  @Override
  public int count(Item p_213901_1_) {
    return mocking.count(p_213901_1_);
  }

  @Override
  public boolean hasAny(Set<Item> p_213902_1_) {
    return mocking.hasAny(p_213902_1_);
  }

  @Override
  public boolean hasItemStack(ItemStack p_70431_1_) {
    return mocking.hasItemStack(p_70431_1_);
  }

  @Override
  public boolean hasTag(ITag<Item> itemTag) {
    return mocking.hasTag(itemTag);
  }

  @Override
  public void copyInventory(PlayerInventory p_70455_1_) {
    mocking.copyInventory(p_70455_1_);
  }

  @Override
  public void clear() {
    mocking.clear();
  }

  @Override
  public void accountStacks(RecipeItemHelper p_201571_1_) {
    mocking.accountStacks(p_201571_1_);
  }
}
