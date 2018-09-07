package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.CaseSensitive;
import com.matt.forgehax.util.SafeConverter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

public interface IOption<E> extends IArg<E> {
    /**
     * Get a list of names for this option
     * @return collection of names representing this option
     */
    Collection<String> getNames();

    /**
     * Gets the description for the option
     * @return option description
     */
    String getOptionDescription();

    /**
     * Gets the longest name for this option
     * @return longest name
     */
    @Nullable
    default String getLongName() {
        return getNames().stream()
                .filter(name -> name.length() > 1)
                .min(String::compareTo)
                .orElse(null);
    }

    /**
     * Gets the short name for this option
     * @return character representing the short name
     */
    default Character getShortName() {
        return getNames().stream()
                .filter(name -> name.length() == 1)
                .map(SafeConverter::toCharacter)
                .min(Character::compare)
                .orElse(Character.MIN_VALUE);
    }

    /**
     * If this option has no argument, then it will be a flag
     * @return false if option is not required or optional
     */
    default boolean isFlag() {
        return !isRequired() && !isOptional();
    }

    default boolean contains(@CaseSensitive final Collection<String> names) {
        return getNames().stream().anyMatch(name -> names.stream().anyMatch(name::equals));
    }
    default boolean contains(@CaseSensitive final String... names) {
        return contains(Arrays.asList(names));
    }

    default boolean matches(IOption<?> option) {
        return contains(option.getNames());
    }
}
