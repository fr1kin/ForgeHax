package dev.fiki.forgehax.api.math;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/** Created on 6/26/2017 by fr1kin */
public class TestAngle {
  /*
     If any of these tests fail, it maybe because of the precision set inside AngleHelper.java
     being too high. By default it 9 decimal places
  */

  @Test
  public void testEquals() {
    Angle rad = Angle.radians(Math.PI / 2.D, Math.PI / 4.D);
    Angle deg = Angle.degrees(90, 45);

    assertEquals(rad, deg);
    assertEquals(rad, deg.inRadians());
    assertEquals(rad.inDegrees(), deg);
    assertEquals(rad.inDegrees(), deg.inRadians());
  }

  @Test
  public void testEqualsNonNormalAngle() {
    Angle rad = Angle.radians(4.D * Math.PI, (11.D * Math.PI) / 4.D);
    Angle deg = Angle.degrees(720, 495);

    assertEquals(rad, deg);
    assertEquals(rad, deg.inRadians());
    assertEquals(rad.inDegrees(), deg);
    assertEquals(rad.inDegrees(), deg.inRadians());
  }

  @Test
  public void testNotEquals() {
    Angle rad = Angle.radians(Math.PI / 2.D, Math.PI / 4.D);
    Angle deg = Angle.degrees(180, 45);

    assertNotEquals(rad, deg);
    assertNotEquals(rad, deg.inRadians());
    assertNotEquals(rad.inDegrees(), deg);
    assertNotEquals(rad.inDegrees(), deg.inRadians());
  }

  @Test
  public void testNotEqualsNonNormalAngle() {
    Angle rad = Angle.radians(4.D * Math.PI, (11.D * Math.PI) / 4.D);
    Angle deg = Angle.degrees(810, 495);

    assertNotEquals(rad, deg);
    assertNotEquals(rad, deg.inRadians());
    assertNotEquals(rad.inDegrees(), deg);
    assertNotEquals(rad.inDegrees(), deg.inRadians());
  }

  @Test
  public void testHashCodeEquals() {
    Angle rad = Angle.radians(Math.PI / 2.D, Math.PI / 4.D);
    Angle deg = Angle.degrees(90, 45);

    assertEquals(rad.hashCode(), deg.hashCode());
    assertEquals(rad.hashCode(), deg.inRadians().hashCode());
    assertEquals(rad.inDegrees().hashCode(), deg.hashCode());
    assertEquals(rad.inDegrees().hashCode(), deg.inRadians().hashCode());
  }

  @Test
  public void TestEqualsNormalization() {
    Angle deg1 = Angle.degrees(90, 45);
    Angle deg2 = Angle.degrees(90 + 360, 45 + 360);

    assertEquals(deg1, deg2);
  }
}
