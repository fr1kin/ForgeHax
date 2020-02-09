package dev.fiki.forgehax.common.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
public class ProjectionViewMatrixSetupEvent extends Event {
  final MatrixStack matrixStack;
  final Matrix4f projectionMatrix;
}
