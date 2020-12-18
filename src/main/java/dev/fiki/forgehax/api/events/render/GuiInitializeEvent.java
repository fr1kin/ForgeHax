package dev.fiki.forgehax.api.events.render;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;

import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
@AllArgsConstructor
public class GuiInitializeEvent extends Event {
  private final Screen gui;
  private Consumer<Widget> add;
  private Consumer<Widget> remove;
  private List<Widget> list;

  @Cancelable
  public static class Pre extends GuiInitializeEvent {
    public Pre(Screen gui, Consumer<Widget> add, Consumer<Widget> remove, List<Widget> list) {
      super(gui, add, remove, list);
    }
  }

  public static class Post extends GuiInitializeEvent {
    public Post(Screen gui, Consumer<Widget> add, Consumer<Widget> remove, List<Widget> list) {
      super(gui, add, remove, list);
    }
  }
}
