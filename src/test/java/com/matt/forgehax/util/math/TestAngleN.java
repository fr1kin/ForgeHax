package com.matt.forgehax.util.math;

import org.junit.Assert;
import org.junit.Test;

/** Created on 6/26/2017 by fr1kin */
public class TestAngleN {
  /*
     If any of these tests fail, it maybe because of the precision set inside AngleHelper.java
     being too high. By default it 9 decimal places
  */

  @Test
  public void testEquals() {
    AngleN rad = AngleN.radians(Math.PI / 2.D, Math.PI / 4.D);
    AngleN deg = AngleN.degrees(90, 45);

    Assert.assertTrue(rad.equals(deg));
    Assert.assertTrue(rad.equals(deg.toRadians()));
    Assert.assertTrue(rad.toDegrees().equals(deg));
    Assert.assertTrue(rad.toDegrees().equals(deg.toRadians()));
  }

  @Test
  public void testEqualsNonNormalAngle() {
    AngleN rad = AngleN.radians(4.D * Math.PI, (11.D * Math.PI) / 4.D);
    AngleN deg = AngleN.degrees(720, 495);

    Assert.assertTrue(rad.equals(deg));
    Assert.assertTrue(rad.equals(deg.toRadians()));
    Assert.assertTrue(rad.toDegrees().equals(deg));
    Assert.assertTrue(rad.toDegrees().equals(deg.toRadians()));
  }

  @Test
  public void testNotEquals() {
    AngleN rad = AngleN.radians(Math.PI / 2.D, Math.PI / 4.D);
    AngleN deg = AngleN.degrees(180, 45);

    Assert.assertFalse(rad.equals(deg));
    Assert.assertFalse(rad.equals(deg.toRadians()));
    Assert.assertFalse(rad.toDegrees().equals(deg));
    Assert.assertFalse(rad.toDegrees().equals(deg.toRadians()));
  }

  @Test
  public void testNotEqualsNonNormalAngle() {
    AngleN rad = AngleN.radians(4.D * Math.PI, (11.D * Math.PI) / 4.D);
    AngleN deg = AngleN.degrees(810, 495);

    Assert.assertFalse(rad.equals(deg));
    Assert.assertFalse(rad.equals(deg.toRadians()));
    Assert.assertFalse(rad.toDegrees().equals(deg));
    Assert.assertFalse(rad.toDegrees().equals(deg.toRadians()));
  }

  @Test
  public void testHashCodeEquals() {
    AngleN rad = AngleN.radians(Math.PI / 2.D, Math.PI / 4.D);
    AngleN deg = AngleN.degrees(90, 45);

    Assert.assertTrue(rad.hashCode() == deg.hashCode());
    Assert.assertTrue(rad.hashCode() == deg.toRadians().hashCode());
    Assert.assertTrue(rad.toDegrees().hashCode() == deg.hashCode());
    Assert.assertTrue(rad.toDegrees().hashCode() == deg.toRadians().hashCode());
  }
}
