package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.ICommandV2;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created on 1/30/2018 by fr1kin
 */
public interface IPredictableArgument {
    /**
     * List of predictor for the given input.
     * Ranges from best to worst predictor
     * @param command calling command
     * @param input text to infer
     * @return Empty if nothing can possibly match, otherwise the first result will have the best matching suggestion
     */
    @Nonnull
    List<String> getPredictions(ICommandV2 command, String input);

    interface Function<E> {
        /**
         * List of predictor for the given input.
         * Ranges from best to worst predictor
         * @param o instance of generic calling this function
         * @param command calling command
         * @param input text to infer
         * @return Empty if nothing can possibly match, otherwise the first result will have the best matching suggestion
         */
        @Nonnull
        List<String> apply(E o, ICommandV2 command, String input);
    }
}
