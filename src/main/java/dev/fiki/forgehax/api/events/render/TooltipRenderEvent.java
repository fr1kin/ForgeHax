package dev.fiki.forgehax.api.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextProperties;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TooltipRenderEvent extends Event {
  private final ItemStack itemStack;
  private final List<? extends ITextProperties> lines;
  private final MatrixStack stack;
  private int x;
  private int y;
  private FontRenderer fontRenderer;

  @Cancelable
  public static class Pre extends TooltipRenderEvent {
    public Pre(ItemStack itemStack, List<? extends ITextProperties> lines, MatrixStack stack, int x, int y, FontRenderer fr) {
      super(itemStack, lines, stack, x, y, fr);
    }
  }

  public static class Post extends TooltipRenderEvent {
    public Post(ItemStack itemStack, List<? extends ITextProperties> lines, MatrixStack stack, int x, int y, FontRenderer fr) {
      super(itemStack, lines, stack, x, y, fr);
    }
  }
}
