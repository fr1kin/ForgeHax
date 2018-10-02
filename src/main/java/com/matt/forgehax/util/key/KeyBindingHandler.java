package com.matt.forgehax.util.key;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.reflection.FastReflection;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;

public class KeyBindingHandler implements Globals {
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

    private final KeyBinding binding;

    private IKeyConflictContext oldConflictContext = null;

    private int bindingCount = 0;

    public KeyBindingHandler(KeyBinding bind) {
        binding = bind;
    }

    public KeyBinding getBinding() {
        return binding;
    }

    public boolean isPressed() {
        return FastReflection.Fields.Binding_pressed.get(binding);
    }

    public void setPressed(boolean pressed) {
        FastReflection.Fields.Binding_pressed.set(binding, pressed);
    }

    public int getPressTime() {
        return FastReflection.Fields.Binding_pressTime.get(binding);
    }

    public void setPressTime(int time) {
        FastReflection.Fields.Binding_pressTime.set(binding, time);
    }

    public boolean isBound() {
        return binding.getKeyConflictContext() == OVERRIDE_KEYCONFLICT_CONTEXT;
    }

    public void bind() {
        // increase every time bind is attempted
        // this is to prevent issues with mod compatibility
        bindingCount++;
        if(oldConflictContext == null) {
            oldConflictContext = binding.getKeyConflictContext();
            binding.setKeyConflictContext(OVERRIDE_KEYCONFLICT_CONTEXT);
        }
    }

    public void attemptBind() {
        if(!isBound()) bind();
    }

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

    public void attemptUnbind() {
        if(isBound()) unbind();
    }
}
