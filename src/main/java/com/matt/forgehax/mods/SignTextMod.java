package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.math.AlignHelper.Align;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.command.Setting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.datatransfer.StringSelection;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Mouse;

/**
 * Created by Babbaj on 9/16/2017.
 */
@RegisterMod
public class SignTextMod extends HudMod {

  public SignTextMod() {
    super(Category.WORLD, "SignText", false, "Display sign text, copy it with middleclick");
  }

  private final Setting<Boolean> display =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("display")
          .description("Show pointed sign text on screen")
          .defaultTo(true)
          .build();

  static final Align alignment = Align.TOP;

  @Override
  protected Align getDefaultAlignment() { return Align.TOP; }

  @Override
  protected int getDefaultOffsetX() { return 0; }

  @Override
  protected int getDefaultOffsetY() { return 10; }

  @Override
  protected double getDefaultScale() { return 1d; }

  private final List<String> text = new ArrayList<>();
  private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    if (!display.get() || text.isEmpty()) {
      return;
    }

    MC.getTextureManager().bindTexture(SIGN_TEXTURE);

    SurfaceHelper.drawTexturedRect(getPosX(-50), getPosY(-5), 0, 20, 100, 45, 500);

    int align = alignment.ordinal();

    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.BLACK.toBuffer(), scale.get(), false, align);
  }

  @SubscribeEvent
  public void onInput(MouseEvent event) {
    RayTraceResult result = MC.player.rayTrace(999, 0);
    text.clear();

    if (result == null) {
      return;
    }

    if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
      TileEntity tileEntity = MC.world.getTileEntity(result.getBlockPos());

      if (tileEntity instanceof TileEntitySign) {
        TileEntitySign sign = (TileEntitySign) tileEntity;

        int signTextLength = 0;
        // find the first line from the bottom that isn't empty
        for (int i = 3; i >= 0; i--) {
          if (!sign.signText[i].getUnformattedText().isEmpty()) {
            signTextLength = i + 1;
            break;
          }
        }
        if (signTextLength == 0) {
          return; // if the sign is empty don't do anything
        }

        for (int i = 0; i < signTextLength; i++)
          text.add(sign.signText[i].getFormattedText().replace(TextFormatting.RESET.toString(), ""));

        if (event.getButton() == 2 && Mouse.getEventButtonState()) { // on middle click
          String fullText = String.join("\n", text);
          Helper.printMessage("Copied sign");
          setClipboardString(fullText);
        }
      }
    }
  }

  private static void setClipboardString(String stringIn) {
    StringSelection selection = new StringSelection(stringIn);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
  }
}
