package com.matt.forgehax.util.command.callbacks;

import com.matt.forgehax.util.command.Command;

/** Created on 6/8/2017 by fr1kin */
public class CancelableCallbackData extends CallbackData {
  private boolean canceled = false;

  public CancelableCallbackData(Command command) {
    super(command);
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  public void cancel() {
    setCanceled(true);
  }
}
