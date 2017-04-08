package com.matt.forgehax.asm2;

import com.matt.forgehax.asm.ForgeHaxAccessTransformer;
import com.matt.forgehax.asm2.util.FileDumper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class CoreMod implements IFMLLoadingPlugin {
    public CoreMod() {
        Core.Initialization.registerListener((obfuscated) -> {
            if(false) FileDumper.dumpAllFiles(Core.getObfuscationHelper());
        });
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {Transformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return "com.matt.forgehax.asm2.CoreContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        final boolean obfuscated = (Boolean)data.get("runtimeDeobfuscationEnabled");
        Core.Initialization.getListeners().forEach(listener -> listener.onInitialize(obfuscated));
        // let the garbage collector do its work
        Core.Initialization.finished();
    }

    @Override
    public String getAccessTransformerClass() {
        return ForgeHaxAccessTransformer.class.getName();
    }


}
