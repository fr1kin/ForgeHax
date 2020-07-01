package com.matt.forgehax.mods.services;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.Helper;
import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.windows.GuiWindow;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.StubBuilder;
import com.matt.forgehax.util.command.callbacks.CallbackData;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.serialization.ISerializableJson;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.ScaledResolution;



/**
 * Created by Babbaj on 9/10/2017.
 * tonio made it save positions
 */
@RegisterMod
public class GuiService extends ServiceMod {

    public final Options<WindowPosition> windows =
      getCommandStub()
          .builders()
          .<WindowPosition>newOptionsBuilder()
          .name("windows")
          .description("used to save the window positions")
          .supplier(Sets::newConcurrentHashSet)
          .factory(WindowPosition::new)
          .build();
  
  public final Setting<Integer> red =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("red")
          .description("Red amount, 0-255")
          .min(0)
          .max(255)
          .defaultTo(128)
          .build();
  public final Setting<Integer> green =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("green")
          .description("Green amount, 0-255")
          .min(0)
          .max(255)
          .defaultTo(128)
          .build();
  public final Setting<Integer> blue =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("blue")
          .description("Blue amount, 0-255")
          .min(0)
          .max(255)
          .defaultTo(128)
          .build();

  public final Setting<Float> max_height =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("height")
          .description("Max percent of the screen, from 0 to 1")
          .min(0F)
          .max(1F)
          .defaultTo(0.4F)
          .build();
  
  public GuiService() {
    super("GUI");
  }

  @Override
  public void onUnload() {
    super.onUnload();
    windows.serialize();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    windows.deserialize();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("un-hide")
        .description("Sets all windows to visible")
        .processor(
            data -> {
              for (GuiWindow g : ClickGui.getInstance().windowList)
                g.isHidden = false;
            })
        .build();
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("hide")
        .description("Sets all windows to compressed")
        .processor(
            data -> {
              for (GuiWindow g : ClickGui.getInstance().windowList)
                g.isHidden = true;
            })
        .build();
    
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("reset")
        .description("Resets all positions")
        .processor(
            data -> {
              int size = ClickGui.getInstance().windowList.size();
              ScaledResolution scaledRes = ClickGui.getInstance().scaledRes;
              List<GuiWindow> windowList = ClickGui.getInstance().windowList;
              for (int i = 0; i < ClickGui.getInstance().windowList.size(); i++) {
                // Calculate fresh if none is found
                final int x = (i + 3) / 2 * scaledRes.getScaledWidth() / (size - 2)
                    - windowList.get(i).width / 2;
                final int y = scaledRes.getScaledHeight() / 25 + order(i) * scaledRes.getScaledHeight() / 2;
          
                // Here check if the window goes offscreen, if true push it down all the others
                windowList.get(i).setPosition(x, y);
              }
            })
        .build();
  }

  private int order(final int i) {
    if(i < 2) {
      return 0;
    }
    return (i + 1) % 2; // Distance between windows
  }

  
  @Override
  public void onBindPressed(CallbackData cb) {
    if (Helper.getLocalPlayer() != null) {
      MC.displayGuiScreen(ClickGui.getInstance());
    }
  }
  
  @Override
  protected StubBuilder buildStubCommand(StubBuilder builder) {
    return builder
      .kpressed(this::onBindPressed)
      .kdown(this::onBindKeyDown)
      .bind(Keyboard.KEY_RSHIFT) // default to right shift
      ;
  }
  public static class WindowPosition implements ISerializableJson {
    public final String title;
    public int x=0, y=0;
    public boolean hidden = false;

    public WindowPosition(String title, int x, int y) {
      this.title = title;
      this.x = x;
      this.y = y;
    }

    public WindowPosition(String title) {
      this.title = title;
    }

    @Override
    public void serialize(JsonWriter writer) throws IOException {
      JsonObject buf = new JsonObject();
      for (GuiWindow w : ClickGui.getInstance().windowList) {
        if (w.title.equals(this.title)) {
          System.out.println(String.format("{%s [%d|%d %s]", w.title, w.posX, w.headerY, w.isHidden));
          buf.addProperty("x", w.posX);
          buf.addProperty("y", w.headerY);
          buf.addProperty("hidden", w.isHidden);
          writer.jsonValue(buf.toString());
          return;
        }
      }
      writer.value("");
    }

    @Override
    public void deserialize(JsonReader reader) {
      JsonObject buf = new JsonParser().parse(reader).getAsJsonObject();
      this.x = buf.get("x").getAsInt();
      this.y = buf.get("y").getAsInt();
      this.hidden = buf.get("hidden").getAsBoolean();
    }

    @Override
    public String getUniqueHeader() {
      return this.title;
    }

    @Override
    public String toString() {
      return getUniqueHeader();
    }

  }
}
