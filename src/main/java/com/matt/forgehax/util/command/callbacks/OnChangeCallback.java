package com.matt.forgehax.util.command.callbacks;

import com.matt.forgehax.util.command.Command;

/**
 * Created on 6/8/2017 by fr1kin
 */
public class OnChangeCallback<E> extends CancelableCallbackData {
  
  private final E from;
  private final E to;
  
  public OnChangeCallback(Command command, E from, E to) {
    super(command);
    this.from = from;
    this.to = to;
  }
  
  public E getFrom() {
    return from;
  }
  
  public E getTo() {
    return to;
  }
}
