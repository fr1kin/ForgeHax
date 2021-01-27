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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

public class EmptyPlayerInventory extends PlayerInventory {
  public EmptyPlayerInventory() {
    super(null);
  }

  @Override
  public ItemStack getCurrentItem() {
    return ItemStack.EMPTY;
  }

  @Override
  public int getFirstEmptyStack() {
    return 0;
  }

  @Override
  public void setPickedItemStack(ItemStack stack) {
  }

  @Override
  public void pickItem(int index) {
  }

  @Override
  public int getSlotFor(ItemStack stack) {
    return 0;
  }

  @Override
  public int findSlotMatchingUnusedItem(ItemStack p_194014_1_) {
    return 0;
  }

  @Override
  public int getBestHotbarSlot() {
    return 0;
  }

  @Override
  public void changeCurrentItem(double direction) {
  }

  @Override
  public int storeItemStack(ItemStack itemStackIn) {
    return 0;
  }

  @Override
  public void tick() {
  }

  @Override
  public boolean addItemStackToInventory(ItemStack itemStackIn) {
    return false;
  }

  @Override
  public boolean add(int slotIn, ItemStack stack) {
    return false;
  }

  @Override
  public void placeItemBackInInventory(World worldIn, ItemStack stack) {
  }

  @Override
  public ItemStack decrStackSize(int index, int count) {
    return ItemStack.EMPTY;
  }

  @Override
  public void deleteStack(ItemStack stack) {
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
  }

  @Override
  public float getDestroySpeed(BlockState state) {
    return 1.f;
  }

  @Override
  public ListNBT write(ListNBT nbtTagListIn) {
    return nbtTagListIn;
  }

  @Override
  public void read(ListNBT nbtTagListIn) {
  }

  @Override
  public int getSizeInventory() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public ItemStack getStackInSlot(int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public ITextComponent getName() {
    return new StringTextComponent("FakeInventory");
  }

  @Override
  public ItemStack armorItemInSlot(int slotIn) {
    return ItemStack.EMPTY;
  }

  @Override
  public void dropAllItems() {
  }

  @Override
  public void markDirty() {
  }

  @Override
  public int getTimesChanged() {
    return 0;
  }

  @Override
  public void setItemStack(ItemStack itemStackIn) {
  }

  @Override
  public ItemStack getItemStack() {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean isUsableByPlayer(PlayerEntity player) {
    return false;
  }

  @Override
  public boolean hasItemStack(ItemStack itemStackIn) {
    return false;
  }

  @Override
  public void copyInventory(PlayerInventory playerInventory) {
  }

  @Override
  public void clear() {
  }

  @Override
  public void accountStacks(RecipeItemHelper p_201571_1_) {
  }

  @Override
  public int getInventoryStackLimit() {
    return 0;
  }

  @Override
  public void openInventory(PlayerEntity player) {
  }

  @Override
  public void closeInventory(PlayerEntity player) {
  }

  @Override
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    return false;
  }

  @Override
  public int count(Item itemIn) {
    return 0;
  }

  @Override
  public boolean hasAny(Set<Item> set) {
    return false;
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  public ITextComponent getDisplayName() {
    return new StringTextComponent("FakeInventory");
  }

  @Nullable
  @Override
  public ITextComponent getCustomName() {
    return new StringTextComponent("FakeInventory");
  }

  @Override
  public int func_234564_a_(Predicate<ItemStack> p_234564_1_, int p_234564_2_, IInventory p_234564_3_) {
    return 0;
  }

  @Override
  public void func_234563_a_(DamageSource p_234563_1_, float p_234563_2_) {
  }

  @Override
  public boolean hasTag(ITag<Item> itemTag) {
    return false;
  }
}
