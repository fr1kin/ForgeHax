package dev.fiki.forgehax.api.event;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventBusTest {
  private static EventBus eventBus;
  private static TestListenable object;

  @BeforeAll
  static void setup() {
    eventBus = new EventBus();
    object = new TestListenable();
  }

  @Test
  @DisplayName("registering events")
  void registerEvent() {
    eventBus.register(object);

    assertThat(eventBus.getObjectListeners(object))
        .describedAs("listener should be inserted")
        .hasSize(1);
  }

  @Test
  @DisplayName("posting an event")
  void postEvent() {
    eventBus.register(new TestListenable());

    TestEventImpl event = new TestEventImpl();
    eventBus.post(event);

    assertThat(event.message).isEqualTo("foo");
  }

  @Test
  @DisplayName("unregistering events")
  void unregisterEvent() {
    eventBus.unregister(object);

    assertThat(eventBus.getObjectListeners(object))
        .describedAs("listener should be removed")
        .isEmpty();
  }

  //
  //
  //

  public static class TestEventImpl extends Event {
    String message;

    void setMessage(String message) {
      this.message = message;
    }
  }

  public static class TestListenable {
    @SubscribeListener
    public void onSomeEvent(TestEventImpl event) {
      event.setMessage("foo");
    }
  }
}
