package dev.fiki.forgehax.main.util.key;

import com.google.common.collect.Queues;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.Collection;
import java.util.Queue;
import java.util.stream.Collectors;

public class KeysHandle {
  private Queue<KeyWithContext> keys = Queues.newArrayDeque();

  KeysHandle(Collection<KeyBinding> keys) {
    this.keys.addAll(keys.stream()
        .map(key -> {
          IKeyConflictContext old = key.getKeyConflictContext();
          key.setKeyConflictContext(BindingHelper.getEmptyKeyConflictContext());
          return new KeyWithContext(key, old);
        })
        .collect(Collectors.toList()));
  }

  public void revert() {
    while(!keys.isEmpty()) {
      KeyWithContext o = keys.poll();
      o.getKey().setKeyConflictContext(o.getContext());
    }
  }

  @Getter
  @AllArgsConstructor
  private static class KeyWithContext {
    private final KeyBinding key;
    private final IKeyConflictContext context;
  }
}
