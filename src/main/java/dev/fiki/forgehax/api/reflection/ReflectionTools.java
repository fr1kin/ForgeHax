package dev.fiki.forgehax.api.reflection;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPlayerPacket;

import java.util.Map;

@RequiredArgsConstructor
public class ReflectionTools {
  @Getter
  private static ReflectionTools instance;

  {
    instance = this;
  }

  //

  @MapField(parentClass = CPlayerPacket.class, value = "y")
  public final ReflectionField<Double> CPacketPlayer_y;
  @MapField(parentClass = CPlayerPacket.class, value = "onGround")
  public final ReflectionField<Boolean> CPacketPlayer_onGround;

  //

  @MapField(parentClass = Entity.class, value = "onGround")
  public final ReflectionField<Boolean> Entity_onGround;

  //

  @MapField(parentClass = Minecraft.class, value = "leftClickCounter")
  public final ReflectionField<Integer> Minecraft_leftClickCounter;
  @MapField(parentClass = Minecraft.class, value = "rightClickDelayTimer")
  public final ReflectionField<Integer> Minecraft_rightClickDelayTimer;

  //

  @MapField(parentClass = BufferBuilder.class, value = "drawMode")
  public final ReflectionField<Integer> BufferBuilder_drawMode;

  //

  @MapField(parentClass = KeyBinding.class, value = "pressTime")
  public final ReflectionField<Integer> KeyBinding_pressTime;
  @MapField(parentClass = KeyBinding.class, value = "pressed")
  public final ReflectionField<Boolean> KeyBinding_pressed;

  //

  @MapField(parentClass = InputMappings.Input.class, value = "REGISTRY")
  public final ReflectionField<Map<String, InputMappings.Input>> InputMappings_Input_REGISTRY;

}
