package dev.fiki.forgehax.api.mod;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created on 9/4/2017 by fr1kin
 */
@Getter
@AllArgsConstructor
public enum Category {
  NONE("", ""),
  COMBAT("Combat", "Combat related mods"),
  PLAYER("Player", "Mods that interact with the local player"),
  RENDER("Render", "2D/3D rendering mods"),
  WORLD("World", "Any mod that has to do with the world"),
  CHAT("Chat", "Chat related mods"),
  UI("UI", "User interface related"),
  EXPLOIT("Exploit", "Exploit prevention or execution"),
  MISC("Misc", "Miscellaneous"),
  SERVICE("Service", "Background mods"),
  ;

  private String prettyName;
  private String description;
}
