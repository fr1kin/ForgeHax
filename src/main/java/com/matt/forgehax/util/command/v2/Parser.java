package com.matt.forgehax.util.command.v2;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.CaseSensitive;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2;
import com.matt.forgehax.util.command.v2.argument.OptionV2;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Created on 4/18/2018 by fr1kin
 */
public class Parser<E extends ICommandV2> {
    protected Parser(E command, String[] args) throws CommandRuntimeExceptionV2.MissingArgument {
        Options options = new Options(command.getOptions());

        boolean acceptingArguments = true;

        ListIterator<String> it = Arrays.asList(args).listIterator();
        ListIterator<ArgumentV2<?>> itargs = command.getArguments().listIterator();

        while(it.hasNext()) {
            String next = it.next();

            if(isOption(next)) {
                String[] ss = parseOption(next);
                String name = ss[0], arg = ss[1];

                // look for the option
                OptionV2 option = command.getOption(name);

                if(option != null) { // we have a match
                    if(option.hasArgument()) {
                        ArgumentV2<?> argu = option.getArgument();
                        if(arg == null) { // we have to check the next argument in the list
                            if(it.hasNext()) { // we have another argument
                                arg = it.next();

                                // check to see if the argument is another option
                                if(isOption(arg)) {
                                    ss = parseOption(arg);
                                    if(command.getOption(ss[0]) != null) { // if an option matches the next argument, then it is probably not a coincidence
                                        if(argu.isRequired())
                                            throw new CommandRuntimeExceptionV2.MissingArgument(argu); // stop processing and throw missing argument exception
                                        else {
                                            // this options argument is optional, so it will use the default value
                                            arg = null;
                                            it.previous();
                                        }
                                    }
                                }
                            } else if(argu.isRequired()) throw new CommandRuntimeExceptionV2.MissingArgument(argu); // this argument must exist
                        }
                    } else arg = null; // this option doesn't have an argument, so just ignore this

                    // add the option to the list
                    options.append(option, arg);

                    // stop accepting arguments
                    acceptingArguments = false;

                    continue; // stop
                }
            }

            if(acceptingArguments) {

            }
        }
    }

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

    public static class Arguments {
        final List<ArgumentV2<?>> all = Lists.newArrayList();

        Arguments(List<ArgumentV2<?>> defaults) {
            all.addAll(defaults);
        }
    }

    public static class Options {
        final List<OptionV2<?>> all = Lists.newArrayList();

        Options(List<OptionV2<?>> defaults) {
            all.addAll(defaults);
        }

        private void verify() {
            for(OptionV2<?> op : all) {
                switch (op.getType()) {
                    case OPTIONAL_ARGUMENT:
                    {
                        if(!op.getArgument().hasValue()) appendDefault(op); // use default value
                        break;
                    }
                    default: break;
                }
            }
        }

        private Optional<OptionV2<?>> find(OptionV2<?> similar) {
            return all.stream()
                    .filter(similar::equals)
                    .findFirst();
        }

        private Optional<OptionV2<?>> find(final String name) {
            return all.stream()
                    .filter(op -> op.contains(name))
                    .findFirst();
        }

        private void append(OptionV2<?> similar, final String value) {
            find(similar).ifPresent(op -> all.set(all.indexOf(op), op.appendValue(value)));
        }
        private void appendDefault(OptionV2<?> similar) {
            find(similar).ifPresent(op -> all.set(all.indexOf(op), op.appendDefaultValue()));
        }

        @Nullable
        public OptionV2<?> get(OptionV2<?> similar) {
            return find(similar).filter(op -> !op.hasArgument() || op.getArgument().hasValue()).orElse(null);
        }

        @Nullable
        public OptionV2<?> get(@CaseSensitive final String name) {
            return find(name).filter(op -> !op.hasArgument() || op.getArgument().hasValue()).orElse(null);
        }

        public boolean has(OptionV2<?> option) {
            return get(option) != null;
        }
        public boolean has(@CaseSensitive final String name) {
            return get(name) != null;
        }
    }
}
