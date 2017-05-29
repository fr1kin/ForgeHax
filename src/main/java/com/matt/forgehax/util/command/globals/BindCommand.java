package com.matt.forgehax.util.command.globals;

import com.matt.forgehax.Wrapper;
import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.mods.ToggleMod;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.command.CommandExecuteException;
import com.matt.forgehax.util.command.CommandLine;
import org.lwjgl.input.Keyboard;

import java.util.List;

import static com.matt.forgehax.Wrapper.getModManager;

/**
 * Created on 5/27/2017 by fr1kin
 */
public class BindCommand {
    public static Command newInstance() {
        return new CommandBuilder()
                .setName("bind")
                .setDescription("Binds a key to a command")
                .setProcessor(options -> {
                    List<?> args = options.nonOptionArguments();
                    CommandLine.requireArguments(args, 2);
                    String key = String.valueOf(args.get(0));
                    int keyCode = Keyboard.getKeyIndex(key.toUpperCase());
                    if(keyCode == Keyboard.KEY_NONE) throw new CommandExecuteException(String.format("\"%s\" is not a valid key name", key));
                    String name = String.valueOf(args.get(1));
                    BaseMod mod = getModManager().getMod(name);
                    if(mod != null && mod instanceof ToggleMod) {
                        ToggleMod toggleMod = (ToggleMod)mod;
                        toggleMod.getToggleBind().setKeyCode(keyCode);
                        Wrapper.printMessage(String.format("Bound \"%s\" to key \"%s\"", name, key));
                        return true;
                    } else Wrapper.printMessage(String.format("\"%s\" is not a valid toggleable mod", name));
                    return false;
                })
                .build()
                ;
    }
}
