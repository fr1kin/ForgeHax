package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.extension.GeneralEx;
import dev.fiki.forgehax.api.extension.ItemEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.modloader.di.Injected;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "AutoHotbarReplenish",
    description = "Will replenish tools or block stacks automatically",
    category = Category.PLAYER,
    flags = EnumFlag.EXECUTOR_MAIN_THREAD
)
@RequiredArgsConstructor
@ExtensionMethod({GeneralEx.class, ItemEx.class, LocalPlayerEx.class})
public class AutoHotbarReplenish extends ToggleMod {
  @Injected
  private final Executor main;
  @Injected("async")
  private final Executor async;

  private final IntegerSetting durability_threshold = newIntegerSetting()
      .name("durability-threshold")
      .description("Will auto replace tools when they hit this damage value")
      .defaultTo(5)
      .min(0)
      .max((int) Short.MAX_VALUE)
      .build();

  private final IntegerSetting stack_threshold = newIntegerSetting()
      .name("stack-threshold")
      .description("Will replace stacks when there only remains this many")
      .defaultTo(10)
      .min(1)
      .max((int) Short.MAX_VALUE)
      .build();

  private final IntegerSetting tick_delay = newIntegerSetting()
      .name("tick-delay")
      .description("Number of ticks between each window click packet. 0 will have no limit and a negative value will send n packets per tick")
      .defaultTo(1)
      .build();

  private final BooleanSetting noGui = newBooleanSetting()
      .name("no-gui")
      .description("Don't run when a gui is open")
      .defaultTo(true)
      .build();

  private AtomicReference<CompletableFuture<?>> worker = new AtomicReference<>(CompletableFuture.completedFuture(null));

  private boolean isMonitoring(Slot slot) {
    ItemStack stack = slot.getItem();
    return stack.canBeDamaged() || stack.isStackable();
  }

  private boolean isAboveThreshold(ItemStack stack) {
    return stack.canBeDamaged()
        ? stack.getDurability() > durability_threshold.getValue()
        : stack.getStackCount() > stack_threshold.getValue();
  }

  private boolean isAboveThreshold(Slot slot) {
    return isAboveThreshold(slot.getItem());
  }

  private boolean isExchangeable(ItemStack stack) {
    return !stack.canBeDamaged() || isAboveThreshold(stack);
  }

  private boolean isExchangeable(Slot slot) {
    return isExchangeable(slot.getItem());
  }

  @SneakyThrows
  private void sleepTicks(int ticks) {
    // 20 ticks a second = 1 tick every 50ms
    Thread.sleep(50L * Math.max(ticks, 0));
  }

  private Executor asyncExecutor() {
    return tick_delay.intValue() <= 0 ? Runnable::run : async;
  }

  private void stopWorker() {
    val w = worker.getAndSet(CompletableFuture.completedFuture(null));
    if (w != null) {
      w.cancel(true);
    }
  }

  @Override
  protected void onDisabled() {
    stopWorker();
  }

  @SubscribeListener
  public void onTick(PreGameTickEvent event) {
    // only process when a gui isn't opened by the player
    if (!isInWorld() || (getDisplayScreen() != null && noGui.getValue())) {
      return;
    }

    if (worker.get().isDone()) {
      val lp = getLocalPlayer();

      // get all items in hotbar
      worker.set(lp.getHotbarSlots().stream()
          // only track the ones that can be replaced or filtered
          .filter(this::isMonitoring)
          // filter out items that are not ready to be replenished
          .negated(this::isAboveThreshold)
          // filter out items that do not have replenishments
          .filter(slot -> lp.getTopSlots().stream()
              .filter(this::isMonitoring)
              // all stackables and tools above threshold
              .filter(this::isExchangeable)
              .map(Slot::getItem)
              .anyMatch(slot.getItem()::sameItemStackIgnoreDurability))
          .min(Comparator.comparingInt(ItemEx::getDistanceFromSelected))
          .map(hotbarSlot -> {
            final ItemStack stack = hotbarSlot.getItem();

            // if the item can be damaged then it is a tool
            if (stack.canBeDamaged()) {
              return lp.getTopSlots().stream()
                  .filter(this::isMonitoring)
                  .filter(this::isExchangeable)
                  .filter(s -> stack.sameItemStackIgnoreDurability(s.getItem()))
                  // get the item with the best matching enchantments
                  .max(Comparator.comparing(Slot::getItem, ItemEx::compareEnchantments))
                  .map(slot -> CompletableFuture.runAsync(() -> {
                  }, main)
                      .thenRun(() -> slot.click(ClickType.SWAP, hotbarSlot.getHotbarIndex()))
                      .waitTicks(tick_delay.intValue(), asyncExecutor(), main))
                  .orElse(null);
            } else {
              return lp.getTopSlots().stream()
                  .filter(this::isMonitoring)
                  .filter(this::isExchangeable)
                  .filter(s -> stack.sameItemStackIgnoreDurability(s.getItem()))
                  // get the slot with the least amount of items in the stack because
                  // this may require the fewest clicks to complete
                  .min(Comparator.comparing(Slot::getItem, Comparator.comparingInt(ItemStack::getCount)))
                  .map(slot -> CompletableFuture.supplyAsync(() -> slot.click(ClickType.PICKUP, 0), main)
                      .waitTicks(tick_delay.intValue(), asyncExecutor(), main)
                      // this will place the item into the hotbar
                      .thenApply(itemStack -> hotbarSlot.click(ClickType.PICKUP, 0))
                      // cleanup
                      .thenCompose(stack1 -> {
                        // if we dont get an empty item stack result, then we are still carrying an item
                        // and need to deposit it back to where it was
                        if (!stack1.isEmpty()) {
                          return CompletableFuture.runAsync(() -> {
                          }, main)
                              .waitTicks(tick_delay.intValue(), asyncExecutor(), main)
                              .thenApply(o -> slot.click(ClickType.PICKUP, 0))
                              .thenCompose(stack2 -> {
                                // we picked up something that managed to get in our inventory
                                // time to dumpster it
                                if (!stack2.isEmpty()) {
                                  return CompletableFuture.runAsync(() -> {
                                  }, main)
                                      .waitTicks(tick_delay.intValue(), asyncExecutor(), main)
                                      .thenRun(() -> lp.throwHeldItem());
                                }
                                return CompletableFuture.completedFuture(null);
                              });
                        } else {
                          // otherwise we don't have to care
                          return CompletableFuture.completedFuture(null);
                        }
                      })
                      .waitTicks(tick_delay.intValue(), asyncExecutor(), main)
                  )
                  .orElse(null);
            }
          })
          .orElse(CompletableFuture.completedFuture(null)));
    }
  }
}
