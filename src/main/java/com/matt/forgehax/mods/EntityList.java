package com.matt.forgehax.mods;

import java.util.stream.Collectors;

import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ListMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.entity.item.EntityEnderCrystal;

import java.util.*;

import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.util.draw.SurfaceHelper.getTextHeight;

@RegisterMod
public class EntityList extends ListMod {

  public EntityList() {
    super(Category.GUI, "EntityList", false, "Displays a list of all rendered entities");
  }

  private final Setting<Boolean> items =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("items")
      .description("Include non-living entities")
      .defaultTo(true)
      .build();

  private final Setting<Boolean> animate =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("animate")
      .description("Add entities to screen one at a time")
      .defaultTo(true)
      .build();

  private final Setting<Boolean> players =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("players")
      .description("Include players")
      .defaultTo(false)
      .build();

  @Override
  protected AlignHelper.Align getDefaultAlignment() {
    return AlignHelper.Align.TOPLEFT;
  }

  @Override
  protected int getDefaultOffsetX() { return 100; }

  @Override
  protected int getDefaultOffsetY() { return 1; }

  @Override
  protected double getDefaultScale() { return 0.5d; }

  @Override
  public boolean isInfoDisplayElement() { return false; }

  @Override
  public String getDisplayText() {
    return (getModName() + " [" + count + "]");
  }

  private int count = 0, max_len = 0;

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    if (!MC.gameSettings.showDebugInfo) {
      int align = alignment.get().ordinal();
      List<String> entityList = new ArrayList<>();
	    List<String> text = new ArrayList<>();

      getWorld()
        .loadedEntityList
        .stream()
        .filter(e -> items.get() || EntityUtils.isLiving(e))
        .filter(e -> items.get() || EntityUtils.isAlive(e))
        .filter(e -> players.get() || !EntityUtils.isPlayer(e))
        .filter(e -> !Objects.equals(getLocalPlayer(), e) && !EntityUtils.isFakeLocalPlayer(e))
        .filter(EntityUtils::isValidEntity)
        .map(entity -> {
		  if (entity instanceof EntityItem)
            return ((EntityItem) entity).getItem().getDisplayName();
          else
            return entity.getDisplayName().getUnformattedText();
        })
        .forEach(name -> entityList.add(name));

	    String buf = "";
      int num = 0;
      count = entityList.size();
	    for (String element : entityList.stream().distinct().collect(Collectors.toList())) {
		    buf = String.format("%s", element);
		    num = Collections.frequency(entityList, element);
		    if (num > 1) buf += String.format(" (x%d)", num);
		    text.add(appendArrow(buf));
        if (animate.get() && text.size() >= (max_len + 1)) break;
	    }
      max_len = text.size();

      text.sort(sortMode.get().getComparator());

      // Prints on screen
      SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
    }
  }
}
