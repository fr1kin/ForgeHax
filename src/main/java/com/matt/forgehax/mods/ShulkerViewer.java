package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.mods.services.ChatCommandService;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.key.Keys;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@RegisterMod
public class ShulkerViewer extends ToggleMod {
  private static final ResourceLocation SHULKER_GUI_TEXTURE =
      new ResourceLocation("textures/gui/container/shulker_box.png");
  private static final int SHULKER_GUI_SIZE = 16 + 54 + 6;

  private static final int CACHE_HOVERING_INDEX = 0;
  private static final int CACHE_HOLDING_INDEX = 1;
  private static final int CACHE_RESERVE_SIZE = 2;

  private final Setting<Boolean> help_text =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("help-text")
          .description(
              "Text to inform new users that the shulker contents can be viewed by holding a key down")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> toggle_lock =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("toggle-lock")
          .description("GUI will remain locked in place until key is pressed again")
          .defaultTo(false)
          .build();

  private final Setting<Integer> tooltip_opacity =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("tooltip-opacity")
          .description("Opacity of the Shulker GUI when displaying")
          .defaultTo(200)
          .min(0)
          .max(255)
          .build();

  private final Setting<Integer> locked_opacity =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("locked-opacity")
          .description("Opacity of the Shulker GUI when locked in place")
          .defaultTo(255)
          .min(0)
          .max(255)
          .build();

  private final Setting<Integer> x_offset =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("x-offset")
          .description("X Offset for the tool tip")
          .defaultTo(8)
          .build();

  private final Setting<Integer> y_offset =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("y-offset")
          .description("Y Offset for the tool tip")
          .defaultTo(0)
          .build();

  private final KeyBinding lockDownKey =
      new KeyBinding("ShulkerViewer Lock", GLFW.GLFW_KEY_LEFT_SHIFT, "ForgeHax");

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

  public ShulkerViewer() {
    super(Category.RENDER, "ShulkerViewer", false, "View the contents of a shulker box.");
    ClientRegistry.registerKeyBinding(lockDownKey);
    lockDownKey.setKeyConflictContext(
        new IKeyConflictContext() {
          @Override
          public boolean isActive() {
            return MC.field_71462_r instanceof ContainerScreen;
          }

          @Override
          public boolean conflicts(IKeyConflictContext other) {
            return false; // this will never conflict as
          }
        });
  }

  private boolean isLocked() {
    return locked && updated;
  }

  private boolean setInCache(int index, @Nullable GuiShulkerViewer viewer) {
    if (index < 0) return false;
    else if (viewer == null && index > (CACHE_RESERVE_SIZE - 1) && index == guiCache.size() - 1) {
      guiCache.remove(index); // remove non-reserved extras
      int previous = index - 1;
      if (previous > (CACHE_RESERVE_SIZE - 1)
          && !getInCache(previous)
              .isPresent()) // check if previous entry is null and remove it recursively if it is
      return setInCache(previous, null);
      else return true;
    } else if (index > guiCache.size() - 1) { // array not big enough
      for (int i = Math.max(guiCache.size(), 1); i < index; ++i)
        guiCache.add(i, null); // fill with nulls up to the index
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
    for (int i = 0; i < CACHE_RESERVE_SIZE; ++i)
      setInCache(
          i, null); // set all reserve slots to null, and add them if they don't already exist
    while (guiCache.size() > CACHE_RESERVE_SIZE)
      setInCache(guiCache.size() - 1, null); // clear the rest
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


  @SubscribeEvent
  public void onKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
    if (event.getKeyCode() == lockDownKey.getKey().getKeyCode()) {
        if (toggle_lock.get()) {
          if (!isKeySet) {
            locked = !locked;
            if (!locked) updated = false;
            isKeySet = true;
          }
        } else {
          locked = true;
        }

    }
  }

  @SubscribeEvent
  public void onKeyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent.Pre event) {
    if (event.getKeyCode() == lockDownKey.getKey().getKeyCode()) {
      if (toggle_lock.get()) {
        isKeySet = false;
      } else {
        locked = updated = false;
      }
    }
  }

  private boolean isShulkerBox(Item item) {
    return item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof ShulkerBoxBlock;
  }

  @SubscribeEvent
  public void onPreTooptipRender(RenderTooltipEvent.Pre event) {
    if (!(MC.field_71462_r instanceof ContainerScreen) || isModGeneratedToolTip) return;

    if (isMouseInShulkerGui) {
      // do not render tool tips that are inside the region of our shulker gui
      event.setCanceled(true);
    } else if (isShulkerBox(event.getStack().getItem()))
      event.setCanceled(true); // do not draw normal tool tip
  }

  @SubscribeEvent
  public void onGuiChanged(GuiOpenEvent event) {
    if (event.getGui() == null) reset();
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRender(GuiScreenEvent.DrawScreenEvent.Post event) {
    if (!(MC.field_71462_r instanceof ContainerScreen)) return;

    cacheLock.lock();
    try {
      ContainerScreen gui = (ContainerScreen)event.getGui();

      if (!isLocked()) {
        // show stats for the item being hovered over
        Slot slotUnder = gui.getSlotUnderMouse();
        if (slotUnder == null
            || !slotUnder.getHasStack()
            || slotUnder.getStack().isEmpty()
            || !(isShulkerBox(slotUnder.getStack().getItem())))
          setInCache(CACHE_HOVERING_INDEX, null);
        else if (!ItemStack.areItemStacksEqual(
            getInCache(0).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY),
            slotUnder.getStack()))
          setInCache(CACHE_HOVERING_INDEX, newShulkerGui(slotUnder.getStack(), 1));

        // show stats for held item
        ItemStack stackHeld = LocalPlayerInventory.getInventory().getItemStack();
        if (stackHeld.isEmpty() || !(isShulkerBox(stackHeld.getItem())))
          setInCache(CACHE_HOLDING_INDEX, null);
        else if (!ItemStack.areItemStacksEqual(
            getInCache(1).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY),
            stackHeld)) setInCache(CACHE_HOLDING_INDEX, newShulkerGui(stackHeld, 0));

        if (locked && !updated && guiCache.stream().anyMatch(Objects::nonNull)) updated = true;
      }

      AtomicInteger renderX;
      AtomicInteger renderY;
      if (!isLocked() || (lastX == -1 && lastY == -1)) {
        int count = (int) guiCache.stream().filter(Objects::nonNull).count();
        renderX = new AtomicInteger(lastX = event.getMouseX() + x_offset.get());
        renderY =
            new AtomicInteger(
                lastY = event.getMouseY() - (SHULKER_GUI_SIZE * count) / 2 + y_offset.get());
      } else {
        renderX = new AtomicInteger(lastX);
        renderY = new AtomicInteger(lastY);
      }

      isMouseInShulkerGui = false; // recheck

      guiCache
          .stream()
          .filter(Objects::nonNull)
          .sorted()
          .forEach(
              ui -> {
                ui.posX = renderX.get();
                ui.posY = renderY.get();
                ui.render(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
                renderY.set(renderY.get() + SHULKER_GUI_SIZE + 1);
              });
    } finally {
      cacheLock.unlock();
    }

    if (help_text.get()) {
      MainWindow window = MC.mainWindow;
      GlStateManager.disableLighting();
      GlStateManager.disableDepthTest();
      SurfaceHelper.drawTextShadow(
          "Hold "
              + Keys.getKeyName(lockDownKey.getKey().getKeyCode())
              + " to view the tooltips of a Shulker boxes content!",
          5,
          window.getScaledHeight() - (int) (SurfaceHelper.getStringHeight(null) + 2) * 3 - 2,
          Colors.RED.toBuffer(),
          1);
      SurfaceHelper.drawTextShadow(
          "The activation key can be configured under Minecraft's Options -> Controls -> ForgeHax -> ShulkerViewer Lock.",
          5,
          window.getScaledHeight() - (int) (SurfaceHelper.getStringHeight(null) + 2) * 2 - 2,
          Colors.GREEN.toBuffer(),
          1);
      SurfaceHelper.drawTextShadow(
          "Type in chat \""
              + ChatCommandService.getActivationCharacter()
              + getModName()
              + "\" for more options, and \""
              + ChatCommandService.getActivationCharacter()
              + getModName()
              + " "
              + help_text.getName()
              + " false\" to disable this help message.",
          5,
          window.getScaledHeight() - (int) (SurfaceHelper.getStringHeight(null) + 2) - 2,
          Colors.YELLOW.toBuffer(),
          1);
      GlStateManager.enableDepthTest();
    }

    GlStateManager.enableLighting();
    GlStateManager.color4f(1.f, 1.f, 1.f, 1.0f);
  }

  class GuiShulkerViewer extends ContainerScreen<ShulkerContainer> implements Comparable<GuiShulkerViewer> {
    private final ItemStack parentShulker;
    private final int priority;

    public int posX = 0;
    public int posY = 0;

    public GuiShulkerViewer(ShulkerContainer inventorySlotsIn, ItemStack parentShulker, int priority) {
      super(inventorySlotsIn, MC.player.inventory, new StringTextComponent("GuiShulkerViewer"));
      this.parentShulker = parentShulker;
      this.priority = priority;
      this.minecraft = MC;
      this.font = MC.fontRenderer;
      this.width = MC.mainWindow.getWidth();
      this.height = MC.mainWindow.getHeight();
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
    public void render(int mouseX, int mouseY, float partialTicks) {
      final int DEPTH = 500;

      int x = posX;
      int y = posY;

      GlStateManager.enableTexture();
      GlStateManager.disableLighting();
      GlStateManager.color4f(
          1.f,
          1.f,
          1.f,
          !isLocked()
              ? (tooltip_opacity.getAsFloat() / 255.f)
              : (locked_opacity.getAsFloat() / 255.f));

      GlStateManager.enableBlend();
      GlStateManager.blendFuncSeparate(
          GlStateManager.SourceFactor.SRC_ALPHA,
          GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
          GlStateManager.SourceFactor.ONE,
          GlStateManager.DestFactor.ZERO);

      MC.getTextureManager().bindTexture(SHULKER_GUI_TEXTURE);

      // width 176        = width of container
      // height 16        = top of the gui
      // height 54        = gui item boxes
      // height 6         = bottom of the gui
      SurfaceHelper.drawTexturedRect(x, y, 0, 0, 176, 16, DEPTH);
      SurfaceHelper.drawTexturedRect(x, y + 16, 0, 16, 176, 54, DEPTH);
      SurfaceHelper.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 6, DEPTH);

      GlStateManager.disableDepthTest();
      SurfaceHelper.drawText(parentShulker.getDisplayName().getString(), x + 8, y + 6, Colors.BLACK.toBuffer());
      GlStateManager.enableDepthTest();

      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableColorMaterial();
      GlStateManager.enableLighting();

      Slot hoveringOver = null;

      int rx = x + 8;
      int ry = y - 1;

      for (Slot slot : field_147002_h.inventorySlots) {
        if (slot.getHasStack()) {
          int px = rx + slot.xPos;
          int py = ry + slot.yPos;
          MC.getItemRenderer().zLevel = DEPTH + 1;
          SurfaceHelper.drawItem(slot.getStack(), px, py);
          SurfaceHelper.drawItemOverlay(slot.getStack(), px, py);
          MC.getItemRenderer().zLevel = 0.f;
          if (isPointInRegion(px, py, 16, 16, mouseX, mouseY)) hoveringOver = slot;
        }
      }

      GlStateManager.disableLighting();

      if (hoveringOver != null) {
        // background of the gui
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        GlStateManager.colorMask(true, true, true, false);
        this.fillGradient(
            rx + hoveringOver.xPos,
            ry + hoveringOver.yPos,
            rx + hoveringOver.xPos + 16,
            ry + hoveringOver.yPos + 16,
            -2130706433,
            -2130706433);
        GlStateManager.colorMask(true, true, true, true);

        // tool tip
        GlStateManager.color4f(1.f, 1.f, 1.f, 1.0f);
        GlStateManager.pushMatrix();
        isModGeneratedToolTip = true;
        renderTooltip(hoveringOver.getStack(), mouseX + 8, mouseY + 8);
        isModGeneratedToolTip = false;
        GlStateManager.popMatrix();
        GlStateManager.enableDepthTest();
      }

      if (isPointInRegion(this.posX, this.posY, getWidth(), getHeight(), mouseX, mouseY))
        isMouseInShulkerGui = true;

      GlStateManager.disableBlend();
      GlStateManager.color4f(1.f, 1.f, 1.f, 1.0f);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {}

    @Override
    public int compareTo(GuiShulkerViewer o) {
      return Integer.compare(priority, o.priority);
    }
  }

  static class ShulkerContainer extends Container {
    ShulkerContainer(@Nullable ContainerType<?> type, int id) {
      super(type, id);
    }


    public ShulkerContainer(ShulkerInventory inventory, int size) {
      super(ContainerType.field_221526_t, 666);
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
}
