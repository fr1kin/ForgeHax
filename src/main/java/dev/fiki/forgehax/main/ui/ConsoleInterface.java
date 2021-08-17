package dev.fiki.forgehax.main.ui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.cmd.execution.IConsole;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.RenderTypeEx;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.main.Common;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.ListIterator;

import static dev.fiki.forgehax.main.Common.*;

@Getter
@Setter
@ExtensionMethod({VectorEx.class, VertexBuilderEx.class})
public class ConsoleInterface implements IGuiEventListener, IConsole {
  private final List<ConsoleEntry> entries = Lists.newCopyOnWriteArrayList();
  private final List<String> entered = Lists.newArrayList();

  private ConsoleInputScreen consoleScreen = null;

  private float scale = 1.f;

  private int maxSize = 100;
  private int maxLines = 10;
  private int fadeOutDuration = 20;
  private int messageDuration = 100;

  @Setter(AccessLevel.NONE)
  private int currentLineCount = 0;
  @Setter(AccessLevel.NONE)
  private int scrollOffset = 0;

  void addEntered(String text) {
    if (entered.isEmpty() || !entered.get(0).equals(text)) {
      entered.add(0, text);
    }
  }

  String getEnteredByRollingIndex(int index) {
    return entered.isEmpty() ? null : entered.get(Math.floorMod(index, entered.size()));
  }

  boolean hasEnteredHistory() {
    return !entered.isEmpty();
  }

  public void addMessage(String string) {
    ConsoleEntry entry = new ConsoleEntry(this, string);
    entries.add(entry);
    currentLineCount += entry.getLineCount();

    if (entries.size() > maxSize) {
      ConsoleEntry top = entries.remove(0);
      currentLineCount -= top.getLineCount();
    }
  }

  public void onRescale(int screenWidth, int screenHeight) {
    int lines = 0;
    for (ConsoleEntry entry : entries) {
      entry.updateMessages();
      lines += entry.getLineCount();
    }
    currentLineCount = lines;
  }

  public void scroll(int offset) {
    this.scrollOffset += offset;
  }

  public int getScrollOffset() {
    return 0; // TODO:
  }

  public void onTick() {
    for (ConsoleEntry entry : entries) {
      entry.tick();
    }
  }

  public void onRender() {
    float screenWidth = getScreenWidth();
    float screenHeight = getScreenHeight();

    final boolean consoleOpen = isConsoleOpen();

    val buffers = getBufferProvider();
    val source = buffers.getBufferSource();
    val main = buffers.getBuffer(RenderTypeEx.glTriangle());
    val stack = new MatrixStack();

    ListIterator<ConsoleEntry> it = entries.listIterator(entries.size());

    final float lineHeight = getLineHeight();
    final int maxLines = getLineCount();
    stack.translate(0.f, maxLines * lineHeight, 0.f);

    int linesConsumed = 0;
    while (it.hasPrevious() && linesConsumed < maxLines) {
      ConsoleEntry entry = it.previous();
      int index = it.previousIndex() + 1;

      if (!consoleOpen && entry.getTicksExisted() >= getMessageDuration() + getFadeOutDuration()) {
        // this message and every message before it have faded out
        // stop rendering here
        break;
      }

      Color bgColor = (index & 0x1) == 0 ? Colors.BLACK : Colors.DARK_GRAY;

      int entryLinesConsuming = Math.min(entry.getLineCount(), (maxLines - linesConsumed));
      for (int i = 0; i < entryLinesConsuming; ++i, ++linesConsumed) {
        stack.pushPose();
        stack.translate(0.f, linesConsumed * -lineHeight, 0.f);

        String message = entry.getMessages().get(entryLinesConsuming - 1 - i);

        main.rect(GL11.GL_TRIANGLES, 0, 0, getLineWidth(), lineHeight,
            bgColor.setAlpha(entry.getAlphaDecay(175)), stack.getLastMatrix());

        stack.translate(getPadding(), getPadding(), 50.f);

        SurfaceHelper.renderString(source, stack.last().pose(),
            message, 0, 0, Colors.WHITE.setAlpha(entry.getAlphaDecay(255)), true);

        stack.popPose();
      }
    }

    float emptyVerticalSpace = (maxLines - linesConsumed + 1) * lineHeight;

    RenderSystem.pushMatrix();
    RenderSystem.translatef(getMargin(), getMargin() - emptyVerticalSpace, 50.f);
    buffers.getBufferSource().endBatch();
    RenderSystem.popMatrix();
  }

  public void onKeyPressed(KeyBinding binding) {
    if (isConsoleOpen()) {
      return; // already exists
    }

    ConsoleInputScreen con = new ConsoleInputScreen(this, binding);
    con.setText(consoleScreen == null ? "" : consoleScreen.getText());
    con.setPreviousScreen(getDisplayScreen());
    setDisplayScreen(consoleScreen = con);
  }

  public boolean isConsoleOpen() {
    return consoleScreen != null && getDisplayScreen() == consoleScreen;
  }

  float getMargin() {
    return 5.f;
  }

  float getPadding() {
    return 1.f;
  }

  float getLineWidth() {
    return getScreenWidth() - getMargin() * 2.f;
  }

  float getLineHeight() {
    return (float) (SurfaceHelper.getStringHeight() + 2.f);
  }

  float getTextLength(String text) {
    return (float) SurfaceHelper.getStringWidth(text);
  }

  int getLineCount() {
    return isConsoleOpen() ? getMaxLines() * 2 : getMaxLines();
  }

  float getTotalHeight() {
    return getLineCount() * getLineHeight();
  }

  float getCurrentTotalHeight() {
    return Math.min(getCurrentLineCount(), getLineCount()) * getLineHeight();
  }

  FontRenderer getFontRenderer() {
    return Common.getFontRenderer();
  }

  @Override
  public void inform(String message, Object... args) {
    String fmt = String.format(message, args);
    addMessage(fmt);

//    Common.printInform(fmt);
  }

  @Override
  public void warn(String message, Object... args) {
    String fmt = String.format(message, args);
    addMessage(fmt);

//    Common.printWarning(fmt);
  }

  @Override
  public void error(String message, Object... args) {
    String fmt = String.format(message, args);
    addMessage(fmt);

//    Common.printError(fmt);
  }
}
