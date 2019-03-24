package com.matt.forgehax.asm;

import com.matt.forgehax.asm.patches.*;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.environment.State;
import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.api.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForgehaxTransformationService implements ITransformationService {
    @Nonnull
    @Override
    public String name() {
        return "ForgehaxASM";
    }

    @Override
    public void initialize(IEnvironment environment) {
        final boolean isDev = environment.getProperty(Environment.Keys.VERSION.get())
                .map(v -> v.equals("FMLDev") || v.equals("MOD_DEV"))
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
        return getTransformersForClasses(
            NetManagerPatch.class,
            MinecraftPatch.class,
            KeyboardListenerPatch.class,
            BlockPatch.class,
            KeyBindingPatch.class,
            BoatPatch.class,
            RenderBoatPatch.class,
            PlayerTabOverlayPatch.class,
            WorldRendererPatch.class,
            VisGraphPatch.class,
            BufferBuilderPatch.class,
            ActiveRenderInfoPatch.class,
            EntityPatch.class,
            EntityPlayerSPPatch.class,
            GameRendererPatch.class,
            EntityLivingBasePatch.class
        );
    }

    @SuppressWarnings("unchecked")
    private List<ITransformer> getTransformersForClasses(Class<?>... patches) {
        return (List<ITransformer>)Stream.of(patches) // epic cast because compiler bug
                .flatMap(clazz -> Stream.of(clazz.getDeclaredClasses()))
                .filter(inner -> inner.isAnnotationPresent(RegisterTransformer.class))
                .peek(inner -> {
                    if (!hasNoArgConstructor(inner))
                        throw new IllegalStateException(inner.getSimpleName() + " does not have a 0 arg constructor");
                })
                .map(inner -> Transformer.createWrapper((ITransformer)this.newInstance(inner), inner.getDeclaredAnnotation(RegisterTransformer.class)))
                .collect(Collectors.toList());

    }

    private <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean hasNoArgConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }
}
