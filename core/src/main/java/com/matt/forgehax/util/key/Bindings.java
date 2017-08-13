package com.matt.forgehax.util.key;

import com.matt.forgehax.Globals;

public class Bindings implements Globals {
    public static KeyBindingHandler forward = new KeyBindingHandler(MC.gameSettings.keyBindForward);
    public static KeyBindingHandler back = new KeyBindingHandler(MC.gameSettings.keyBindBack);
    public static KeyBindingHandler left = new KeyBindingHandler(MC.gameSettings.keyBindLeft);
    public static KeyBindingHandler right = new KeyBindingHandler(MC.gameSettings.keyBindRight);

    public static KeyBindingHandler jump = new KeyBindingHandler(MC.gameSettings.keyBindJump);

    public static KeyBindingHandler sprint = new KeyBindingHandler(MC.gameSettings.keyBindSprint);
    public static KeyBindingHandler sneak = new KeyBindingHandler(MC.gameSettings.keyBindSneak);

    public static KeyBindingHandler attack = new KeyBindingHandler(MC.gameSettings.keyBindAttack);
    public static KeyBindingHandler use = new KeyBindingHandler(MC.gameSettings.keyBindUseItem);
}
