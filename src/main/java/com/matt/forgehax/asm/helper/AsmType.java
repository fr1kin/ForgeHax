package com.matt.forgehax.asm.helper;

import com.matt.forgehax.asm.ForgeHaxCoreMod;

public abstract class AsmType<E extends AsmType> {
    protected String realName = "";
    protected String obfuscatedName = "";

    public String getName() {
        return realName;
    }
    public E setName(String realName) {
        this.realName = realName;
        return (E)this;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }
    public E setObfuscatedName(String obfuscatedName) {
        this.obfuscatedName = obfuscatedName;
        return (E)this;
    }

    public String getRuntimeName() {
        return (ForgeHaxCoreMod.isObfuscated && !obfuscatedName.isEmpty()) ? obfuscatedName : realName;
    }
}
