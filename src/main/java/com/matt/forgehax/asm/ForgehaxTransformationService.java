package com.matt.forgehax.asm;

import com.matt.forgehax.asm.patches.NetManagerPatch;
import com.matt.forgehax.asm.transformer.MethodTransformerWrapper;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;

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
        System.out.println("Initialized ForgehaxASM");
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {

    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return Arrays.asList(
                new MethodTransformerWrapper(new NetManagerPatch.DispatchPacket()),
                new MethodTransformerWrapper(new NetManagerPatch.FlushHook()),
                new MethodTransformerWrapper(new NetManagerPatch.ChannelRead0())
        );
    }
}
