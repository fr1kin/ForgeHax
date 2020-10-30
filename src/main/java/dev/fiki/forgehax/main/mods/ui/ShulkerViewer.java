package dev.fiki.forgehax.main.mods.ui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.services.ChatCommandService;
import dev.fiki.forgehax.main.util.Utils;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.key.KeyConflictContexts;
import dev.fiki.forgehax.main.util.key.KeyInputs;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "ShulkerViewer",
    description = "View the contents of a shulker box",
    category = Category.UI
)
public class ShulkerViewer extends ToggleMod {

  private static final ResourceLocation SHULKER_GUI_TEXTURE =
      new ResourceLocation("textures/gui/container/shulker_box.png");
  private static final int SHULKER_GUI_SIZE = 16 + 54 + 6;

  private static final int CACHE_HOVERING_INDEX = 0;
  private static final int CACHE_HOLDING_INDEX = 1;
  private static final int CACHE_RESERVE_SIZE = 2;

  private final BooleanSetting help_text = newBooleanSetting()
      .name("help-text")
      .description("Text to inform new users that the shulker contents can be viewed by holding a key down")
      .defaultTo(true)
      .build();

  private final BooleanSetting toggle_lock = newBooleanSetting()
      .name("toggle-lock")
      .description("GUI will remain locked in place until key is pressed again")
      .defaultTo(false)
      .build();

  private final IntegerSetting tooltip_opacity = newIntegerSetting()
      .name("tooltip-opacity")
      .description("Opacity of the Shulker GUI when displaying")
      .defaultTo(200)
      .min(0)
      .max(255)
      .build();

  private final IntegerSetting locked_opacity = newIntegerSetting()
      .name("locked-opacity")
      .description("Opacity of the Shulker GUI when locked in place")
      .defaultTo(255)
      .min(0)
      .max(255)
      .build();

  private final IntegerSetting x_offset = newIntegerSetting()
      .name("x-offset")
      .description("X Offset for the tool tip")
      .defaultTo(8)
      .build();

  private final IntegerSetting y_offset = newIntegerSetting()
      .name("y-offset")
      .description("Y Offset for the tool tip")
      .defaultTo(0)
      .build();

  private final KeyBindingSetting lockDownKey = newKeyBindingSetting()
      .name("hold-bind")
      .description("Bind for holding down the shulker view tooltip")
      .keyName("Hold")
      .defaultKeyCategory()
      .key(KeyInputs.KEY_LEFT_ALT)
      .keyPressedListener(this::onLockPressed)
      .keyReleasedListener(this::onLockReleased)
      .conflictContext(KeyConflictContexts.inContainerGui())
      .build();

  private final List<GuiShulkerViewer> guiCache =
      Lists.newArrayListWithExpectedSize(CACHE_RESERVE_SIZE);
  private final Lock cacheLock = new ReentrantLock();

  private boolean locked = false;
  private boolean updated = false;

  private boolean isKeySet = false;

  private boolean isMouseInShulkerGui = false;
  private boolean isModGeneratedToolTip = false;

  private int lastX = -1;
  private int lastY = -1;

  private boolean isLocked() {
    return locked && updated;
  }

  private boolean setInCache(int index, @Nullable GuiShulkerViewer viewer) {
    if (index < 0) {
      return false;
    } else if (viewer == null && index > (CACHE_RESERVE_SIZE - 1) && index == guiCache.size() - 1) {
      guiCache.remove(index); // remove non-reserved extras
      int previous = index - 1;
      if (previous > (CACHE_RESERVE_SIZE - 1)
          && !getInCache(previous)
          .isPresent()) // check if previous entry is null and remove it recursively if it is
      {
        return setInCache(previous, null);
      } else {
        return true;
      }
    } else if (index > guiCache.size() - 1) { // array not big enough
      for (int i = Math.max(guiCache.size(), 1); i < index; ++i) {
        guiCache.add(i, null); // fill with nulls up to the index
      }
      guiCache.add(index, viewer);
      return true;
    } else {
      guiCache.set(index, viewer);
      return true;
    }
  }

  private boolean appendInCache(@Nonnull GuiShulkerViewer viewer) {
    return setInCache(Math.max(guiCache.size() - 1, CACHE_RESERVE_SIZE), viewer);
  }

  private Optional<GuiShulkerViewer> getInCache(int index) {
    return Utils.isInRange(guiCache, index)
        ? Optional.ofNullable(guiCache.get(index))
        : Optional.empty();
  }

  private void clearCache() {
    for (int i = 0; i < CACHE_RESERVE_SIZE; ++i) {
      setInCache(
          i, null); // set all reserve slots to null, and add them if they don't already exist
    }
    while (guiCache.size() > CACHE_RESERVE_SIZE) {
      setInCache(guiCache.size() - 1, null); // clear the rest
    }
  }

  private void reset() {
    locked = updated = isKeySet = isMouseInShulkerGui = isModGeneratedToolTip = false;
    lastX = lastY = -1;
    clearCache();
  }

  private GuiShulkerViewer newShulkerGui(ItemStack parentShulker, int priority) {
    return new GuiShulkerViewer(
        new ShulkerContainer(new ShulkerInventory(Utils.getShulkerContents(parentShulker)), 27),
        parentShulker,
        priority);
  }

  private boolean isInRegion(int x, int y, int width, int height, int testingX, int testingY) {
    return testingX >= x && testingY >= y && testingX <= x + width && testingY <= y + height;
  }

  private boolean isItemShulkerBox(Item item) {
    return Optional.of(item)
        .filter(BlockItem.class::isInstance)
        .map(BlockItem.class::cast)
        .map(BlockItem::getBlock)
        .filter(ShulkerBoxBlock.class::isInstance)
        .isPresent();

  }

  @Override
  protected void onEnabled() {
    cacheLock.lock();
    try {
      reset();
    } finally {
      cacheLock.unlock();
    }
  }

  @Override
  protected void onDisabled() {
    onEnabled();
  }

  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText() + " " + String.format("[size = %d]", guiCache.size());
  }

  private void onLockPressed(KeyBinding binding) {
    if (toggle_lock.getValue()) {
      if (!isKeySet) {
        locked = !locked;
        if (!locked) {
          updated = false;
        }
        isKeySet = true;
      }
    } else {
      locked = true;
    }
  }

  private void onLockReleased(KeyBinding binding) {
    if (toggle_lock.getValue()) {
      isKeySet = false;
    } else {
      locked = updated = false;
    }
  }

  @SubscribeEvent
  public void onPreTooptipRender(RenderTooltipEvent.Pre event) {
    if (!(getDisplayScreen() instanceof ContainerScreen) || isModGeneratedToolTip) {
      return;
    }

    if (isMouseInShulkerGui) {
      // do not render tool tips that are inside the region of our shulker gui
      event.setCanceled(true);
    } else if (isItemShulkerBox(event.getStack().getItem())) {
      event.setCanceled(true); // do not draw normal tool tip
    }
  }

  @SubscribeEvent
  public void onGuiChanged(GuiOpenEvent event) {
    if (event.getGui() == null) {
      reset();
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRender(GuiScreenEvent.DrawScreenEvent.Post event) {
    if (!(getDisplayScreen() instanceof ContainerScreen)) {
      return;
    }

    cacheLock.lock();
    try {
      ContainerScreen gui = (ContainerScreen) event.getGui();

      if (!isLocked()) {
        // show stats for the item being hovered over
        Slot slotUnder = gui.getSlotUnderMouse();
        if (slotUnder == null
            || !slotUnder.getHasStack()
            || slotUnder.getStack().isEmpty()
            // TODO: 1.15 detect if item is a shulkerbox
            || !isItemShulkerBox(slotUnder.getStack().getItem())) {
          setInCache(CACHE_HOVERING_INDEX, null);
        } else if (!ItemStack.areItemStacksEqual(
            getInCache(0).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY),
            slotUnder.getStack())) {
          setInCache(CACHE_HOVERING_INDEX, newShulkerGui(slotUnder.getStack(), 1));
        }

        // show stats for held item
        ItemStack stackHeld = LocalPlayerInventory.getInventory().getItemStack();
        if (stackHeld.isEmpty() || !isItemShulkerBox(stackHeld.getItem())) {
          setInCache(CACHE_HOLDING_INDEX, null);
        } else if (!ItemStack.areItemStacksEqual(
            getInCache(1).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY),
            stackHeld)) {
          setInCache(CACHE_HOLDING_INDEX, newShulkerGui(stackHeld, 0));
        }

        if (locked && !updated && guiCache.stream().anyMatch(Objects::nonNull)) {
          updated = true;
        }
      }

      int offsetX, offsetY;
      if (!isLocked() || (lastX == -1 && lastY == -1)) {
        int count = (int) guiCache.stream().filter(Objects::nonNull).count();
        offsetX = lastX = event.getMouseX() + x_offset.getValue();
        offsetY = lastY = event.getMouseY() - (SHULKER_GUI_SIZE * count) / 2 + y_offset.getValue();
      } else {
        offsetX = lastX;
        offsetY = lastY;
      }

      isMouseInShulkerGui = false; // recheck

      for (GuiShulkerViewer ui : guiCache) {
        if (ui != null) {
          ui.posX = offsetX;
          ui.posY = offsetY;
          ui.render(event.getMatrixStack(), event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
          offsetY += SHULKER_GUI_SIZE + 1;
        }
      }
    } finally {
      cacheLock.unlock();
    }

    if (help_text.getValue()) {
      RenderSystem.disableLighting();
      RenderSystem.disableDepthTest();

      SurfaceHelper.drawTextShadow(
          "Hold "
              + lockDownKey.getKeyName()
              + " to view the tooltips of a Shulker boxes content!",
          5,
          getScreenHeight() - (int) (SurfaceHelper.getStringHeight() + 2) * 3 - 2,
          Colors.RED.toBuffer(),
          1);
      SurfaceHelper.drawTextShadow(
          "The activation key can be configured under Minecraft's Options -> Controls -> ForgeHax -> ShulkerViewer Lock.",
          5,
          getScreenHeight() - (int) (SurfaceHelper.getStringHeight() + 2) * 2 - 2,
          Colors.GREEN.toBuffer(),
          1);
      SurfaceHelper.drawTextShadow(
          "Type in chat \""
              + ChatCommandService.getActivationCharacter()
              + getName()
              + "\" for more options, and \""
              + ChatCommandService.getActivationCharacter()
              + getName()
              + " "
              + help_text.getName()
              + " false\" to disable this help message.",
          5,
          getScreenHeight() - (int) (SurfaceHelper.getStringHeight() + 2) - 2,
          Colors.YELLOW.toBuffer(),
          1);

      RenderSystem.enableDepthTest();
    }

    RenderSystem.enableLighting();
    RenderSystem.color4f(1.f, 1.f, 1.f, 1.0f);
  }

  class GuiShulkerViewer extends ContainerScreen<Container> implements Comparable<GuiShulkerViewer> {

    private final ItemStack parentShulker;
    private final int priority;

    public int posX = 0;
    public int posY = 0;

    public GuiShulkerViewer(Container inventorySlotsIn, ItemStack parentShulker, int priority) {
      super(inventorySlotsIn, new FakePlayerInventory(), new StringTextComponent("ShulkerViewer"));
      this.minecraft = MC;
      this.font = getFontRenderer();
      this.parentShulker = parentShulker;
      this.priority = priority;
      this.width = getScreenWidth();
      this.height = getScreenHeight();
      this.xSize = 176;
      this.ySize = SHULKER_GUI_SIZE;
    }

    public ItemStack getParentShulker() {
      return parentShulker;
    }

    public int getPosX() {
      return posX;
    }

    public int getPosY() {
      return posY;
    }

    public int getWidth() {
      return xSize;
    }

    public int getHeight() {
      return ySize;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
      final int DEPTH = 500;

      stack.push();
      stack.translate(posX, posY, 0);

      this.guiLeft = posX + 8;
      this.guiTop = posY - 1;

      RenderSystem.enableTexture();
      RenderSystem.disableLighting();

      RenderSystem.color4f(
          1.f,
          1.f,
          1.f,
          !isLocked()
              ? (tooltip_opacity.getValue() / 255.f)
              : (locked_opacity.getValue() / 255.f));

      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
          GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
          GlStateManager.SourceFactor.ONE,
          GlStateManager.DestFactor.ZERO);

      MC.getTextureManager().bindTexture(SHULKER_GUI_TEXTURE);

      RenderSystem.enableBlend();
      RenderSystem.enableTexture();
      RenderSystem.defaultBlendFunc();

      // width 176        = width of container
      // height 16        = top of the gui
      // height 54        = gui item boxes
      // height 6         = bottom of the gui
      GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 176, 16, DEPTH);
      GuiUtils.drawTexturedModalRect(0, 16, 0, 16, 176, 54, DEPTH);
      GuiUtils.drawTexturedModalRect(0, 16 + 54, 0, 160, 176, 6, DEPTH);

      RenderSystem.enableTexture();
      RenderSystem.disableDepthTest();
      RenderSystem.enableAlphaTest();

      RenderSystem.enableColorMaterial();

      stack.push();
      stack.translate(0, 0, DEPTH);

      SurfaceHelper.drawText(parentShulker.getDisplayName().getString(),
          8, 6, Colors.BLACK.toBuffer());

      stack.pop();

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
          new net.minecraftforge.client.event.GuiContainerEvent.DrawBackground(this, stack, mouseX, mouseY));

//      getFontRenderer().drawString(parentShulker.getDisplayName().getFormattedText(),
//          x + 8, y + 6, Colors.BLACK.toBuffer());

      RenderSystem.enableDepthTest();

      RenderHelper.enableStandardItemLighting();
      RenderSystem.enableRescaleNormal();
      RenderSystem.enableColorMaterial();
      RenderSystem.enableLighting();

      Slot hoveringOver = null;

      stack.push();
      stack.translate(8, -1, 0.f);

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
          new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, stack, mouseX, mouseY));

      for (Slot slot : container.inventorySlots) {
        if (slot.getHasStack()) {
          int px = slot.xPos;
          int py = slot.yPos;
          if (isPointInRegion(px, py, 16, 16, mouseX, mouseY)) {
            hoveringOver = slot;

            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);

            GuiUtils.drawGradientRect(stack.getLast().getMatrix(),
                DEPTH,
                hoveringOver.xPos,
                hoveringOver.yPos,
                hoveringOver.xPos + 16,
                hoveringOver.yPos + 16,
                Colors.WHITE.setAlpha(200).toBuffer(),
                Colors.WHITE.setAlpha(200).toBuffer());

            RenderSystem.enableLighting();
            RenderSystem.enableDepthTest();
          }

          SurfaceHelper.setItemRendererDepth(DEPTH + 1);
          SurfaceHelper.drawItem(slot.getStack(), px, py);
          SurfaceHelper.drawItemOverlay(slot.getStack(), px, py);
          SurfaceHelper.setItemRendererDepth(0.f);
        }
      }

      stack.pop();

      RenderSystem.disableLighting();

      if (hoveringOver != null) {
        // tool tip
        RenderSystem.enableAlphaTest();
        RenderSystem.color4f(1.f, 1.f, 1.f, 1.0f);

        stack.push();
        stack.translate(0, 0, DEPTH);

        isModGeneratedToolTip = true;
        try {
          ItemStack itemStack = hoveringOver.getStack();
          GuiUtils.preItemToolTip(itemStack);
          renderTooltip(stack, itemStack, mouseX + 8, mouseY + 8);
          GuiUtils.postItemToolTip();
        } finally {
          isModGeneratedToolTip = false;
        }

        stack.pop();
        RenderSystem.enableDepthTest();
      }

      if (isPointInRegion(this.posX, this.posY, getWidth(), getHeight(), mouseX, mouseY)) {
        isMouseInShulkerGui = true;
      }

      RenderSystem.disableBlend();
      RenderSystem.color4f(1.f, 1.f, 1.f, 1.0f);

      stack.pop();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
    }

    @Override
    public int compareTo(GuiShulkerViewer o) {
      return Integer.compare(priority, o.priority);
    }
  }

  static class ShulkerContainer extends Container {

    public ShulkerContainer(ShulkerInventory inventory, int size) {
      super(ContainerType.GENERIC_9X3, -1); // TODO: 1.15 should the id be -1?
      for (int i = 0; i < size; ++i) {
        int x = i % 9 * 18;
        int y = ((i / 9 + 1) * 18) + 1;
        addSlot(new Slot(inventory, i, x, y));
      }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
      return false;
    }
  }

  static class ShulkerInventory implements IInventory {

    private final List<ItemStack> contents;

    public ShulkerInventory(List<ItemStack> contents) {
      this.contents = contents;
    }

    @Override
    public int getSizeInventory() {
      return contents.size();
    }

    @Override
    public boolean isEmpty() {
      return contents.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
      return contents.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getInventoryStackLimit() {
      return 27;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
      return false;
    }

    @Override
    public void openInventory(PlayerEntity player) {
    }

    @Override
    public void closeInventory(PlayerEntity player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
      return index > 0 && index < contents.size() && contents.get(index).equals(stack);
    }

    @Override
    public void clear() {
    }
  }

  private static class FakePlayerInventory extends PlayerInventory {

    public FakePlayerInventory() {
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
  }
}
