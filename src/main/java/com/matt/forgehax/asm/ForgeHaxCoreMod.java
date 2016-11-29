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
        return ForgeHaxAccessTransformer.class.getName();
    }

    public static void print(final String message, final Object... args) {
        if(logger != null) {
            printf(message, args);
        } else {
            // wait for the logger to get its instance
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (logger == null) Thread.sleep(1);
                        printf(message, args);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    public static void print(final List<String> log) {
        StringBuilder builder = new StringBuilder("\n");
        for(String str : log) {
            if(!str.endsWith("\n")) {
                builder.append("\n");
            }
            builder.append(str);
        }
        print(builder.toString());
    }

    public static void printf(String str, Object... args) {
        ForgeHaxCoreMod.logger.error(String.format(str, args));
    }
}
