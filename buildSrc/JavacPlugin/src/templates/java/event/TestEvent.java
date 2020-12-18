package event;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import dev.fiki.forgehax.api.event.ListenerList;

import java.util.Map;

@Cancelable
public class TestEvent extends Event {
  private final int a;
  private final float aa;
  private final double[] aaa;
  private final boolean b;
  private final Object c;
  private final Object[] cc;

  public TestEvent(int a, boolean b) {
    super();
    this.a = a;
    this.aa = 0;
    this.aaa = null;
    this.b = b;
    this.c = null;
    this.cc = null;
  }

  public ListenerList callImplicitListenerList() {
    return listenerList();
  }

  public ListenerList callExplicitListenerList() {
    return TestEvent.listenerList();
  }

  public ListenerList callParentListenerList() {
    return Event.listenerList();
  }
}

@Cancelable
class GenericTestEvent<A extends Object, B extends Number, C extends CharSequence, D> extends Event {
  private final A a;
  private final B b;
  private final Map<C, D> cd;

  public GenericTestEvent(A a, B b, Map<C, D> cd) {
    this.a = a;
    this.b = b;
    this.cd = cd;
  }

  public static class Nested<A extends Object, B extends Number, C extends CharSequence, D>
      extends GenericTestEvent<A, B, C, D> {
    public Nested(A a, B b, Map<C, D> cd) {
      super(a, b, cd);
    }
  }
}
