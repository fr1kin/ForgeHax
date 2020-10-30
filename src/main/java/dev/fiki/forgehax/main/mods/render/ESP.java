package dev.fiki.forgehax.main.mods.render;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.util.cmd.AbstractParentCommand;
import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.ColorSetting;
import dev.fiki.forgehax.main.util.cmd.settings.EnumSetting;
import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.draw.RenderTypeEx;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.draw.font.Fonts;
import dev.fiki.forgehax.main.util.entity.EnchantmentUtils;
import dev.fiki.forgehax.main.util.entity.EnchantmentUtils.ItemEnchantment;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import dev.fiki.forgehax.main.util.events.Render2DEvent;
import dev.fiki.forgehax.main.util.math.ScreenPos;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "ESP",
    description = "Shows entity locations and info",
    category = Category.RENDER
)
public class ESP extends ToggleMod implements Fonts {

  private static final int HEALTHBAR_WIDTH = 15;
  private static final int HEALTHBAR_HEIGHT = 3;

  private final Map<RelationState, DrawingSetting> settingCache = Maps.newHashMap();

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

  @Override
  public boolean addChild(ICommand command) {
    boolean ret = super.addChild(command);

    if (ret && command instanceof DrawingSetting) {
      DrawingSetting setting = (DrawingSetting) command;
      for (RelationState state : setting.getRelations()) {
        DrawingSetting prev = settingCache.put(state, setting);
        if (prev != null) {
          getLogger().warn("Drawing option {} overwrote option {} state {}", setting.getName(),
              state.name(), prev.getName());
        }
      }
    }

    return ret;
  }

  @Override
  public boolean deleteChild(ICommand command) {
    boolean ret = super.deleteChild(command);

    if (ret && command instanceof DrawingSetting) {
      DrawingSetting setting = (DrawingSetting) command;
      for (RelationState state : setting.getRelations()) {
        settingCache.remove(state, setting);
      }
    }

    return ret;
  }

  @SubscribeEvent
  public void onRenderPlayerNameTag(RenderNameplateEvent event) {
    if (EntityUtils.isPlayer(event.getEntity())) {
      event.setResult(Event.Result.DENY);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRender2D(final Render2DEvent event) {
    final float partialTicks = event.getPartialTicks();
    final double screenWidth = event.getScreenWidth();
    final double screenHeight = event.getScreenHeight();

    final IRenderTypeBuffer.Impl buffers = getBufferProvider().getBufferSource();
    final BufferBuilderEx triangles = getBufferProvider().getBuffer(RenderTypeEx.glTriangle());
    final MatrixStack stack = event.getMatrixStack();

    final EquipmentList selfEquipmentList = new EquipmentList(getLocalPlayer());

    worldEntities()
        .filter(EntityUtils::isLiving)
        .filter(EntityUtils::notLocalPlayer)
        .filter(EntityUtils::isAlive)
        .filter(EntityUtils::isValidEntity)
        .map(LivingEntity.class::cast)
        .forEach(living -> {
          final DrawingSetting settings = settingCache.get(EntityUtils.getRelationship(living));

          // no option exists
          if (settings == null || !settings.getEnabled().getValue()) {
            return;
          }

          Vector3d bottomPos = EntityUtils.getInterpolatedPos(living, partialTicks);
          Vector3d topPos = bottomPos.add(0.D, living.getRenderBoundingBox().maxY - living.getPosY(), 0.D);

          ScreenPos top = VectorUtils.toScreen(topPos);
          ScreenPos bot = VectorUtils.toScreen(bottomPos);

          // stop here if neither are visible
          if (!top.isVisible() && !bot.isVisible()) {
            return;
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
                stack.push();
                stack.translate(topX, topY - offsetY - 1, 0.f);
                String text = Math.round(hp * 100.f) + "%";

                float x = (float) (SurfaceHelper.getStringWidth(text) / 2.f);
                float y = (float) SurfaceHelper.getStringHeight();

                stack.scale(textScale, textScale, 0.f);
                stack.translate(-x, -y, 0.d);

                SurfaceHelper.renderString(buffers, stack.getLast().getMatrix(),
                    text, 0, 0, color, true);

                offsetY += SurfaceHelper.getStringHeight() + 1.f;
                stack.pop();
                break;
              }
              case BAR: {
                final float barHeight = 4;
                float barWidth = (float) Math.max(width, 14.f);

                float x = (float) (topX - (barWidth / 2.f));
                float y = (float) (topY - barHeight - 1.f) - offsetY;

                float barRemaining = Math.round(((1.f - hp) * (barWidth - 3)));

                triangles.putRect(x, y, barWidth, barHeight, Colors.BLACK);
                triangles.putRect(x + 1 + barRemaining, y + 1, barWidth - 2 - barRemaining, barHeight - 2, color);

                offsetY += barHeight + 1.f;
                break;
              }
            }
          }

          if (settings.isNametagEnabled()) {
            stack.push();
            stack.translate(topX, topY - offsetY - 1, 0.f);

            String name = living.getName().getString();
            if(name == null || name.isEmpty()) {
              name = living.getScoreboardName();
            }

            float x = (float) (SurfaceHelper.getStringWidth(name) / 2.f);
            float y = (float) SurfaceHelper.getStringHeight();

            stack.scale(textScale, textScale, 0.f);
            stack.translate(-x, -y, 0.d);

            SurfaceHelper.renderString(buffers, stack.getLast().getMatrix(),
                name, 0, 0, Colors.WHITE, true);

            offsetY += SurfaceHelper.getStringHeight() + 2.f;
            stack.pop();
          }

          if (settings.isArmorEnabled()) {
            final int itemSize = 12;

            stack.push();

            EquipmentList equipmentList = new EquipmentList(living);

            float x = (float) (topX - (equipmentList.getRenderCount() * itemSize / 2.f));
            float y = (float) (topY - itemSize - offsetY);

            stack.translate(x, y, 0.f);

            for(EquipmentList.Equipment equipment : equipmentList) {
              if(!equipment.shouldRender()) {
                // null or empty item
                continue;
              }

              ItemStack itemStack = equipment.getItemStack();

              stack.push();
              stack.translate(equipment.getRenderOffset() * itemSize, 0.f, 0.f);

              List<ItemEnchantment> enchantments = equipment.getEnchantments();
              if(!enchantments.isEmpty()) {
                final float enchantTextScale = 0.5f;

                // get the local players equivalent
                EquipmentList.Equipment selfEquipment = selfEquipmentList.getByType(equipment.getType());

                stack.push();

                stack.translate(0.f, -1.f, 0.f);
                stack.scale(enchantTextScale, enchantTextScale, 0.f);
                stack.translate(0.f, -enchantments.size() * SurfaceHelper.getStringHeight(), 0.f);

                enchantments.sort(null);
                int j = 0;
                for(ItemEnchantment enchantment : enchantments) {
                  stack.push();
                  stack.translate(1f, j++ * SurfaceHelper.getStringHeight(), -50.f);

                  // render enchantment short name
                  SurfaceHelper.renderString(buffers, stack.getLast().getMatrix(),
                      enchantment.getShortName(), 0, 0, Colors.WHITE, true);

                  if(enchantment.isMultiLevel()) {
                    // render level
                    String level = String.valueOf(enchantment.getLevel());
                    stack.translate(itemSize + SurfaceHelper.getStringWidth(level) - 2f, 0.f, 0.f);

                    Color color = Colors.ORANGE;

                    ItemEnchantment selfEnchantment = selfEquipment.getEnchantmentById(enchantment.getEnchantment());
                    if(selfEnchantment != null) {
                      int cmp = enchantment.getLevel() - selfEnchantment.getLevel();
                      if(cmp == 0) {
                        color = Colors.CYAN;
                      } else if(cmp < 0) {
                        color = Colors.GREEN;
                      } else {
                        color = Colors.ORANGE;
                      }
                    }

                    SurfaceHelper.renderString(buffers, stack.getLast().getMatrix(),
                        level, 0, 0, color, true);
                  }

                  stack.pop();
                }
                stack.pop();
              }

              if(itemStack.getItem().showDurabilityBar(itemStack)) {
                stack.push();
                float dur = (float) (itemStack.getMaxDamage() - itemStack.getDamage()) / (float) itemStack.getMaxDamage();
                String text = Math.round(dur * 100.f) + "%";

                stack.scale(0.5f, 0.5f, 150);

                SurfaceHelper.renderString(buffers, stack.getLast().getMatrix(),
                    text, 0, 0, getColorLevel(dur), true);
                stack.pop();
              }

              stack.translate(itemSize / 2.f, itemSize / 2.f, 0);
              stack.scale(1.f, -1.f, 1.f);
              stack.scale(itemSize, itemSize, 0);

              SurfaceHelper.renderItem(living, itemStack, stack, MC.getRenderTypeBuffers().getBufferSource());

              stack.pop();
            }

            stack.pop();
          }

          if (settings.isBoxEnabled()) {
            float x = (float) (topX - (width / 2.d));
            float y = (float) topY;

            triangles.putRectInLoop(x - 1, y - 1, width + 2, height + 2, 3, Colors.BLACK);
            triangles.putRectInLoop(x, y, width, height, 1, settings.getColor().getValue());
          }
        });

    //

    MC.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
    MC.getTextureManager().getTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
        .setBlurMipmapDirect(false, false);

    RenderHelper.disableStandardItemLighting();
    RenderHelper.setupGuiFlatDiffuseLighting();

    buffers.finish(RenderTypeEx.blockTranslucentCull());
    buffers.finish(RenderTypeEx.blockCutout());
    buffers.finish(RenderType.getGlint());
    buffers.finish(RenderType.getEntityGlint());
    MC.getRenderTypeBuffers().getBufferSource().finish();
    buffers.finish();

    RenderHelper.enableStandardItemLighting();
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
      UNKNOWN
      ;
    }

    private final List<Equipment> equipment = Lists.newArrayListWithCapacity(6);
    private int renderCount;

    EquipmentList(LivingEntity living) {
      // main hand
      add(EquipmentType.MAIN_HAND, living.getHeldItemMainhand());

      // add armor
      List<ItemStack> armors = Lists.newArrayList(living.getArmorInventoryList());
      Collections.reverse(armors);
      for(ItemStack stack : armors) {
        if(stack.getItem() instanceof ArmorItem) {
          ArmorItem armorItem = (ArmorItem) stack.getItem();
          switch (armorItem.getEquipmentSlot()) {
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
      add(EquipmentType.OFF_HAND, living.getHeldItemOffhand());
    }

    private void add(EquipmentType type, ItemStack stack) {
      equipment.add(new Equipment(stack, type, stack.isEmpty() ? -1 : renderCount));

      // increment if not empty
      if(!stack.isEmpty()) renderCount++;
    }

    public Equipment getByType(EquipmentType type) {
      if(!EquipmentType.UNKNOWN.equals(type)) {
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
        for(ItemEnchantment enchantment : getEnchantments()) {
          if(enchantment.getEnchantment().equals(other)) {
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
