package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Maps;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.cmd.settings.maps.SimpleSettingMap;
import dev.fiki.forgehax.main.util.key.BindingHelper;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.reflection.fasttype.FastField;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.BiConsumer;

/**
 * Created by Babbaj on 1/30/2018.
 */
@RegisterMod
public class AutoKey extends ToggleMod {
  @Getter
  private static AutoKey instance;

  private IntegerSetting holdTime = newIntegerSetting()
      .name("hold-time")
      .description("how long to hold button for tap")
      .defaultTo(150) // approximate minimum for reliable key pressing
      .build();

  private final IntegerSetting delay = newIntegerSetting()
      .name("delay")
      .description("delay(ms) between clicks")
      .defaultTo(500) // 500 ms
      .min(0)
      .build();

  private final SimpleSettingMap<KeyBinding, ClickMode> active = newSettingMap(KeyBinding.class, ClickMode.class)
      .name("keys")
      .description("Current active keys")
      .keyArgument(Arguments.newArgument(KeyBinding.class)
          .label("key")
          .converter(KeyBinding::getKeyDescription)
          .parser(BindingHelper::getKeyBindByDescription)
          .build())
      .valueArgument(Arguments.newEnumArgument(ClickMode.class)
          .label("mode")
          .build())
      .supplier(Maps::newHashMap)
      .build();

  private long lastTimeMillis;

  public AutoKey() {
    super(Category.PLAYER, "AutoKey", false, "Automatically click/press keys");
    instance = this;
  }

  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    final int lastClick = (int) (System.currentTimeMillis() - lastTimeMillis);
    if (lastClick >= delay.getValue()) {
      lastTimeMillis = System.currentTimeMillis();
    }

    active.forEach((key, mode) -> mode.apply(key, lastClick));
  }

  private static void incrementPressTime(KeyBinding binding) {
    FastField<Integer> field = FastReflection.Fields.KeyBinding_pressTime;
    int currTime = field.get(binding);
    field.set(binding, currTime + 1);
  }

  private enum ClickMode {
    TAP(
        (key, time) -> {
          if (time < AutoKey.getInstance().holdTime.getValue()) {
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

    BiConsumer<KeyBinding, Integer> clickAction;

    ClickMode(BiConsumer<KeyBinding, Integer> action) {
      this.clickAction = action;
    }

    public void apply(KeyBinding key, int lastTime) {
      clickAction.accept(key, lastTime);
    }
  }
}
