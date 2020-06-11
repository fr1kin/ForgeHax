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

  private final Setting<Boolean> showTime =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("time")
      .description("Show time")
      .defaultTo(true)
      .build();

  private final Setting<Boolean> showDate =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("date")
      .description("Show date")
      .defaultTo(true)
      .build();

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  @Override
  public boolean notInList() {
	return true;
  }

  public String getInfoDisplayText() {
    StringBuilder builderTime = new StringBuilder();

    if(showDate.getAsBoolean()) {
      final String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
      builderTime.append(String.format("%s", date));
    }

    if (showTime.getAsBoolean()) {
      if (showDate.getAsBoolean()) {
        builderTime.append(", ");
      }
      
      final String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
      builderTime.append(String.format("%s", time));
    }

    return builderTime.toString();
  }
}
