package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.entity.EnchantmentUtils;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.GuiContainerRenderEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.function.Predicate;

@RegisterMod(
    name = "Highlighter",
    description = "Highlight container contents",
    category = Category.RENDER
)
public class Highlighter extends ToggleMod {
  private final StringSetting find = newStringSetting()
      .name("find")
      .description("Highlight any item matching this string")
      .defaultTo("")
      .build();

  private boolean isEnchanted(ItemStack stack) {
    return Items.ENCHANTED_BOOK.equals(stack.getItem()) || stack.isEnchanted();
  }

  private ListNBT getEnchantmentNBT(ItemStack stack) {
    return Items.ENCHANTED_BOOK.equals(stack.getItem())
        ? EnchantedBookItem.getEnchantments(stack)
        : stack.getEnchantmentTags();
  }

  private boolean shouldHighlight(ItemStack stack, Predicate<String> matcher) {
    if (stack.isEmpty()) {
      return false;
    } else if (matcher.test(stack.getDisplayName().getString())) {
      return true;
    } else if (isEnchanted(stack) &&
        EnchantmentUtils.getEnchantments(getEnchantmentNBT(stack)).stream()
            .map(en -> en.getEnchantment().getFullname(en.getLevel()))
            .map(ITextComponent::getString)
            .anyMatch(matcher)) {
      return true;
    }
    return false; // default case
  }

  @SubscribeListener
  public void onGuiContainerDrawEvent(GuiContainerRenderEvent.Background event) {
    RenderSystem.enableDepthTest();

    event.getStack().pushPose();
    event.getStack().translate(event.getContainerScreen().getGuiLeft(), event.getContainerScreen().getGuiTop(), 0);

    final String matching = find.getValue().toLowerCase();
    for (Slot slot : event.getContainerScreen().getMenu().slots) {
      ItemStack stack = slot.getItem();
      if (shouldHighlight(stack, str -> str.toLowerCase().contains(matching))) {
        GuiUtils.drawGradientRect(event.getStack().last().pose(), 0,
            slot.x, slot.y,
            slot.x + 16, slot.y + 16,
            Color.of(218, 165, 32, 200).toBuffer(),
            Color.of(189, 183, 107, 200).toBuffer());
      }
    }

    event.getStack().popPose();
  }
}
