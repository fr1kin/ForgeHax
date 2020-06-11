package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import java.text.SimpleDateFormat;
import java.util.Date;

@RegisterMod
public class SystemTime extends ToggleMod {

  public SystemTime() {
    super(Category.GUI, "SystemTime", false, "Shows the time from your operating system");
  }

  private final Setting<String> dateFormat =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("date-format")
          .description("Date format")
          .defaultTo("d/MM/yyyy, HH:mm:ss")
          .build();

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  public String getInfoDisplayText() {
    return new SimpleDateFormat(dateFormat.get()).format(new Date());
  }
}
