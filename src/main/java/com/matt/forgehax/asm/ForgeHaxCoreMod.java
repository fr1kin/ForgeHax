package com.matt.forgehax.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ForgeHaxCoreMod implements IFMLLoadingPlugin {
    public static boolean isObfuscated = true;
    public static Logger logger;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {ForgeHaxTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return "com.matt.forgehax.asm.ForgeHaxCoreContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        if(data.containsKey("runtimeDeobfuscationEnabled")) isObfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
