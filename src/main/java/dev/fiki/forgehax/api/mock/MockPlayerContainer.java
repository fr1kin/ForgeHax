package dev.fiki.forgehax.api.mock;

import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.NonNullList;

import java.util.List;

public class MockPlayerContainer extends PlayerContainer {
  private final PlayerContainer mocking;

  public MockPlayerContainer(PlayerEntity player, PlayerContainer container) {
    super(player.inventory, true, player);
    this.mocking = container;
  }

  public MockPlayerContainer(PlayerEntity player) {
    this(player, player.container);
  }

  @Override
  public void func_217056_a(boolean p_217056_1_, IRecipe<?> p_217056_2_, ServerPlayerEntity p_217056_3_) {
    mocking.func_217056_a(p_217056_1_, p_217056_2_, p_217056_3_);
  }

  @Override
  public void fillStackedContents(RecipeItemHelper p_201771_1_) {
    mocking.fillStackedContents(p_201771_1_);
  }

  @Override
  public void clear() {
    mocking.clear();
  }

  @Override
  public boolean matches(IRecipe<? super CraftingInventory> p_201769_1_) {
    return mocking.matches(p_201769_1_);
  }

  @Override
  public void onCraftMatrixChanged(IInventory p_75130_1_) {
    mocking.onCraftMatrixChanged(p_75130_1_);
  }

  @Override
  public void putStackInSlot(int p_75141_1_, ItemStack p_75141_2_) {
    mocking.putStackInSlot(p_75141_1_, p_75141_2_);
  }

  @Override
  public void setAll(List<ItemStack> p_190896_1_) {
    mocking.setAll(p_190896_1_);
  }

  @Override
  public void updateProgressBar(int p_75137_1_, int p_75137_2_) {
    mocking.updateProgressBar(p_75137_1_, p_75137_2_);
  }

  @Override
  public short getNextTransactionID(PlayerInventory p_75136_1_) {
    return mocking.getNextTransactionID(p_75136_1_);
  }

  @Override
  public boolean getCanCraft(PlayerEntity p_75129_1_) {
    return mocking.getCanCraft(p_75129_1_);
  }

  @Override
  public void setCanCraft(PlayerEntity p_75128_1_, boolean p_75128_2_) {
    mocking.setCanCraft(p_75128_1_, p_75128_2_);
  }

  @Override
  public void onContainerClosed(PlayerEntity p_75134_1_) {
    mocking.onContainerClosed(p_75134_1_);
  }

  @Override
  public boolean canInteractWith(PlayerEntity p_75145_1_) {
    return mocking.canInteractWith(p_75145_1_);
  }

  @Override
  public boolean canDragIntoSlot(Slot p_94531_1_) {
    return mocking.canDragIntoSlot(p_94531_1_);
  }

  @Override
  public ContainerType<?> getType() {
    return mocking.getType();
  }

  @Override
  public void addListener(IContainerListener p_75132_1_) {
    mocking.addListener(p_75132_1_);
  }

  @Override
  public void removeListener(IContainerListener p_82847_1_) {
    mocking.removeListener(p_82847_1_);
  }

  @Override
  public NonNullList<ItemStack> getInventory() {
    return mocking.getInventory();
  }

  @Override
  public void detectAndSendChanges() {
    mocking.detectAndSendChanges();
  }

  @Override
  public boolean enchantItem(PlayerEntity p_75140_1_, int p_75140_2_) {
    return mocking.enchantItem(p_75140_1_, p_75140_2_);
  }

  @Override
  public Slot getSlot(int p_75139_1_) {
    return mocking.getSlot(p_75139_1_);
  }

  @Override
  public ItemStack transferStackInSlot(PlayerEntity p_82846_1_, int p_82846_2_) {
    return mocking.transferStackInSlot(p_82846_1_, p_82846_2_);
  }

  @Override
  public ItemStack slotClick(int p_184996_1_, int p_184996_2_, ClickType p_184996_3_, PlayerEntity p_184996_4_) {
    return mocking.slotClick(p_184996_1_, p_184996_2_, p_184996_3_, p_184996_4_);
  }

  @Override
  public boolean canMergeSlot(ItemStack p_94530_1_, Slot p_94530_2_) {
    return mocking.canMergeSlot(p_94530_1_, p_94530_2_);
  }

  @Override
  public int getOutputSlot() {
    return mocking.getOutputSlot();
  }

  @Override
  public int getWidth() {
    return mocking.getWidth();
  }

  @Override
  public int getHeight() {
    return mocking.getHeight();
  }

  @Override
  public int getSize() {
    return mocking.getSize();
  }

  @Override
  public List<RecipeBookCategories> getRecipeBookCategories() {
    return mocking.getRecipeBookCategories();
  }
}
