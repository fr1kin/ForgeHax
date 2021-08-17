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
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.List;

public class MockPlayerContainer extends PlayerContainer {
  private final PlayerContainer mocking;

  public MockPlayerContainer(PlayerEntity player, PlayerContainer container) {
    super(player.inventory, true, player);
    this.mocking = container;
  }

  public MockPlayerContainer(PlayerEntity player) {
    this(player, player.inventoryMenu);
  }

  @Override
  public void fillCraftSlotsStackedContents(RecipeItemHelper p_201771_1_) {
    mocking.fillCraftSlotsStackedContents(p_201771_1_);
  }

  @Override
  public void clearCraftingContent() {
    mocking.clearCraftingContent();
  }

  @Override
  public boolean recipeMatches(IRecipe<? super CraftingInventory> p_201769_1_) {
    return mocking.recipeMatches(p_201769_1_);
  }

  @Override
  public void slotsChanged(IInventory p_75130_1_) {
    mocking.slotsChanged(p_75130_1_);
  }

  @Override
  public void removed(PlayerEntity p_75134_1_) {
    mocking.removed(p_75134_1_);
  }

  @Override
  public boolean stillValid(PlayerEntity p_75145_1_) {
    return mocking.stillValid(p_75145_1_);
  }

  @Override
  public ItemStack quickMoveStack(PlayerEntity p_82846_1_, int p_82846_2_) {
    return mocking.quickMoveStack(p_82846_1_, p_82846_2_);
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack p_94530_1_, Slot p_94530_2_) {
    return mocking.canTakeItemForPickAll(p_94530_1_, p_94530_2_);
  }

  @Override
  public int getResultSlotIndex() {
    return mocking.getResultSlotIndex();
  }

  @Override
  public int getGridWidth() {
    return mocking.getGridWidth();
  }

  @Override
  public int getGridHeight() {
    return mocking.getGridHeight();
  }

  @Override
  public int getSize() {
    return mocking.getSize();
  }

  @Override
  public CraftingInventory getCraftSlots() {
    return mocking.getCraftSlots();
  }

  @Override
  public RecipeBookCategory getRecipeBookType() {
    return mocking.getRecipeBookType();
  }

  @Override
  public void handlePlacement(boolean p_217056_1_, IRecipe<?> p_217056_2_, ServerPlayerEntity p_217056_3_) {
    mocking.handlePlacement(p_217056_1_, p_217056_2_, p_217056_3_);
  }

  @Override
  public List<RecipeBookCategories> getRecipeBookCategories() {
    return mocking.getRecipeBookCategories();
  }

  @Override
  public ContainerType<?> getType() {
    return mocking.getType();
  }

  @Override
  protected Slot addSlot(Slot p_75146_1_) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected IntReferenceHolder addDataSlot(IntReferenceHolder p_216958_1_) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void addDataSlots(IIntArray p_216961_1_) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSlotListener(IContainerListener p_75132_1_) {
    mocking.addSlotListener(p_75132_1_);
  }

  @Override
  public void removeSlotListener(IContainerListener p_82847_1_) {
    mocking.removeSlotListener(p_82847_1_);
  }

  @Override
  public NonNullList<ItemStack> getItems() {
    return mocking.getItems();
  }

  @Override
  public void broadcastChanges() {
    mocking.broadcastChanges();
  }

  @Override
  public boolean clickMenuButton(PlayerEntity p_75140_1_, int p_75140_2_) {
    return mocking.clickMenuButton(p_75140_1_, p_75140_2_);
  }

  @Override
  public Slot getSlot(int p_75139_1_) {
    return mocking.getSlot(p_75139_1_);
  }

  @Override
  public ItemStack clicked(int p_184996_1_, int p_184996_2_, ClickType p_184996_3_, PlayerEntity p_184996_4_) {
    return mocking.clicked(p_184996_1_, p_184996_2_, p_184996_3_, p_184996_4_);
  }

  @Override
  protected void clearContainer(PlayerEntity p_193327_1_, World p_193327_2_, IInventory p_193327_3_) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setItem(int p_75141_1_, ItemStack p_75141_2_) {
    mocking.setItem(p_75141_1_, p_75141_2_);
  }

  @Override
  public void setAll(List<ItemStack> p_190896_1_) {
    mocking.setAll(p_190896_1_);
  }

  @Override
  public void setData(int p_75137_1_, int p_75137_2_) {
    mocking.setData(p_75137_1_, p_75137_2_);
  }

  @Override
  public short backup(PlayerInventory p_75136_1_) {
    return mocking.backup(p_75136_1_);
  }

  @Override
  public boolean isSynched(PlayerEntity p_75129_1_) {
    return mocking.isSynched(p_75129_1_);
  }

  @Override
  public void setSynched(PlayerEntity p_75128_1_, boolean p_75128_2_) {
    mocking.setSynched(p_75128_1_, p_75128_2_);
  }

  @Override
  protected boolean moveItemStackTo(ItemStack p_75135_1_, int p_75135_2_, int p_75135_3_, boolean p_75135_4_) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void resetQuickCraft() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canDragTo(Slot p_94531_1_) {
    return mocking.canDragTo(p_94531_1_);
  }
}
