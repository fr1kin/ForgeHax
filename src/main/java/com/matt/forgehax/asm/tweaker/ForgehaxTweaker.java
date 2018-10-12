package com.matt.forgehax.asm.tweaker;

import io.github.impactdevelopment.simpletweaker.SimpleTweaker;
import java.util.ArrayList;
import java.util.Objects;
import net.futureclient.asm.AsmLibApi;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

public class ForgehaxTweaker extends SimpleTweaker {

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        AsmLibApi.init();
        AsmLibApi.registerConfig("asmlib.forgehax.config.json");
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

}
