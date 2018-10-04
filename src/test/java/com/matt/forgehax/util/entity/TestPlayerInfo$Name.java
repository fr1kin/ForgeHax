package com.matt.forgehax.util.entity;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** Created on 8/1/2017 by fr1kin */
public class TestPlayerInfo$Name {
  @Test
  public void testOrdering() {
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

    Assert.assertTrue(names.size() == actualList.size());

    for (int i = 0; i < actualList.size(); i++) {
      PlayerInfo.Name expecting = actualList.get(i);
      PlayerInfo.Name actual = names.get(i);
      Assert.assertEquals(expecting, actual);
    }
  }
}
