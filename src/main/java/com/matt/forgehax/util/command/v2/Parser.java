package com.matt.forgehax.util.command.v2;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.ArrayHelper;
import com.matt.forgehax.util.command.v2.argument.*;
import com.matt.forgehax.util.command.v2.exception.CmdMissingArgumentException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 4/18/2018 by fr1kin
 */
public class Parser<E extends ICmd> {
    private final List<ArgMap<?>> arguments;
    private final List<OptionMap<?>> options;
    private final List<String> remaining = Lists.newArrayList();

    protected Parser(E command, String[] args) throws CmdMissingArgumentException {
        this.arguments = command.getArguments().stream()
                .map(ArgBuilder::newArgMap)
                .collect(Collectors.toList());

        this.options = command.getOptions().stream()
                .map(OptionBuilder::newOptionMap)
                .collect(Collectors.toList());

        boolean stopOptions = false;

        int index = 0;
        int argumentIndex = 0;

        for(; index < args.length; ++index) {
            String next = args[index];

            if(!stopOptions && isOption(next)) {
                Extract parse = new Extract(next);

                // look for the option
                Optional<OptionMap<Object>> opt = getOption(parse.name);

                if(opt.isPresent()) { // we have a match
                    OptionMap option = opt.get();
                    if(!option.isFlag()) {
                        if(parse.value != null) {
                            option.withInput(parse.value); // already provided the argument
                        } else if(ArrayHelper.isInRange(args, index + 1)) { // we have another argument
                            String arg = args[++index]; // get next arg

                            if(isOption(arg)) {
                                Extract p = new Extract(arg);
                                if(getOption(p.name) == null) {
                                    option.withInput(arg); // since there is no matching option, assume this was intended to be an argument
                                } else {
                                    if(option.isRequired())
                                        throw new CmdMissingArgumentException(command, option); // no argument provided
                                    else if(option.isOptional()) {
                                        option.withDefaultValue(); // append the default value
                                        --index; // go back to process the option we jumped to properly
                                    }
                                }
                            } else {
                                option.withInput(arg); // normal value
                            }
                        } else {
                            if (option.isRequired())
                                throw new CmdMissingArgumentException(command, option); // no argument provided
                            else if(option.isOptional()) {
                                option.withDefaultValue(); // use default argument since none was provided
                            }
                        }
                    } else {
                        option.withDefaultValue();
                    }
                    continue; // stop
                }

                // since this isn't an option, it will be interpreted as an argument
            }

            if(argumentIndex > arguments.size() - 1) {
                remaining.add(next);
                continue;
            }

            arguments.get(argumentIndex).withInput(next);

            ++argumentIndex;
            stopOptions = true; // do not process anymore options
        }

        for(int i = argumentIndex; i < arguments.size(); ++i) {
            ArgMap<?> arg = arguments.get(i);
            if(arg.isRequired())
                throw new CmdMissingArgumentException(command, arg);
            else
                arg.withDefaultValue();
        }
    }

    public <T> Optional<ArgMap<T>> getArgument(int index) {
        return index > -1 && index < arguments.size() ? Optional.of(arguments.get(index)).map(a -> (ArgMap<T>)a) : Optional.empty();
    }

    public <T> Optional<OptionMap<T>> getOption(String name) {
        return options.stream()
                .filter(option -> option.contains(name))
                .findFirst()
                .map(o -> (OptionMap<T>)o);
    }

    public String[] getRemaining() {
        return remaining.toArray(new String[0]);
    }

    //
    //
    //

    private static boolean isLongToken(String text) {
        return text.startsWith("--") && text.length() > "--".length();
    }

    private static boolean isShortToken(String text) {
        return text.startsWith("-") && !text.startsWith("--") && text.length() > "-".length();
    }

    private static boolean isOption(String text) {
        return isLongToken(text) || isShortToken(text);
    }

    private static String extractOption(String text) {
        return isLongToken(text) ? text.substring(2) : text.substring(1); // remove the -- or - from the option
    }

    private static String[] parseOption(String text) {
        String name = extractOption(text); // remove the -- or - from the option
        String arg = null; // possible argument

        // can be formatted like option=argument
        if(name.contains("=")) {
            String[] ss = name.split("=");
            name = ss[0];
            arg = ss[1];
        }

        return new String[] {name, arg};
    }

    static final class Extract {
        String name;
        String value;

        Extract(String from) {
            String[] ss = parseOption(from);
            this.name = ss[0];
            this.value = ss[1];
        }
    }
}
