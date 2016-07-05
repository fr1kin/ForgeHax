package com.matt.forgehax.mods;

import java.io.File;

public class FilterMod extends BaseMod {
    private File fitlerDir = new File(MOD.getConfigFolder(), "fitlers");

    public FilterMod(String name, String desc) {
        super(name, desc);
    }


}
