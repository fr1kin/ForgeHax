package dev.fiki.forgehax.main.mods;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.util.cmd.settings.StringSetting;
import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.entity.EnchantmentUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.function.Predicate;

@RegisterMod
public class Highlighter extends ToggleMod {
  private final StringSetting find = newStringSetting()
      .name("find")
      .description("Highlight any item matching this string")
      .defaultTo("")
      .build();

  public Highlighter() {
    super(Category.MISC, "Highlighter", false, "Highlight container contents");
  }

  private boolean isEnchanted(ItemStack stack) {
    return Items.ENCHANTED_BOOK.equals(stack.getItem()) || stack.isEnchanted();
  }

  private ListNBT getEnchantmentNBT(ItemStack stack) {
    return Items.ENCHANTED_BOOK.equals(stack.getItem())
        ? EnchantedBookItem.getEnchantments(stack)
        : stack.getEnchantmentTagList();
  }

  private boolean shouldHighlight(ItemStack stack, Predicate<String> matcher) {
    if (stack.isEmpty()) {
      return false;
    } else if (matcher.test(stack.getDisplayName().getUnformattedComponentText())) {
      return true;
    } else if (isEnchanted(stack) &&
        EnchantmentUtils.getEnchantments(getEnchantmentNBT(stack)).stream()
            .map(en -> en.getEnchantment().getDisplayName(en.getLevel()))
            .map(ITextComponent::getUnformattedComponentText)
            .anyMatch(matcher)) {
      return true;
    }
    return false; // default case
  }

  @SubscribeEvent
  public void onGuiContainerDrawEvent(GuiContainerEvent.DrawBackground event) {
    RenderSystem.pushMatrix();
    RenderSystem.enableDepthTest();
    RenderSystem.translatef(event.getGuiContainer().getGuiLeft(), event.getGuiContainer().getGuiTop(), 0);

    final String matching = find.getValue().toLowerCase();
    for (Slot slot : event.getGuiContainer().getContainer().inventorySlots) {
      ItemStack stack = slot.getStack();
      if (shouldHighlight(stack, str -> str.toLowerCase().contains(matching))) {
        GuiUtils.drawGradientRect(0,
            slot.xPos, slot.yPos,
            slot.xPos + 16, slot.yPos + 16,
            Color.of(218,165,32, 200).toBuffer(),
            Color.of(189,183,107, 200).toBuffer());
      }
    }

    RenderSystem.popMatrix();
  }
}
