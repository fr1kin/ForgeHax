package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.entity.EnchantmentUtils;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Objects;

@RegisterMod
public class EntityEspMod extends ToggleMod {
    private static final int HEALTHBAR_WIDTH = 15;
    private static final int HEALTHBAR_HEIGHT = 3;

    public enum DrawOptions {
        DISABLED,
        NAME,
        SIMPLE,
        ADVANCED,
    }

    public enum ArmorOptions {
        DISABLED,
        SIMPLE,
        ENCHANTMENTS
    }

    public final Setting<Integer> players = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("players")
            .description("Enables players")
            .defaultTo(DrawOptions.NAME.ordinal())
            .min(0)
            .max(DrawOptions.values().length - 1)
            .build();

    public final Setting<Integer> mobs_hostile = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("mobs_hostile")
            .description("Enables hostile mobs")
            .defaultTo(DrawOptions.NAME.ordinal())
            .min(0)
            .max(DrawOptions.values().length - 1)
            .build();

    public final Setting<Integer> mobs_friendly = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("mobs_friendly")
            .description("Enables friendly mobs")
            .defaultTo(DrawOptions.NAME.ordinal())
            .min(0)
            .max(DrawOptions.values().length - 1)
            .build();

    public final Setting<Integer> armor = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("armor")
            .description("Draws armor")
            .defaultTo(ArmorOptions.ENCHANTMENTS.ordinal())
            .min(0)
            .max(ArmorOptions.values().length)
            .build();

    public final Setting<Boolean> distance = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("distance")
            .description("Draws distance")
            .defaultTo(false)
            .build();

    public EntityEspMod() {
        super("EntityESP", false, "Shows entity locations and info");
    }

    private Setting<Integer> getCorrespondingSetting(Entity entity) {
        return EntityUtils.isHostileMob(entity) ? mobs_hostile : (EntityUtils.isPlayer(entity) ? players : mobs_friendly);
    }

    /**
     * Check if we should draw the entity
     */
    private boolean shouldDraw(EntityLivingBase entity) {
        return LocalPlayerUtils.isTargetEntity(entity) || (
                !entity.equals(MC.player) &&
                        EntityUtils.isAlive(entity) &&
                        EntityUtils.isValidEntity(entity) && (
                        (mobs_hostile.get() > 0 && EntityUtils.isHostileMob(entity)) || // check this first
                                (players.get() > 0 && EntityUtils.isPlayer(entity)) ||
                                (mobs_friendly.get() > 0 && EntityUtils.isFriendlyMob(entity))
                ));
    }

    @SubscribeEvent
    public void onRenderPlayerNameTag(RenderLivingEvent.Specials.Pre event) {
        if(EntityUtils.isPlayer(event.getEntity()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            for (Entity entity : MC.world.loadedEntityList) {
                if(EntityUtils.isLiving(entity) && shouldDraw((EntityLivingBase) entity)) {
                    EntityLivingBase living = (EntityLivingBase) (entity);
                    Vec3d bottomVec = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
                    Vec3d topVec = bottomVec.add(new Vec3d(0, (entity.getRenderBoundingBox().maxY - entity.posY), 0));
                    VectorUtils.ScreenPos top = VectorUtils.toScreen(topVec.x, topVec.y, topVec.z);
                    VectorUtils.ScreenPos bot = VectorUtils.toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
                    if (top.isVisible || bot.isVisible) {
                        Setting<Integer> enabled = getCorrespondingSetting(living);

                        int topX = top.x;
                        int topY = top.y + 1;
                        int botX = bot.x;
                        int botY = bot.y + 1;
                        int height = (bot.y - top.y);
                        int width = height;

                        // optical esp
                        // drawMode == null means they are the target but the esp is disabled for them
                        if (LocalPlayerUtils.isTargetEntity(entity)
                                || enabled.get() > 0) {
                            int x = (top.x - (width / 2));
                            int y = top.y;
                            int w = width;
                            int h = height;
                            // outer
                            SurfaceUtils.drawOutlinedRect(x - 1, y - 1, w + 2, h + 2, Utils.Colors.BLACK, 2.f);
                            // inner
                            SurfaceUtils.drawOutlinedRect(x, y, w, h, EntityUtils.getDrawColor(living), 2.f);
                            // outer
                            SurfaceUtils.drawOutlinedRect(x + 1, y + 1, w - 2, h - 2, Utils.Colors.BLACK, 2.f);
                        }

                        //----TOP ESP----

                        // health esp
                        if (enabled.get() == (DrawOptions.ADVANCED.ordinal()) || enabled.get() == (DrawOptions.SIMPLE.ordinal())) {
                            double hp = (living.getHealth() / living.getMaxHealth());
                            int posX = topX - (HEALTHBAR_WIDTH / 2);
                            int posY = topY - HEALTHBAR_HEIGHT - 2;
                            SurfaceUtils.drawRect(posX, posY, HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT, Utils.toRGBA(0, 0, 0, 255));
                            SurfaceUtils.drawRect(
                                    posX + 1,
                                    posY + 1,
                                    (int) ((float) (HEALTHBAR_WIDTH - 2) * hp),
                                    HEALTHBAR_HEIGHT - 2,
                                    Utils.toRGBA((int) ((255 - hp) * 255), (int) (255 * hp), 0, 255)
                            );
                            topY -= HEALTHBAR_HEIGHT + 1;
                        }

                        // name esp
                        if (enabled.get() == DrawOptions.ADVANCED.ordinal() || enabled.get() == (DrawOptions.SIMPLE.ordinal()) || enabled.get() == (DrawOptions.NAME.ordinal())) {
                            String text = living.getDisplayName().getFormattedText();
                            if(distance.get() && (enabled.get() == DrawOptions.SIMPLE.ordinal() || enabled.get() == (DrawOptions.ADVANCED.ordinal()))) {
                                text += String.format(" (%.1f)", living.getPositionVector().distanceTo(MC.player.getPositionVector()));
                            }
                            SurfaceUtils.drawTextShadow(text, topX - (SurfaceUtils.getTextWidth(text) / 2), topY - SurfaceUtils.getTextHeight() - 1, Utils.toRGBA(255, 255, 255, 255));
                            topY -= SurfaceUtils.getTextHeight() + 1;
                        }

                        //----BOTTOM ESP----

                        // armor esp
                        if (enabled.get() == (DrawOptions.ADVANCED.ordinal()) && armor.get() > 0) {
                            List<ItemStack> armor = Lists.newArrayList();
                            for (ItemStack stack : living.getEquipmentAndArmor())
                                if (stack != null || Objects.equals(stack, ItemStack.EMPTY)) // only add non-null items
                                    armor.add(0, stack);
                            if (armor.size() > 0) {
                                int endY = botY + 16;
                                int posX = topX - ((16 * armor.size()) / 2);
                                for (int i = 0; i < armor.size(); i++) {
                                    ItemStack stack = armor.get(i);
                                    int startX = posX + (i * 16);
                                    int startY = botY;
                                    SurfaceUtils.drawItemWithOverlay(stack, startX, startY);
                                    // enchantment esp
                                    if (this.armor.get() == (ArmorOptions.ENCHANTMENTS.ordinal())) {
                                        List<EnchantmentUtils.EntityEnchantment> enchantments = EnchantmentUtils.getEnchantmentsSorted(stack.getEnchantmentTagList());
                                        if (enchantments != null) {
                                            for (EnchantmentUtils.EntityEnchantment enchant : enchantments) {
                                                SurfaceUtils.drawTextShadow(enchant.getShortName(),
                                                        startX,
                                                        startY,
                                                        Utils.toRGBA(255, 255, 255, 255),
                                                        0.50D
                                                );
                                                startY += SurfaceUtils.getTextHeight(0.50D);
                                                if (startY > endY)
                                                    endY = startY;
                                            }
                                        }
                                    }
                                }
                                botY = endY + 1;
                            }
                        }
                    }
                }
            }
        }
    }
}
