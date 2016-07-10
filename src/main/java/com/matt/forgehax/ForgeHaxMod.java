package com.matt.forgehax;

public @interface ForgeHaxMod {
    /**
     * Mods name
     */
    String name = "";

    /**
     * Description of the mod
     */
    String description = "";

    /**
     * Default value of the mod (on/off)
     */
    boolean defaultValue = false;

    /**
     * Default key the mod is bound to
     * -1 to disable the mods key bind
     */
    int defaultKeyBind = -1;
}
