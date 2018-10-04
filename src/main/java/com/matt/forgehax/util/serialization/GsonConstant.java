package com.matt.forgehax.util.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/** Created on 5/23/2017 by fr1kin */
public interface GsonConstant {
  Gson GSON = new Gson();
  Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
  JsonParser PARSER = new JsonParser();
}
