package com.matt.forgehax.asm2;

import com.google.common.collect.BiMap;
import com.google.common.io.Files;
import com.matt.forgehax.asm.ForgeHaxAccessTransformer;
import com.matt.forgehax.asm.ForgeHaxTransformer;
import com.matt.forgehax.asm2.util.FileDumper;
import com.matt.forgehax.asm2.util.ObfuscationHelper;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

public class CoreMod implements IFMLLoadingPlugin {
    private static final Logger LOGGER = LogManager.getLogger("ForgeHaxAsm");

    private static final boolean DUMP_ASM_DATA = Boolean.parseBoolean(System.getProperty("forgehax.filedump", "true"));

    private static boolean obfuscated = true;
    private static ObfuscationHelper obfuscationHelper;

    public static boolean isObfuscated() {
        return obfuscated;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static ObfuscationHelper getObfuscationHelper() {
        return obfuscationHelper;
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
        obfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
        obfuscationHelper = ObfuscationHelper.newInstance(obfuscated, LOGGER);
        if(DUMP_ASM_DATA) FileDumper.dumpAllFiles(obfuscationHelper);
    }

    @Override
    public String getAccessTransformerClass() {
        return ForgeHaxAccessTransformer.class.getName();
    }
}
