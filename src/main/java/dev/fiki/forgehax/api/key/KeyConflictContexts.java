package dev.fiki.forgehax.api.key;

import dev.fiki.forgehax.main.Common;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraftforge.client.settings.IKeyConflictContext;

public interface KeyConflictContexts {
  IKeyConflictContext NONE = new IKeyConflictContext() {
    @Override
    public boolean isActive() {
      return true;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return false;
    }
  };

  IKeyConflictContext IN_GAME = new IKeyConflictContext() {
    @Override
    public boolean isActive() {
      return Common.getDisplayScreen() == null;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return false;
    }
  };

  IKeyConflictContext IN_CONTAINER_GUI = new IKeyConflictContext() {
    @Override
    public boolean isActive() {
      return Common.getDisplayScreen() instanceof ContainerScreen;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return false;
    }
  };

  static IKeyConflictContext none() {
    return NONE;
  }

  static IKeyConflictContext inGame() {
    return IN_GAME;
  }

  static IKeyConflictContext inContainerGui() {
    return IN_CONTAINER_GUI;
  }
}
