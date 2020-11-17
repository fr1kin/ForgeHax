package dev.fiki.forgehax.api.draw;

/*
   The MIT License (MIT)

   Copyright (c) 2014-2016 Jadran "Lunatrius" Kotnik

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
*/

import net.minecraft.util.Direction;

public final class GeometryMasks {
  
  public static final class Quad {
    public static final int DOWN = 0x01;
    public static final int UP = 0x02;
    public static final int NORTH = 0x04;
    public static final int SOUTH = 0x08;
    public static final int WEST = 0x10;
    public static final int EAST = 0x20;
    public static final int ALL = DOWN | UP | NORTH | SOUTH | WEST | EAST;

    public static int getFlagForDirection(Direction direction) {
      switch (direction) {
        case UP:
          return UP;
        case DOWN:
          return DOWN;
        case EAST:
          return EAST;
        case WEST:
          return WEST;
        case NORTH:
          return NORTH;
        case SOUTH:
          return SOUTH;
      }
      throw new IllegalArgumentException("Unknown direction \"" + direction.name() + "\"");
    }
  }
  
  public static final class Line {
    public static final int DOWN_WEST = 0x001;
    public static final int UP_WEST = 0x002;
    public static final int DOWN_EAST = 0x004;
    public static final int UP_EAST = 0x008;
    public static final int DOWN_NORTH = 0x010;
    public static final int UP_NORTH = 0x020;
    public static final int DOWN_SOUTH = 0x040;
    public static final int UP_SOUTH = 0x080;
    public static final int NORTH_WEST = 0x100;
    public static final int NORTH_EAST = 0x200;
    public static final int SOUTH_WEST = 0x400;
    public static final int SOUTH_EAST = 0x800;
    public static final int ALL =
        DOWN_WEST
            | UP_WEST
            | DOWN_EAST
            | UP_EAST
            | DOWN_NORTH
            | UP_NORTH
            | DOWN_SOUTH
            | UP_SOUTH
            | NORTH_WEST
            | NORTH_EAST
            | SOUTH_WEST
            | SOUTH_EAST;

    public static int getFlagForDirection(Direction direction) {
      switch (direction) {
        case UP:
          return UP_WEST | UP_EAST | UP_NORTH | UP_SOUTH;
        case DOWN:
          return DOWN_WEST | DOWN_EAST | DOWN_NORTH | DOWN_SOUTH;
        case EAST:
          return DOWN_EAST | UP_EAST | NORTH_EAST | SOUTH_EAST;
        case WEST:
          return DOWN_WEST | UP_WEST | NORTH_WEST | SOUTH_WEST;
        case NORTH:
          return NORTH_WEST | NORTH_EAST | UP_NORTH | DOWN_NORTH;
        case SOUTH:
          return SOUTH_WEST | SOUTH_EAST | DOWN_SOUTH | UP_SOUTH;
      }
      throw new IllegalArgumentException("Unknown direction \"" + direction.name() + "\"");
    }
  }
}
