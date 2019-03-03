package com.matt.forgehax.asm;

import com.matt.forgehax.asm.patches.*;
import com.matt.forgehax.asm.transformer.MethodTransformerWrapper;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.environment.State;
import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.api.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ForgehaxTransformationService implements ITransformationService {
    @Nonnull
    @Override
    public String name() {
        return "ForgehaxASM";
    }

    @Override
    public void initialize(IEnvironment environment) {
        final boolean isDev = environment.getProperty(Environment.Keys.VERSION.get())
                .map(v -> v.equals("FMLDev"))
                .orElseThrow(() -> new IllegalStateException("Failed to get forge version??"));
        RuntimeState.initializeWithState(isDev ? State.NORMAL : State.SRG);

        System.out.println("Initialized ForgehaxASM");
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {

    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        // TODO: get these automatically
        return Arrays.asList(
            new MethodTransformerWrapper(new NetManagerPatch.DispatchPacket()),
            new MethodTransformerWrapper(new NetManagerPatch.FlushHook()),
            new MethodTransformerWrapper(new NetManagerPatch.ChannelRead0()),
            new MethodTransformerWrapper(new MinecraftPatch.RunTick()),
            new MethodTransformerWrapper(new MinecraftPatch.SendClickBlockToController()),
            new MethodTransformerWrapper(new KeyboardListenerPatch.OnKeyEvent()),
            new MethodTransformerWrapper(new BlockPatch.GetCollisionShape()),
            new MethodTransformerWrapper(new KeyBindingPatch.IsKeyDown())
        );
    }
}
