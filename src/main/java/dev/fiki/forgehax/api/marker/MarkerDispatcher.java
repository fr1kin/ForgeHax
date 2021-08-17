package dev.fiki.forgehax.api.marker;

import com.google.common.collect.Queues;
import dev.fiki.forgehax.api.color.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.concurrent.DelegatedTaskExecutor;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@Log4j2
public class MarkerDispatcher {
  private final Executor executor;
  private final DelegatedTaskExecutor<Runnable> delegatedTaskExecutor;

  final Queue<BufferBuilder> freeBuilders;
  final Queue<MarkerJob> workers = Queues.newPriorityQueue();
  final Queue<Runnable> uploadTasks = Queues.newConcurrentLinkedQueue();

  Function<BlockState, Color> blockToColor = state -> null;
  Vector3d renderPosition = Vector3d.ZERO;
  World world;

  public MarkerDispatcher(Executor executor) {
    this.executor = executor;
    this.delegatedTaskExecutor = DelegatedTaskExecutor.create(executor, "Marker Renderer");

    this.freeBuilders = Queues.newArrayDeque(Stream.generate(() -> new BufferBuilder(256))
        .limit(Math.max(1, Runtime.getRuntime().availableProcessors() / 2))
        .collect(Collectors.toList()));
  }

  private void process() {
    if (!freeBuilders.isEmpty() && !workers.isEmpty()) {
      final MarkerJob job = workers.poll();

      if (job == null) {
        return;
      }

      final BufferBuilder buffer = freeBuilders.poll();

      if (buffer == null) {
        log.warn("Not enough builders!");
        return;
      }

      CompletableFuture.runAsync(() -> {
      }, executor)
          .thenCompose(v -> job.execute(buffer))
          .whenComplete((success, ex) -> {
            if (ex != null) {
              log.error("MarkerDispatcher error in process: {}", ex.getMessage());
              log.error(ex, ex);
            }

            delegatedTaskExecutor.tell(() -> {
              buffer.discard();

              freeBuilders.add(buffer);

              process();
            });
          });
    }
  }

  boolean addUploadTask(Runnable runnable) {
    return uploadTasks.add(runnable);
  }

  public void schedule(MarkerJob job) {
    delegatedTaskExecutor.tell(() -> {
      workers.offer(job);
      process();
    });
  }

  public void updateChunks() {
    while (!uploadTasks.isEmpty()) {
      Runnable task = uploadTasks.poll();
      if (task != null) {
        task.run();
      }
    }
  }

  public void stopUpdates() {
    while (!workers.isEmpty()) {
      MarkerJob job = workers.poll();
      if (job != null) {
        job.cancel();
      }
    }
  }

  public void kill() {
    this.stopUpdates();
    delegatedTaskExecutor.close();
    freeBuilders.clear();
  }
}
