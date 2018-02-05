package com.matt.forgehax.util.command.v2.argument;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created on 1/30/2018 by fr1kin
 */
public interface ISuggestionProvider {
    /**
     * List of suggestions for the given input.
     * Ranges from best to worst suggestions
     * @param input text to infer
     * @return Empty if nothing can possibly match, otherwise the first result will have the best matching suggestion
     */
    @Nonnull
    List<String> getSuggestions(String input);

    interface Function<E> {
        /**
         * List of suggestions for the given input.
         * Ranges from best to worst suggestions
         * @param o instance of generic calling this function
         * @param input text to infer
         * @return Empty if nothing can possibly match, otherwise the first result will have the best matching suggestion
         */
        @Nonnull
        List<String> apply(E o, String input);
    }
}
