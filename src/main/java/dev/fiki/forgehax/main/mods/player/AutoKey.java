package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Maps;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.cmd.settings.maps.SimpleSettingMap;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.key.BindingHelper;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.settings.KeyBinding;

/**
 * Created by Babbaj on 1/30/2018.
 */
@RegisterMod(
    name = "AutoKey",
    description = "Automatically click/press keys",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AutoKey extends ToggleMod {
  private final ReflectionTools reflection;

  private final IntegerSetting holdTime = newIntegerSetting()
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
          .converter(KeyBinding::getName)
          .parser(BindingHelper::getKeyBindByDescription)
          .build())
      .valueArgument(Arguments.newEnumArgument(ClickMode.class)
          .label("mode")
          .build())
      .supplier(Maps::newHashMap)
      .build();

  private long lastTimeMillis;

  @SubscribeListener
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    final int lastClick = (int) (System.currentTimeMillis() - lastTimeMillis);
    if (lastClick >= delay.getValue()) {
      lastTimeMillis = System.currentTimeMillis();
    }

    active.forEach((key, mode) -> {
      switch (mode) {
        case TAP:
          if (lastClick < holdTime.getValue()) {
            incrementPressTime(key);
            key.setDown(true);
          } else {
            key.setDown(false);
          }
          break;
        case HOLD:
          incrementPressTime(key);
          key.setDown(true);
          break;
      }
    });
  }

  private void incrementPressTime(KeyBinding binding) {
    ReflectionField<Integer> field = reflection.KeyBinding_clickCount;
    int currTime = field.get(binding);
    field.set(binding, currTime + 1);
  }

  private enum ClickMode {
    TAP, // hold key for at least 150ms
    HOLD;
  }
}
