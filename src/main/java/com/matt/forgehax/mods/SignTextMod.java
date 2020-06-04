package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.math.AlignHelper.Align;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.command.Setting;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.awt.datatransfer.StringSelection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Mouse;

/**
 * Created by Babbaj on 9/16/2017.
 */
@RegisterMod
public class SignTextMod extends HudMod {

  private final Setting<Boolean> display =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("display")
          .description("Show pointed sign text on screen")
          .defaultTo(true)
          .build();
  
  public SignTextMod() {
    super(Category.WORLD, "SignText", false, "Display sign text, copy it with middleclick");
  }

  @Override
  protected Align getDefaultAlignment() { return Align.TOPLEFT; }
  @Override
  protected int getDefaultOffsetX() { return 200; }
  @Override
  protected int getDefaultOffsetY() { return 20; }
  @Override
  protected double getDefaultScale() { return 1d; }

  private int posY;
  private List<String> text = new ArrayList<>();
  
  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
	if (!display.get()) return;
    int align = alignment.get().ordinal();
    // Shift up when chat is open && alignment is at bottom
    if (alignment.get().toString().startsWith("BOTTOM") && MC.currentScreen instanceof GuiChat) {
      posY = getPosY(offsetY.get() + 15);
    } else {
      posY = getPosY(offsetY.get() + 0);
    }

    SurfaceHelper.drawTextAlign(text, getPosX(0), posY,
        Colors.WHITE.toBuffer(), scale.get(), true, align);
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
