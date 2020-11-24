package dev.fiki.forgehax.api.math;

public class AlignHelper {
  public enum Align {
    TOPLEFT, TOP, TOPRIGHT,
    CENTERLEFT, CENTER, CENTERRIGHT,
    BOTTOMLEFT, BOTTOM, BOTTOMRIGHT;

  }
    /*
    Horizontal
    0 1 2 % 3 = 0,1,2
    3 4 5 % 3 = 0,1,2
    6 7 8 % 3 = 0,1,2
    Vertical
    0 1 2 // 3 = 0,0,0
    3 4 5 // 3 = 1,1,1
    6 7 8 // 3 = 2,2,2
    */

  // returns either left or right (text flow) direction. Middle is considered to continue right
  public static int getFlowDirX2(Align mask) {
    return getFlowDirX2(mask.ordinal());
  }

  public static int getFlowDirX2(int mask) {
    return mask % 3 < 2 ? 1 : -1;
  }

  public static int getFlowDirY2(Align mask) {
    return getFlowDirY2(mask.ordinal());
  }

  public static int getFlowDirY2(int mask) {
    return mask / 3 < 2 ? 1 : -1;
  }

  public static int getPosX(int mask) {
    return mask % 3;
  }

  public static int getPosY(int mask) {
    return mask / 3;
  }

  // returns direction relative to center (cartesian)
  public static int getSignumX(int mask) {
    return mask % 3 - 1;
  }

  public static int getSignumY(int mask) {
    return mask / 3 - 1;
  }

  public static int alignH(int width, int mask) {
    return width * getPosX(mask) / 2;
  }

  public static int alignV(int height, int mask) {
    return height * getPosY(mask) / 2;
  }

  public static boolean isLeft(Align mask) {
    return isLeft(mask.ordinal());
  }

  public static boolean isLeft(int mask) {
    return mask % 3 == 0;
  }

  public static boolean isMiddleH(Align mask) {
    return isMiddleH(mask.ordinal());
  }

  public static boolean isMiddleH(int mask) {
    return mask % 3 == 1;
  }

  public static boolean isRight(Align mask) {
    return isRight(mask.ordinal());
  }

  public static boolean isRight(int mask) {
    return mask % 3 == 2;
  }

  public static boolean isTop(Align mask) {
    return isTop(mask.ordinal());
  }

  public static boolean isTop(int mask) {
    return mask / 3 == 0;
  }

  public static boolean isMiddleV(Align mask) {
    return isMiddleV(mask.ordinal());
  }

  public static boolean isMiddleV(int mask) {
    return mask / 3 == 1;
  }

  public static boolean isBottom(Align mask) {
    return mask.ordinal() / 3 == 2;
  }

  public static boolean isBottom(int mask) {
    return mask / 3 == 2;
  }

  public static boolean isCenter(Align mask) {
    return isMiddleH(mask.ordinal()) && isMiddleV(mask.ordinal());
  }

  public static boolean isTopLeft(Align mask) {
    return isTop(mask.ordinal()) && isLeft(mask.ordinal());
  }

  public static boolean isTopRight(Align mask) {
    return isTop(mask.ordinal()) && isRight(mask.ordinal());
  }

  public static boolean isBottomLeft(Align mask) {
    return isBottom(mask.ordinal()) && isLeft(mask.ordinal());
  }

  public static boolean isBottomRight(Align mask) {
    return isBottom(mask.ordinal()) && isRight(mask.ordinal());
  }
}
