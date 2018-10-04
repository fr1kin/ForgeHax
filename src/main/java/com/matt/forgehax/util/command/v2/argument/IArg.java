package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.converter.IConverter;
import javax.annotation.Nullable;

public interface IArg<E> extends IConverter<E> {
  /**
   * Description of the command
   *
   * @return description
   */
  String getDescription();

  /**
   * A shorter description (one word)
   *
   * @return short desc
   */
  String getShortDescription();

  /**
   * If the argument is required
   *
   * @return required
   */
  boolean isRequired();

  /**
   * If the argument is optional
   *
   * @return optional
   */
  boolean isOptional();

  /**
   * The default value for this argument
   *
   * @return default value
   */
  @Nullable
  E getDefaultValue();
}
