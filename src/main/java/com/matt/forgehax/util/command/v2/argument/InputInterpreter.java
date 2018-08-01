package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.ICommandV2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created on 1/30/2018 by fr1kin
 */
public interface InputInterpreter {
    /**
     * List of interpretations for the given input.
     * Ranges from best to worst interpreter
     * @param command calling command
     * @param input text to infer
     * @return Empty if nothing can possibly match, otherwise the first result will have the best matching suggestion
     */
    @Nonnull
    List<String> getInterpretations(ICommandV2 command, String input);

    @Nullable
    default String getTopInterpretation(ICommandV2 command, String input, String defaultTo) {
        return getInterpretations(command, input).stream()
                .findFirst()
                .orElse(defaultTo);
    }
    @Nullable
    default String getTopInterpretation(ICommandV2 command, String input) {
        return getTopInterpretation(command, input, null);
    }

    boolean isInterpretable();

    interface Function<E> {
        /**
         * List of interpreter for the given input.
         * Ranges from best to worst interpreter
         * @param caller instance of generic calling this function
         * @param command calling command
         * @param input text to infer
         * @return Empty if nothing can possibly match, otherwise the first result will have the best matching suggestion
         */
        @Nonnull
        List<String> apply(E caller, ICommandV2 command, String input);
    }
}
