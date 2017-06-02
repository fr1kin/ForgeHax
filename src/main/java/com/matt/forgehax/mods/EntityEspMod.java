package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.*;
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
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

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

    public Property players;
    public Property hostileMobs;
    public Property friendlyMobs;

    public Property armorEsp;
    public Property distanceEsp;

    public EntityEspMod() {
        super("EntityESP", false, "Shows entity locations and info");
    }

    public DrawOptions getDrawOptionValue(Property prop) {
        for(DrawOptions option : DrawOptions.values())
            if(prop.getString().equals(option.name()))
                return option;
        return null;
    }

    public DrawOptions getDrawOptionValue(EntityLivingBase living) {
        if(EntityUtils.isPlayer(living))
            return getDrawOptionValue(players);
        else if(EntityUtils.isHostileMob(living))
            return getDrawOptionValue(hostileMobs);
        else if(EntityUtils.isFriendlyMob(living))
            return getDrawOptionValue(friendlyMobs);
        else
            return null;
    }

    public boolean isDrawOptionPropertyEnabled(Property prop) {
        return getDrawOptionValue(prop) != null && !getDrawOptionValue(prop).equals(DrawOptions.DISABLED);
    }

    public ArmorOptions getArmorOptionValue(Property prop) {
        for(ArmorOptions option : ArmorOptions.values())
            if(prop.getString().equals(option.name()))
                return option;
        return null;
    }

    /**
     * Check if we should draw the entity
     */
    public boolean shouldDraw(EntityLivingBase entity) {
        return LocalPlayerUtils.isTargetEntity(entity) || (
                !entity.equals(MC.player) &&
                EntityUtils.isAlive(entity) &&
                EntityUtils.isValidEntity(entity) && (
                (isDrawOptionPropertyEnabled(hostileMobs) && EntityUtils.isHostileMob(entity)) || // check this first
                (isDrawOptionPropertyEnabled(players) && EntityUtils.isPlayer(entity)) ||
                (isDrawOptionPropertyEnabled(friendlyMobs) && EntityUtils.isFriendlyMob(entity))
        ));
    }

    @Override
    public void onLoadConfiguration(Configuration configuration) {
        super.onLoadConfiguration(configuration);

        String[] DRAW_OPTIONS = Utils.toArray(DrawOptions.values());
        String[] ARMOR_OPTIONS = Utils.toArray(ArmorOptions.values());

        addSettings(
                players = configuration.get(getModName(),
                        "players",
                        DrawOptions.ADVANCED.name(),
                        "Enables player esp",
                        DRAW_OPTIONS),
                hostileMobs = configuration.get(getModName(),
                        "hostile mobs",
                        DrawOptions.ADVANCED.name(),
                        "Enables hostile mob esp",
                        DRAW_OPTIONS),
                friendlyMobs = configuration.get(getModName(),
                        "friendly mobs",
                        DrawOptions.ADVANCED.name(),
                        "Enables friendly mob esp",
                        DRAW_OPTIONS),
                armorEsp = configuration.get(getModName(),
                        "armor esp",
                        ArmorOptions.ENCHANTMENTS.name(),
                        "Shows info about entities armor if set to advanced esp mode",
                        ARMOR_OPTIONS),
                distanceEsp = configuration.get(getModName(),
                        "distance esp",
                        false,
                        "Shows distance in name tags if selected settings >name"
                )
        );
    }

    @SubscribeEvent
    public void onRenderPlayerNameTag(RenderLivingEvent.Specials.Pre event) {
        if(EntityUtils.isPlayer(event.getEntity()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            ArmorOptions armorMode = getArmorOptionValue(armorEsp);
            for (Entity entity : MC.world.loadedEntityList) {
                if(EntityUtils.isLiving(entity) && shouldDraw((EntityLivingBase) entity)) {
                    EntityLivingBase living = (EntityLivingBase) (entity);
                    Vec3d bottomVec = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
                    Vec3d topVec = bottomVec.add(new Vec3d(0, (entity.getRenderBoundingBox().maxY - entity.posY), 0));
                    VectorUtils.ScreenPos top = VectorUtils.toScreen(topVec.xCoord, topVec.yCoord, topVec.zCoord);
                    VectorUtils.ScreenPos bot = VectorUtils.toScreen(bottomVec.xCoord, bottomVec.yCoord, bottomVec.zCoord);
                    if (top.isVisible || bot.isVisible) {
                        DrawOptions drawMode = getDrawOptionValue(living);

                        int topX = top.x;
                        int topY = top.y + 1;
                        int botX = bot.x;
                        int botY = bot.y + 1;
                        int height = (bot.y - top.y);
                        int width = height;

                        // optical esp
                        // drawMode == null means they are the target but the esp is disabled for them
                        if (LocalPlayerUtils.isTargetEntity(entity) ||
                                drawMode != null && drawMode.equals(DrawOptions.ADVANCED)) {
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

                        // no more drawing
                        if(drawMode == null)
                            continue;

                        //----TOP ESP----

                        // health esp
                        if (drawMode.equals(DrawOptions.ADVANCED) || drawMode.equals(DrawOptions.SIMPLE)) {
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
                        if (drawMode.equals(DrawOptions.ADVANCED) || drawMode.equals(DrawOptions.SIMPLE) || drawMode.equals(DrawOptions.NAME)) {
                            String text = living.getDisplayName().getFormattedText();
                            if(distanceEsp.getBoolean() && (drawMode.equals(DrawOptions.SIMPLE) || drawMode.equals(DrawOptions.ADVANCED))) {
                                text += String.format(" (%.1f)", living.getPositionVector().distanceTo(MC.player.getPositionVector()));
                            }
                            SurfaceUtils.drawTextShadow(text, topX - (SurfaceUtils.getTextWidth(text) / 2), topY - SurfaceUtils.getTextHeight() - 1, Utils.toRGBA(255, 255, 255, 255));
                            topY -= SurfaceUtils.getTextHeight() + 1;
                        }

                        //----BOTTOM ESP----

                        // armor esp
                        if (drawMode.equals(DrawOptions.ADVANCED) && !armorMode.equals(ArmorOptions.DISABLED)) {
                            List<ItemStack> armor = Lists.newArrayList();
                            for (ItemStack stack : living.getEquipmentAndArmor())
                                if (stack != null) // only add non-null items
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
                                    if (armorMode.equals(ArmorOptions.ENCHANTMENTS)) {
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
