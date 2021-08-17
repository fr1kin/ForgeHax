package dev.fiki.forgehax.main.mods.render;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.cmd.AbstractParentCommand;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.ColorSetting;
import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.draw.RenderTypeEx;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.entity.EnchantmentUtils;
import dev.fiki.forgehax.api.entity.EnchantmentUtils.ItemEnchantment;
import dev.fiki.forgehax.api.entity.RelationState;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.NametagRenderEvent;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.math.ScreenPos;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "ESP",
    description = "Shows entity locations and info",
    category = Category.RENDER
)
@ExtensionMethod({EntityEx.class, VectorEx.class, VertexBuilderEx.class})
public class ESP extends ToggleMod {
  private static final int HEALTHBAR_WIDTH = 15;
  private static final int HEALTHBAR_HEIGHT = 3;

  private final DrawingSetting playerOptions = newDrawingSetting()
      .name("players")
      .description("Player draw options")
      .targetRelation(RelationState.PLAYER)
      .color(Colors.GREEN)
      .nameTag(true)
      .health(DrawingSetting.HealthDisplayOption.BAR)
      .armor(DrawingSetting.ArmorDisplayOption.FULL)
      .box(true)
      .build();

  private final DrawingSetting hostileOptions = newDrawingSetting()
      .name("hostile")
      .description("Hostile draw options")
      .targetRelation(RelationState.HOSTILE)
      .color(Colors.ORANGE)
      .build();

  private final DrawingSetting friendlyOptions = newDrawingSetting()
      .name("friendly")
      .description("Friendly draw options")
      .targetRelation(RelationState.FRIENDLY)
      .color(Colors.BLUE)
      .build();

  private Color getColorLevel(float scale) {
    return Color.of((int) ((255 - scale) * 255), (int) (255 * scale), 0);
  }

  private DrawingSetting getDrawingSetting(RelationState relationState) {
    switch (relationState) {
      case PLAYER:
        return playerOptions;
      case FRIENDLY:
        return friendlyOptions;
      case HOSTILE:
        return hostileOptions;
      default:
        return null;
    }
  }

  @SubscribeListener
  public void onRenderPlayerNameTag(NametagRenderEvent event) {
    if (event.getEntity().isPlayerType()) {
      event.setCanceled(true);
    }
  }

  @SubscribeListener(priority = PriorityEnum.LOW)
  public void onRender2D(final RenderPlaneEvent.Back event) {
    final float partialTicks = event.getPartialTicks();
    final double screenWidth = event.getScreenWidth();
    final double screenHeight = event.getScreenHeight();

    val buffers = getBufferProvider().getBufferSource();
    val triangles = getBufferProvider().getBuffer(RenderTypeEx.glTriangle());
    val stack = event.getStack();

    val selfEquipmentList = new EquipmentList(getLocalPlayer());

    for (Entity ent : getWorld().entitiesForRendering()) {
      if (ent.showVehicleHealth()
          && !ent.isLocalPlayer()
          && ent.isReallyAlive()
          && ent.isValidEntity()) {
        LivingEntity living = (LivingEntity) ent;

        final DrawingSetting settings = getDrawingSetting(living.getPlayerRelationship());

        // no option exists
        if (settings == null || !settings.getEnabled().getValue()) {
          continue;
        }

        final Vector3d bottomPos = living.getInterpolatedPos(partialTicks);
        final Vector3d topPos = bottomPos.add(0.D, living.getBoundingBox().maxY - living.getY(), 0.D);

        final ScreenPos top = topPos.toScreen();
        final ScreenPos bot = bottomPos.toScreen();

        // stop here if neither are visible
        if (!top.isVisible() && !bot.isVisible()) {
          continue;
        }

        double topX = top.getX();
        double topY = top.getY() + 1.D;
        double botX = bot.getX();
        double botY = bot.getY() + 1.D;
        double height = (bot.getY() - top.getY());
        double width = height;

        float offsetY = 1.f;
        float textScale = 1.f;

        if (settings.isHealthEnabled()) {
          float hp = MathHelper.clamp(living.getHealth(), 0, living.getMaxHealth()) / living.getMaxHealth();
          Color color = (living.getHealth() + living.getAbsorptionAmount() > living.getMaxHealth())
              // if above 20 hp bar is yellow
              ? Colors.YELLOW
              // turn red as the bar goes down
              : getColorLevel(hp);

          switch (settings.getHealth().getValue()) {
            case TEXT: {
              stack.pushPose();
              stack.translate(topX, topY - offsetY - 1, 0.f);
              String text = Math.round(hp * 100.f) + "%";

              float x = (float) (SurfaceHelper.getStringWidth(text) / 2.f);
              float y = (float) SurfaceHelper.getStringHeight();

              stack.scale(textScale, textScale, 0.f);
              stack.translate(-x, -y, 0.d);

              SurfaceHelper.renderString(buffers, stack.last().pose(),
                  text, 0, 0, color, true);

              offsetY += SurfaceHelper.getStringHeight() + 1.f;
              stack.popPose();
              break;
            }
            case BAR: {
              final float barHeight = 4;
              float barWidth = (float) Math.max(width, 14.f);

              float x = (float) (topX - (barWidth / 2.f));
              float y = (float) (topY - barHeight - 1.f) - offsetY;

              float barRemaining = Math.round(((1.f - hp) * (barWidth - 3)));

              triangles.rect(GL11.GL_TRIANGLES, x, y, barWidth, barHeight, Colors.BLACK, stack.getLastMatrix());
              triangles.rect(GL11.GL_TRIANGLES,
                  x + 1 + barRemaining, y + 1,
                  barWidth - 2 - barRemaining, barHeight - 2,
                  color, stack.getLastMatrix());

              offsetY += barHeight + 1.f;
              break;
            }
          }
        }

        if (settings.isNametagEnabled()) {
          stack.pushPose();
          stack.translate(topX, topY - offsetY - 1, 0.f);

          String name = living.getName().getString();
          if (name == null || name.isEmpty()) {
            name = living.getScoreboardName();
          }

          float x = (float) (SurfaceHelper.getStringWidth(name) / 2.f);
          float y = (float) SurfaceHelper.getStringHeight();

          stack.scale(textScale, textScale, 0.f);
          stack.translate(-x, -y, 0.d);

          SurfaceHelper.renderString(buffers, stack.last().pose(),
              name, 0, 0, Colors.WHITE, true);

          offsetY += SurfaceHelper.getStringHeight() + 2.f;
          stack.popPose();
        }

        if (settings.isArmorEnabled()) {
          final int itemSize = 12;

          stack.pushPose();

          EquipmentList equipmentList = new EquipmentList(living);

          float x = (float) (topX - (equipmentList.getRenderCount() * itemSize / 2.f));
          float y = (float) (topY - itemSize - offsetY);

          stack.translate(x, y, 0.f);

          for (EquipmentList.Equipment equipment : equipmentList) {
            if (!equipment.shouldRender()) {
              // null or empty item
              continue;
            }

            val itemStack = equipment.getItemStack();

            stack.pushPose();
            stack.translate(equipment.getRenderOffset() * itemSize, 0.f, 0.f);

            List<ItemEnchantment> enchantments = equipment.getEnchantments();
            if (!enchantments.isEmpty()) {
              final float enchantTextScale = 0.5f;

              // get the local players equivalent
              val selfEquipment = selfEquipmentList.getByType(equipment.getType());

              stack.pushPose();

              stack.translate(0.f, -1.f, 0.f);
              stack.scale(enchantTextScale, enchantTextScale, 0.f);
              stack.translate(0.f, -enchantments.size() * SurfaceHelper.getStringHeight(), 0.f);

              enchantments.sort(null);
              int j = 0;
              for (ItemEnchantment enchantment : enchantments) {
                stack.pushPose();
                stack.translate(1f, j++ * SurfaceHelper.getStringHeight(), -50.f);

                // render enchantment short name
                SurfaceHelper.renderString(buffers, stack.last().pose(),
                    enchantment.getShortName(), 0, 0, Colors.WHITE, true);

                if (enchantment.isMultiLevel()) {
                  // render level
                  String level = String.valueOf(enchantment.getLevel());
                  stack.translate(itemSize + SurfaceHelper.getStringWidth(level) - 2f, 0.f, 0.f);

                  Color color = Colors.ORANGE;

                  val selfEnchantment = selfEquipment.getEnchantmentById(enchantment.getEnchantment());
                  if (selfEnchantment != null) {
                    int cmp = enchantment.getLevel() - selfEnchantment.getLevel();
                    if (cmp == 0) {
                      color = Colors.CYAN;
                    } else if (cmp < 0) {
                      color = Colors.GREEN;
                    } else {
                      color = Colors.ORANGE;
                    }
                  }

                  SurfaceHelper.renderString(buffers, stack.last().pose(),
                      level, 0, 0, color, true);
                }

                stack.popPose();
              }
              stack.popPose();
            }

            if (itemStack.getItem().showDurabilityBar(itemStack)) {
              stack.pushPose();
              float dur = (float) (itemStack.getMaxDamage() - itemStack.getDamageValue()) / (float) itemStack.getMaxDamage();
              String text = Math.round(dur * 100.f) + "%";

              stack.scale(0.5f, 0.5f, 150);

              SurfaceHelper.renderString(buffers, stack.last().pose(),
                  text, 0, 0, getColorLevel(dur), true);
              stack.popPose();
            }

            SurfaceHelper.renderItemInGui(itemStack, stack, MC.renderBuffers().bufferSource());

            stack.popPose();
          }

          stack.popPose();
        }

        if (settings.isBoxEnabled()) {
          float x = (float) (topX - (width / 2.d));
          float y = (float) topY;

          triangles.outlinedRect(GL11.GL_TRIANGLES,
              x - 1, y - 1,
              width + 2, height + 2,
              3, Colors.BLACK, stack.getLastMatrix());
          triangles.outlinedRect(GL11.GL_TRIANGLES,
              x, y, width, height,
              1, settings.getColor().getValue(), stack.getLastMatrix());
        }
      }
    }

    //

    MC.getTextureManager().bind(PlayerContainer.BLOCK_ATLAS);
    MC.getTextureManager().getTexture(PlayerContainer.BLOCK_ATLAS).setBlurMipmap(false, false);

    RenderHelper.turnOff();
    RenderHelper.setupForFlatItems();

    buffers.endBatch(RenderTypeEx.blockTranslucentCull());
    buffers.endBatch(RenderTypeEx.blockCutout());
    buffers.endBatch(RenderType.glint());
    buffers.endBatch(RenderType.entityGlint());
    MC.renderBuffers().bufferSource().endBatch();
    buffers.endBatch();

    RenderHelper.setupFor3DItems();
//    RenderHelper.enableStandardItemLighting();
  }

  private DrawingSetting.DrawingSettingBuilder newDrawingSetting() {
    return DrawingSetting.builder().parent(this);
  }

  @Getter
  private static class DrawingSetting extends AbstractParentCommand {

    enum HealthDisplayOption {
      OFF,
      TEXT,
      BAR,
      ;
    }

    enum ArmorDisplayOption {
      OFF,
      FULL,
      LITE;
    }

    final EnumSet<RelationState> relations;

    final BooleanSetting enabled;
    final BooleanSetting nametag;
    final EnumSetting<HealthDisplayOption> health;
    final BooleanSetting distance;
    final EnumSetting<ArmorDisplayOption> armor;
    final BooleanSetting box;
    final ColorSetting color;

    @Builder
    public DrawingSetting(IParentCommand parent,
        String name, @Singular Collection<String> aliases, String description,
        @Singular Collection<EnumFlag> flags,
        @Singular Set<RelationState> targetRelations,
        Boolean enabled, Boolean nameTag, HealthDisplayOption health, Boolean distance,
        ArmorDisplayOption armor,
        Boolean box, Color color) {
      super(parent, name, aliases, description, flags);
      this.relations = EnumSet.noneOf(RelationState.class);
      this.relations.addAll(targetRelations);

      this.enabled = newBooleanSetting()
          .name("enabled")
          .description("Enables rendering")
          .defaultTo(MoreObjects.firstNonNull(enabled, true))
          .build();

      this.nametag = newBooleanSetting()
          .name("name")
          .description("Renders name tag")
          .defaultTo(MoreObjects.firstNonNull(nameTag, true))
          .build();

      this.health = newEnumSetting(HealthDisplayOption.class)
          .name("health")
          .description("Render health in text or bar")
          .defaultTo(MoreObjects.firstNonNull(health, HealthDisplayOption.OFF))
          .build();

      this.distance = newBooleanSetting()
          .name("distance")
          .description("Render distance from entity to local player")
          .defaultTo(MoreObjects.firstNonNull(distance, false))
          .build();

      this.armor = newEnumSetting(ArmorDisplayOption.class)
          .name("armor")
          .description("Render armor wearing and items holding")
          .defaultTo(MoreObjects.firstNonNull(armor, ArmorDisplayOption.OFF))
          .build();

      this.box = newBooleanSetting()
          .name("box")
          .description("Render a bounding box")
          .defaultTo(MoreObjects.firstNonNull(box, false))
          .build();

      this.color = newColorSetting()
          .name("color")
          .description("Unique render color")
          .defaultTo(MoreObjects.firstNonNull(color, Colors.RED))
          .build();

      onFullyConstructed();
    }

    public boolean isNametagEnabled() {
      return nametag.getValue();
    }

    public boolean isHealthEnabled() {
      return !HealthDisplayOption.OFF.equals(health.getValue());
    }

    public boolean isBoxEnabled() {
      return box.getValue();
    }

    public boolean isArmorEnabled() {
      return !ArmorDisplayOption.OFF.equals(armor.getValue());
    }
  }

  @Getter
  private static class EquipmentList implements Iterable<EquipmentList.Equipment> {
    enum EquipmentType {
      MAIN_HAND,
      HELMET,
      CHEST,
      LEGGINGS,
      BOOTS,
      OFF_HAND,
      UNKNOWN;
    }

    private final List<Equipment> equipment = Lists.newArrayListWithCapacity(6);
    private int renderCount;

    EquipmentList(LivingEntity living) {
      // main hand
      add(EquipmentType.MAIN_HAND, living.getMainHandItem());

      // add armor
      List<ItemStack> armors = Lists.newArrayList(living.getArmorSlots());
      Collections.reverse(armors);
      for (ItemStack stack : armors) {
        if (stack.getItem() instanceof ArmorItem) {
          ArmorItem armorItem = (ArmorItem) stack.getItem();
          switch (armorItem.getSlot()) {
            case HEAD:
              add(EquipmentType.HELMET, stack);
              break;
            case CHEST:
              add(EquipmentType.CHEST, stack);
              break;
            case LEGS:
              add(EquipmentType.LEGGINGS, stack);
              break;
            case FEET:
              add(EquipmentType.BOOTS, stack);
              break;
            default:
              add(EquipmentType.UNKNOWN, stack);
          }
        } else {
          add(EquipmentType.UNKNOWN, stack);
        }
      }

      // offhand
      add(EquipmentType.OFF_HAND, living.getOffhandItem());
    }

    private void add(EquipmentType type, ItemStack stack) {
      equipment.add(new Equipment(stack, type, stack.isEmpty() ? -1 : renderCount));

      // increment if not empty
      if (!stack.isEmpty()) renderCount++;
    }

    public Equipment getByType(EquipmentType type) {
      if (!EquipmentType.UNKNOWN.equals(type)) {
        for (Equipment equipment : this) {
          if (equipment.getType().equals(type)) {
            return equipment;
          }
        }
      }
      return new Equipment(ItemStack.EMPTY, EquipmentType.UNKNOWN, -1);
    }

    @Override
    public Iterator<Equipment> iterator() {
      return equipment.iterator();
    }

    @Getter
    static class Equipment {
      private final ItemStack itemStack;
      private final EquipmentType type;
      private final int renderOffset;
      private List<ItemEnchantment> enchantments;

      Equipment(ItemStack itemStack, EquipmentType type, int renderOffset) {
        this.itemStack = itemStack;
        this.type = type;
        this.renderOffset = renderOffset;
      }

      public List<ItemEnchantment> getEnchantments() {
        return enchantments == null
            ? enchantments = EnchantmentUtils.getEnchantments(itemStack)
            : enchantments;
      }

      public ItemEnchantment getEnchantmentById(Enchantment other) {
        for (ItemEnchantment enchantment : getEnchantments()) {
          if (enchantment.getEnchantment().equals(other)) {
            return enchantment;
          }
        }
        return null;
      }

      public boolean shouldRender() {
        return renderOffset > -1;
      }
    }
  }
}
