package dev.fiki.forgehax.api.reflection;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPlayerPacket;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

  @MapField(parentClass = Minecraft.class, value = "missTime")
  public final ReflectionField<Integer> Minecraft_missTime;
  @MapField(parentClass = Minecraft.class, value = "rightClickDelay")
  public final ReflectionField<Integer> Minecraft_rightClickDelay;

  //

  @MapField(parentClass = BufferBuilder.class, value = "mode")
  public final ReflectionField<Integer> BufferBuilder_mode;

  //

  @MapField(parentClass = KeyBinding.class, value = "clickCount")
  public final ReflectionField<Integer> KeyBinding_clickCount;
  @MapField(parentClass = KeyBinding.class, value = "isDown")
  public final ReflectionField<Boolean> KeyBinding_isDown;

  //

  @MapField(parentClass = InputMappings.Input.class, value = "NAME_MAP")
  public final ReflectionField<Map<String, InputMappings.Input>> InputMappings_Input_NAME_MAP;

  //

  @MapField(parentClass = IRenderTypeBuffer.Impl.class, value = "builder")
  public final ReflectionField<BufferBuilder> IRenderTypeBuffer$Impl_builder;
  @MapField(parentClass = IRenderTypeBuffer.Impl.class, value = "fixedBuffers")
  public final ReflectionField<Map<RenderType, BufferBuilder>> IRenderTypeBuffer$Impl_fixedBuffers;
  @MapField(parentClass = IRenderTypeBuffer.Impl.class, value = "lastState")
  public final ReflectionField<Optional<RenderType>> IRenderTypeBuffer$Impl_lastState;
  @MapField(parentClass = IRenderTypeBuffer.Impl.class, value = "startedBuffers")
  public final ReflectionField<Set<BufferBuilder>> IRenderTypeBuffer$Impl_startedBuffers;

  //

  @MapField(parentClass = RenderType.class, value = "sortOnUpload")
  public final ReflectionField<Boolean> RenderType_sortOnUpload;
}
