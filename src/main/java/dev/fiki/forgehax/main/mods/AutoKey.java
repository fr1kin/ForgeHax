package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.reflection.fasttype.FastField;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.key.Bindings;
import dev.fiki.forgehax.main.util.key.KeyBindingHandler;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by Babbaj on 1/30/2018.
 */
@RegisterMod
public class AutoKey extends ToggleMod {
  
  public AutoKey() {
    super(Category.PLAYER, "AutoKey", false, "Automatically click/press keys");
  }
  
  private final Setting<Integer> delay =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("delay")
          .description("delay(ms) between clicks")
          .defaultTo(500) // 500 ms
          .min(0)
          .build();
  
  private static Setting<Integer> holdTime; // static to allow easy access from ClickMode
  
  {
    holdTime =
        getCommandStub()
            .builders()
            .<Integer>newSettingBuilder()
            .name("holdTime")
            .description("how long to hold button for tap")
            .defaultTo(150) // approximate minimum for reliable key pressing
            .build();
  }
  
  // TODO: make serializable and save as json
  private final Map<KeyBindingHandler, ClickMode> activeKeys = new HashMap<>();
  
  private long lastTimeMillis;
  
  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    final int lastClick = (int) (System.currentTimeMillis() - lastTimeMillis);
    if (lastClick >= delay.get()) {
      lastTimeMillis = System.currentTimeMillis();
    }
    
    activeKeys.forEach((key, mode) -> mode.apply(key, lastClick));
  }
  
  @Override
  public void onLoad() {
    // add a key
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("addKey")
        .description("add a key to the active key list - (ex: addKey \"jump\" \"hold\"")
        .processor(data -> {
          data.requiredArguments(2);
          KeyBindingHandler key = Bindings.getKey(data.getArgumentAsString(0));

          if (key == null) {
            Globals.printError("Unknown key: %s", data.getArgumentAsString(0));
            return;
          }

          String mode = data.getArgumentAsString(1);
          ClickMode clickMode = Arrays.stream(ClickMode.values())
              .filter(m -> m.toString().toLowerCase().contains(mode.toLowerCase()))
              .findFirst()
              .orElseGet(() -> {
                Globals.printError("Unknown mode, defaulting to tap");
                return ClickMode.TAP;
              });

          activeKeys.put(key, clickMode);
        })
        .build();
    
    // remove all keys
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("clearKeys")
        .description("clear all the active keys")
        .processor(data -> {
            if (data.getArgumentCount() > 0) {
              Globals.printError("Unexpected arguments!");
              return;
            }
            activeKeys.clear();
        })
        .build();
    
    // remove a single key
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("clearKey")
        .description("remove an active key - (ex: clearKey \"jump\"")
        .processor(
            data -> {
              data.requiredArguments(1);
              KeyBindingHandler key = Bindings.getKey(data.getArgumentAsString(0));
              ClickMode mode = activeKeys.remove(key);
              if (mode != null) {
                Globals.printInform("Removed key: %s", key.getBinding().getKeyDescription());
              } else {
                Globals.printInform("Unknown key");
              }
            })
        .build();
  }
  
  private static void incrementPressTime(KeyBindingHandler binding) {
    FastField<Integer> field = FastReflection.Fields.KeyBinding_pressTime;
    int currTime = field.get(binding.getBinding());
    field.set(binding.getBinding(), currTime + 1);
  }
  
  private enum ClickMode {
    TAP(
        (key, time) -> {
          if (time < holdTime.getAsInteger()) {
            incrementPressTime(key);
            key.setPressed(true);
          } else {
            key.setPressed(false);
          }
        }), // hold key for at least 150ms
    
    HOLD(
        (key, time) -> {
          incrementPressTime(key);
          key.setPressed(true);
        }); // hold key forever
    
    BiConsumer<KeyBindingHandler, Integer> clickAction;
    
    ClickMode(BiConsumer<KeyBindingHandler, Integer> action) {
      this.clickAction = action;
    }
    
    public void apply(KeyBindingHandler key, int lastTime) {
      clickAction.accept(key, lastTime);
    }
  }
}
