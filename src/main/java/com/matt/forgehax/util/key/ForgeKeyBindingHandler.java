package com.matt.forgehax.util.key;

import com.matt.forgehax.ForgeHax;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;

public class ForgeKeyBindingHandler extends KeyBindingHandler {

  static {// this class shouldn't be used if not in forge forge
    if (!ForgeHax.isForge()) {
      throw new Error("Not forge environment");
    }
  }

  private static final IKeyConflictContext OVERRIDE_KEYCONFLICT_CONTEXT = new IKeyConflictContext() {
    @Override
    public boolean isActive() {
      return true;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return false;
    }
  };


  private IKeyConflictContext oldConflictContext = null;

  private int bindingCount = 0;

  public ForgeKeyBindingHandler(KeyBinding bind) {
    super(bind);
  }

  @Override
  public boolean isBound() {
    return binding.getKeyConflictContext() == OVERRIDE_KEYCONFLICT_CONTEXT;
  }

  @Override
  public void bind() {
    // increase every time bind is attempted
    // this is to prevent issues with mod compatibility
    bindingCount++;
    if(oldConflictContext == null) {
      oldConflictContext = binding.getKeyConflictContext();
      binding.setKeyConflictContext(OVERRIDE_KEYCONFLICT_CONTEXT);
    }
  }

  @Override
  public void attemptBind() {
    if(!isBound()) bind();
  }

  @Override
  public void unbind() {
    bindingCount--;
    // only unbind key conflict if the binding count is 0 or less (idk why it would be less)
    if(oldConflictContext != null &&
        bindingCount <= 0) {
      binding.setKeyConflictContext(oldConflictContext);
      oldConflictContext = null;
    }
    // reset to 0 just in case it somehow goes below
    if(bindingCount < 0)
      bindingCount = 0;
  }

  @Override
  public void attemptUnbind() {
    if(isBound()) unbind();
  }
}
