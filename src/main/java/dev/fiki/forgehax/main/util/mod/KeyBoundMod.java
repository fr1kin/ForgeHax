package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.key.KeyConflictContexts;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;

import java.util.Set;

@Getter(AccessLevel.PROTECTED)
public abstract class KeyBoundMod extends AbstractMod {
  private final KeyBindingSetting keyBindingSetting = newKeyBindingSetting()
      .name("bind")
      .description("Key bind to enable the mod")
      .unbound()
      .defaultKeyName()
      .defaultKeyCategory()
      .conflictContext(KeyConflictContexts.inGame())
      .keyDownListener(this::onKeyDown)
      .keyPressedListener(this::onKeyPressed)
      .keyReleasedListener(this::onKeyReleased)
      .build();

  {
    newSimpleCommand()
        .name("unbind")
        .description("Unbind the key this mod is set to")
        .executor(args -> keyBindingSetting.unbind())
        .build();
  }

  public KeyBoundMod(Category category, String name, String desc, Set<EnumFlag> flags) {
    super(category, name, desc, flags);
  }

  public KeyBoundMod() {
    super();
  }

  public abstract void onKeyPressed(KeyBinding key);

  public abstract void onKeyDown(KeyBinding key);

  public abstract void onKeyReleased(KeyBinding key);
}
