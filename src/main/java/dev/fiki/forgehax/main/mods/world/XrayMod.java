package dev.fiki.forgehax.main.mods.world;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.BlockHelper;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.Listeners;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.render.CullCavesEvent;
import dev.fiki.forgehax.asm.hooks.XrayHooks;
import dev.fiki.forgehax.main.Common;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Set;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.isInWorld;
import static dev.fiki.forgehax.main.Common.reloadChunkSmooth;

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
      .listener(Listeners.onUpdate(o -> reloadWorldChunks()))
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
        reloadWorldChunks();
      })
      .build();

  public final BooleanSetting fullbright = newBooleanSetting()
      .name("fullbright")
      .description("Light blocks up as much as possible")
      .defaultTo(true)
      .changedListener(((from, to) -> {
        XrayHooks.setFullbright(to);
        reloadWorldChunks();
      }))
      .build();

  {
    blocks.newSimpleCommand()
        .name("match-add")
        .alias("madd")
        .alias("bulk-add")
        .description("Add all matching a given string. Use ? to match exactly 1 character, and * to match 0 or more")
        .argument(Arguments.newStringArgument()
            .label("search blocks")
            .maxArgumentsConsumed(1)
            .build())
        .executor(args -> {
          final String searchString = args.<String>getFirst().getValue();
          final Set<Block> blocks = BlockHelper.getBlocksMatching(Common.getBlockRegistry(), searchString);

          if (blocks.isEmpty()) {
            args.warn("Found no blocks matching %s.", searchString);
            if (!searchString.contains(":")) {
              args.warn("Did you mean \"minecraft:%s\"?", searchString);
            }
          } else {
            args.inform("Adding blocks %s", blocks.stream()
                .map(BlockHelper::getBlockRegistryName)
                .collect(Collectors.joining(", ")));
            this.blocks.addAll(blocks);
          }
        })
        .build();

    blocks.newSimpleCommand()
        .name("match-remove")
        .alias("mremove")
        .alias("mdelete")
        .alias("bulk-remove")
        .description("Remove all matching a given string. Use ? to match exactly 1 character, and * to match 0 or more")
        .argument(Arguments.newStringArgument()
            .label("search blocks")
            .maxArgumentsConsumed(1)
            .build())
        .executor(args -> {
          final String searchString = args.<String>getFirst().getValue();
          final Set<Block> blocks = BlockHelper.getBlocksMatching(this.blocks, searchString);

          if (blocks.isEmpty()) {
            args.warn("Found no blocks matching %s", searchString);
            if (!searchString.contains(":")) {
              args.warn("Did you mean \"minecraft:%s\"?", searchString);
            }
          } else {
            args.inform("Removing blocks %s", blocks.stream()
                .map(BlockHelper::getBlockRegistryName)
                .collect(Collectors.joining(", ")));
            this.blocks.removeAll(blocks);
          }
        })
        .build();
  }

  private void reloadWorldChunks() {
    if (isEnabled() && isInWorld()) {
      reloadChunkSmooth();
    }
  }

  @Override
  public void onEnabled() {
//    previousForgeLightPipelineEnabled = ForgeConfig.CLIENT.forgeLightPipelineEnabled.get();
//    ForgeConfig.CLIENT.forgeLightPipelineEnabled.set(false);

    XrayHooks.setXrayBlocks(true);
    XrayHooks.setFullbright(fullbright.getValue());
    XrayHooks.setBlockAlphaOverride(opacity.floatValue() / 255.f);
    XrayHooks.setShouldXrayBlock(state -> blocks.contains(state.getBlock()));

    reloadWorldChunks();
  }

  @Override
  public void onDisabled() {
//    ForgeConfig.CLIENT.forgeLightPipelineEnabled.set(previousForgeLightPipelineEnabled);
//    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;

    XrayHooks.setXrayBlocks(false);
    XrayHooks.setFullbright(false);
    XrayHooks.setShouldXrayBlock(state -> false);

    if (isInWorld()) {
      reloadChunkSmooth();
    }
  }

  @SubscribeListener
  public void onCullCaves(CullCavesEvent event) {
    event.setCanceled(true);
  }
}
