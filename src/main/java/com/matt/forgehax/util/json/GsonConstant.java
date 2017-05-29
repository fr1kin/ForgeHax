package com.matt.forgehax.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created on 5/23/2017 by fr1kin
 */
public interface GsonConstant {
    Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
}
