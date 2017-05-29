package com.matt.forgehax.util.command.globals;

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
public class UnbindCommand {
    public static Command newInstance() {
        return new CommandBuilder()
                .setName("unbind")
                .setDescription("Unbinds a key")
                .setProcessor(options -> {
                    List<?> args = options.nonOptionArguments();
                    CommandLine.requireArguments(args, 1);
                    String key = String.valueOf(args.get(0));
                    int keyCode = Keyboard.getKeyIndex(key);
                    if(keyCode == Keyboard.KEY_NONE) throw new CommandExecuteException(String.format("\"%s\" is not a valid key name", key));
                    // todo: finish
                    return false;
                })
                .build()
                ;
    }
}
