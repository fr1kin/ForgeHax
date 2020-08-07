package dev.fiki.forgehax.main.util.reflection;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mapper.MappingScan;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPlayerPacket;

import java.util.Map;

@MappingScan
@RequiredArgsConstructor
public class ReflectionTools {
  @Getter
  private static ReflectionTools instance;

  {
    instance = this;
  }

  //

  @FieldMapping(parentClass = CPlayerPacket.class, value = "y")
  public final ReflectionField<Double> CPacketPlayer_y;
  @FieldMapping(parentClass = CPlayerPacket.class, value = "onGround")
  public final ReflectionField<Boolean> CPacketPlayer_onGround;

  //

  @FieldMapping(parentClass = Entity.class, value = "onGround")
  public final ReflectionField<Boolean> Entity_onGround;

  //

  @FieldMapping(parentClass = Minecraft.class, value = "leftClickCounter")
  public final ReflectionField<Integer> Minecraft_leftClickCounter;
  @FieldMapping(parentClass = Minecraft.class, value = "rightClickDelayTimer")
  public final ReflectionField<Integer> Minecraft_rightClickDelayTimer;

  //

  @FieldMapping(parentClass = BufferBuilder.class, value = "drawMode")
  public final ReflectionField<Integer> BufferBuilder_drawMode;

  //

  @FieldMapping(parentClass = KeyBinding.class, value = "pressTime")
  public final ReflectionField<Integer> KeyBinding_pressTime;
  @FieldMapping(parentClass = KeyBinding.class, value = "pressed")
  public final ReflectionField<Boolean> KeyBinding_pressed;

  //

  @FieldMapping(parentClass = InputMappings.Input.class, value = "REGISTRY")
  public final ReflectionField<Map<String, InputMappings.Input>> InputMappings_Input_REGISTRY;

}
