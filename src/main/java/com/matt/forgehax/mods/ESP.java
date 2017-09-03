package com.matt.forgehax.mods;

import com.google.common.util.concurrent.AtomicDouble;
import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.Fonts;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.Plane;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

@RegisterMod
public class Esp extends ToggleMod implements Fonts {
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

    public final Setting<DrawOptions> players = getCommandStub().builders().<DrawOptions>newSettingEnumBuilder()
            .name("players")
            .description("Enables players")
            .defaultTo(DrawOptions.NAME)
            .build();

    public final Setting<DrawOptions> mobs_hostile = getCommandStub().builders().<DrawOptions>newSettingEnumBuilder()
            .name("hostile")
            .description("Enables hostile mobs")
            .defaultTo(DrawOptions.NAME)
            .build();

    public final Setting<DrawOptions> mobs_friendly = getCommandStub().builders().<DrawOptions>newSettingEnumBuilder()
            .name("friendly")
            .description("Enables friendly mobs")
            .defaultTo(DrawOptions.NAME)
            .build();

    public Esp() {
        super("Esp", false, "Shows entity locations and info");
    }

    @SubscribeEvent
    public void onRenderPlayerNameTag(RenderLivingEvent.Specials.Pre event) {
        if(EntityUtils.isPlayer(event.getEntity()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRender2D(final Render2DEvent event) {
        getWorld().loadedEntityList.stream()
                .filter(EntityUtils::isLiving)
                .filter(entity -> !Objects.equals(getLocalPlayer(), entity) && !EntityUtils.isFakeLocalPlayer(entity))
                .filter(EntityUtils::isAlive)
                .filter(EntityUtils::isValidEntity)
                .map(entity -> (EntityLivingBase)entity)
                .forEach(living -> {
                    final Setting<DrawOptions> setting;

                    switch (EntityUtils.getRelationship(living)) {
                        case PLAYER:
                            setting = players;
                            break;
                        case HOSTILE:
                            setting = mobs_hostile;
                            break;
                        case NEUTRAL:
                        case FRIENDLY:
                            setting = mobs_friendly;
                            break;
                        default:
                            setting = null;
                            break;
                    }

                    if(setting == null || DrawOptions.DISABLED.equals(setting.get())) return;

                    Vec3d bottomPos = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
                    Vec3d topPos = bottomPos.addVector(0.D, living.getRenderBoundingBox().maxY - living.posY, 0.D);

                    Plane top = VectorUtils.toScreen(topPos);
                    Plane bot = VectorUtils.toScreen(bottomPos);

                    // stop here if neither are visible
                    if(!top.isVisible() && !bot.isVisible()) return;

                    double topX = top.getX();
                    double topY = top.getY() + 1.D;
                    double botX = bot.getX();
                    double botY = bot.getY() + 1.D;
                    double height = (bot.getY() - top.getY());
                    double width = height;

                    AtomicDouble offset = new AtomicDouble();
                    TopComponents.REVERSE_VALUES.stream()
                            .filter(comp -> comp.valid(setting))
                            .forEach(comp -> {
                                double os = offset.get();
                                offset.set(os + comp.draw(event.getSurfaceBuilder(), living, topX, topY - os, botX, botY, width, height));
                            });
                });
    }

    private interface IComponent {
        /**
         * Draw component
         * @param living entity handle
         * @param topX top x
         * @param topY top y
         * @param botX bot x
         * @param botY bot y
         * @param width width
         * @param height height
         * @return y offset
         */
        double draw(SurfaceBuilder builder, EntityLivingBase living, double topX, double topY, double botX, double botY, double width, double height);

        /**
         * Check if the draw component is valid for this setting
         * @param setting
         * @return
         */
        boolean valid(Setting<DrawOptions> setting);
    }

    private enum TopComponents implements IComponent {
        NAME {
            @Override
            public double draw(SurfaceBuilder builder, EntityLivingBase living, double topX, double topY, double botX, double botY, double width, double height) {
                String text = living.getDisplayName().getUnformattedText();

                double x = topX - ((double)builder.getFontWidth(text) / 2.D);
                double y = topY - (double)builder.getFontHeight() - 1.D;

                builder.clear()
                        .push()
                        .task(SurfaceBuilder::preBlend)
                        .task(SurfaceBuilder::preFontRender)
                        .task(SurfaceBuilder::postRenderTexture2D) // enable texture
                        .fontRenderer(ARIAL)
                        .color(Utils.Colors.BLACK)
                        .apply()
                        .text(text, x + 1, y + 1)
                        .color(Utils.Colors.WHITE)
                        .apply()
                        .text(text, x, y)
                        .task(SurfaceBuilder::postBlend)
                        .task(SurfaceBuilder::postFontRender)
                        .pop();

                return SurfaceHelper.getTextHeight() + 1.D;
            }

            @Override
            public boolean valid(Setting<DrawOptions> setting) {
                return DrawOptions.DISABLED.compareTo(setting.get()) < 0; // DISABLED less than SETTING
            }
        },
        HEALTH {
            @Override
            public double draw(SurfaceBuilder builder, EntityLivingBase living, double topX, double topY, double botX, double botY, double width, double height) {
                float hp = MathHelper.clamp(living.getHealth(), 0, living.getMaxHealth()) / living.getMaxHealth();
                double x = topX - (HEALTHBAR_WIDTH / 2);
                double y = topY - HEALTHBAR_HEIGHT - 2;
                int color = (living.getHealth() + living.getAbsorptionAmount() > living.getMaxHealth()) ? Utils.Colors.YELLOW
                        : Utils.toRGBA((int) ((255 - hp) * 255), (int) (255 * hp), 0, 255); // if above 20 hp bar is yellow

                builder.clear() // clean up from previous uses
                        .push()
                        .task(SurfaceBuilder::preBlend)
                        .task(SurfaceBuilder::preRenderTexture2D)
                        .beginQuads()
                        .color(Utils.Colors.BLACK)
                        .apply() // apply color
                        .rectangle(x, y, HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT)
                        .end()
                        .clear()
                        .beginQuads()
                        .color(color)
                        .apply() // apply color
                        .rectangle(x + 1.D, y + 1.D, ((double)HEALTHBAR_WIDTH - 2.D) * hp, HEALTHBAR_HEIGHT - 2.D)
                        .end()
                        .task(SurfaceBuilder::postBlend)
                        .task(SurfaceBuilder::postRenderTexture2D)
                        .pop();

                return HEALTHBAR_HEIGHT + 1.D;
            }

            @Override
            public boolean valid(Setting<DrawOptions> setting) {
                return DrawOptions.SIMPLE.compareTo(setting.get()) <= 0; // SIMPLE less than or equal to SETTING
            }
        },
        ITEMS {
            @Override
            public double draw(SurfaceBuilder builder, EntityLivingBase living, double topX, double topY, double botX, double botY, double width, double height) {
                List<ItemStack> items = StreamSupport.stream(living.getEquipmentAndArmor().spliterator(), false)
                        .filter(Objects::nonNull)
                        .filter(stack -> !stack.isEmpty())
                        .collect(Collectors.toList());
                if(!items.isEmpty()) { // only continue if there are elements present
                    final double itemSize = 16;
                    double x = topX - ((itemSize * (double)items.size()) / 2.D);
                    double y = topY - itemSize;
                    for(int index = 0; index < items.size(); ++index) {
                        ItemStack stack = items.get(index);
                        double xx = x + (index * itemSize);
                        builder.clear()
                                .push()
                                .task(SurfaceBuilder::preItemRender)
                                .item(stack, xx, y)
                                .itemOverlay(stack, xx, y)
                                .task(SurfaceBuilder::postItemRender)
                                .pop();
                    }
                    return itemSize + 1.D;
                } else return 0.D;
            }

            @Override
            public boolean valid(Setting<DrawOptions> setting) {
                return DrawOptions.ADVANCED.compareTo(setting.get()) <= 0; // ADVANCED less than or equal to SETTING
            }
        }

        ;

        static final List<TopComponents> REVERSE_VALUES = Arrays.asList(TopComponents.values());

        static {
            Collections.reverse(REVERSE_VALUES);
        }
    }
}
