package com.matt.forgehax.mods.services;

import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class ForgeHaxService extends ServiceMod {

  public ForgeHaxService() {
    super("ForgeHaxSettings");
    INSTANCE = this;
  }

  public static ForgeHaxService INSTANCE;

  public final Setting<String> watermarkText =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("watermark-text")
          .description("What the watermark should say")
          .defaultTo("ForgeHax")
          .build();

  public final Setting<Integer> watermarkScale =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("watermark-scale")
          .description("Watermark text size")
          .defaultTo(2)
          .min(1)
          .build();

  public final Setting<Integer> red =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("red-watermark")
          .description("Red value (RGB)")
          .min(0)
          .max(255)
          .defaultTo(255)
          .build();

  public final Setting<Integer> green =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("green-watermark")
          .description("Green value (RGB)")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();

  public final Setting<Integer> blue =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("blue-watermark")
          .description("Blue value (RGB)")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();

  public final Setting<Integer> alpha =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("alpha-watermark")
          .description("Alpha value (RGB)")
          .defaultTo(255)
          .min(0)
          .max(255)
          .build();

  public final Setting<Boolean> toggleMsgs =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("toggle-msgs")
          .description("Enables toggle messages in chat")
          .defaultTo(true)
          .build();

  public String getWatermark() {
    return watermarkText.get();
  }

  public void drawWatermark(final int posX, final int posY, final int align) {
    SurfaceHelper.drawTextAlign(getWatermark(), posX, posY, Color.of(red.get(), green.get(), blue.get(), alpha.get()).toBuffer(),
        watermarkScale.get(), true, align);
  }
}
