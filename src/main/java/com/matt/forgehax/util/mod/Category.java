package com.matt.forgehax.util.mod;

/**
 * Created on 9/4/2017 by fr1kin
 */
public enum Category {
  NONE("", ""),
  COMBAT("Combat", "Combat related mods"),
  PLAYER("Player", "Mods that interact with the local player"),
  RENDER("Render", "2D/3D rendering mods"),
  WORLD("World", "Any mod that has to do with the world"),
  MISC("Misc", "Miscellaneous"),
  SERVICE("Service", "Background mods"),
  ;
  
  private String prettyName;
  private String description;
  
  Category(String prettyName, String description) {
    this.prettyName = prettyName;
    this.description = description;
  }
  
  public String getPrettyName() {
    return prettyName;
  }
  
  public String getDescription() {
    return description;
  }
}
