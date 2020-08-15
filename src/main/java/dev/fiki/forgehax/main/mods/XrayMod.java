package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.asm.events.render.CullCavesEvent;
import dev.fiki.forgehax.asm.hooks.XrayHooks;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.reloadChunks;

@RegisterMod(
    name = "XRay",
    description = "See blocks through walls",
    category = Category.WORLD
)
public class XrayMod extends ToggleMod {

  public final SimpleSettingSet<Block> blocks = newSimpleSettingSet(Block.class)
      .name("blocks")
      .description("Blocks to xray")
      .defaultsTo(Blocks.DIAMOND_ORE)
      .argument(Arguments.newBlockArgument()
          .label("block")
          .build())
      .supplier(Sets::newHashSet)
      .build();

  public final IntegerSetting opacity = newIntegerSetting()
      .name("opacity")
      .description("Xray opacity")
      .defaultTo(150)
      .min(0)
      .max(255)
      .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
      .changedListener((from, to) -> {
        XrayHooks.setBlockAlphaOverride(to.floatValue() / 255.f);
        reloadChunks();
      })
      .build();

  @Override
  public void onEnabled() {
//    previousForgeLightPipelineEnabled = ForgeConfig.CLIENT.forgeLightPipelineEnabled.get();
//    ForgeConfig.CLIENT.forgeLightPipelineEnabled.set(false);

    XrayHooks.setXrayBlocks(true);
    XrayHooks.setBlockAlphaOverride(opacity.floatValue() / 255.f);
    XrayHooks.setShouldXrayBlock(state -> blocks.contains(state.getBlock()));

    reloadChunks();
  }

  @Override
  public void onDisabled() {
//    ForgeConfig.CLIENT.forgeLightPipelineEnabled.set(previousForgeLightPipelineEnabled);
//    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;

    XrayHooks.setXrayBlocks(false);
    XrayHooks.setShouldXrayBlock(state -> false);

    reloadChunks();
  }

  @SubscribeEvent
  public void onCullCaves(CullCavesEvent event) {
    event.setCanceled(true);
  }
}
