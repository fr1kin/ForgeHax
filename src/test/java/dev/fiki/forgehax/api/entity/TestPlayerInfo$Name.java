package dev.fiki.forgehax.api.entity;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created on 8/1/2017 by fr1kin
 */
public class TestPlayerInfo$Name {
  @Test
  void testOrdering() {
    List<PlayerInfo.Name> names = Lists.newArrayList();
    names.add(new PlayerInfo.Name("AutismBot", 1474329415000L));
    names.add(new PlayerInfo.Name("TheCoonSlayer_", 1466373999000L));
    names.add(new PlayerInfo.Name("TristanDuarte", 1431126544000L));
    names.add(new PlayerInfo.Name("Exemplified", 1425572947000L));
    names.add(new PlayerInfo.Name("tristanduarte", 0L));

    // copy this list, this is the natural order
    List<PlayerInfo.Name> actualList = Lists.newArrayList(names);

    // shuffle order
    Collections.shuffle(names);

    // sort
    Collections.sort(names);

    assertEquals(names.size(), actualList.size());

    for (int i = 0; i < actualList.size(); i++) {
      PlayerInfo.Name expecting = actualList.get(i);
      PlayerInfo.Name actual = names.get(i);
      assertEquals(expecting, actual);
    }
  }
}
