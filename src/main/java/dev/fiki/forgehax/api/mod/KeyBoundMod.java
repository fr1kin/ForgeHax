package dev.fiki.forgehax.api.mod;

import dev.fiki.forgehax.api.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.api.key.KeyConflictContexts;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;

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

  public KeyBoundMod() {
    super();
  }

  public abstract void onKeyPressed(KeyBinding key);

  public abstract void onKeyDown(KeyBinding key);

  public abstract void onKeyReleased(KeyBinding key);
}
