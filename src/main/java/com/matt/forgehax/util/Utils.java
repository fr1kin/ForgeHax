package com.matt.forgehax.util;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.math.Angle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;

public class Utils implements Globals {

  /** Use PacketHelper class now */
  @Deprecated
  public static final List<Packet> OUTGOING_PACKET_IGNORE_LIST = Collections.emptyList();

  @Deprecated
  public static int toRGBA(int r, int g, int b, int a) {
    return (r << 16) + (g << 8) + (b << 0) + (a << 24);
  }

  @Deprecated
  public static int toRGBA(float r, float g, float b, float a) {
    return toRGBA((int) (r * 255.f), (int) (g * 255.f), (int) (b * 255.f), (int) (a * 255.f));
  }

  @Deprecated
  public static int toRGBA(float[] colors) {
    if (colors.length != 4) throw new IllegalArgumentException("colors[] must have a length of 4!");
    return toRGBA(colors[0], colors[1], colors[2], colors[3]);
  }

  @Deprecated
  public static int toRGBA(double[] colors) {
    if (colors.length != 4) throw new IllegalArgumentException("colors[] must have a length of 4!");
    return toRGBA((float) colors[0], (float) colors[1], (float) colors[2], (float) colors[3]);
  }

  @Deprecated
  public static int[] toRGBAArray(int colorBuffer) {
    return new int[] {
      (colorBuffer >> 16 & 255),
      (colorBuffer >> 8 & 255),
      (colorBuffer & 255),
      (colorBuffer >> 24 & 255)
    };
  }

  public static <E extends Enum<?>> String[] toArray(E[] o) {
    String[] output = new String[o.length];
    for (int i = 0; i < output.length; i++) output[i] = o[i].name();
    return output;
  }

  public static UUID stringToUUID(String uuid) {
    if (uuid.contains("-")) {
      // if it contains the hyphen we don't have to manually put them in
      return UUID.fromString(uuid);
    } else {
      // otherwise we have to put
      Pattern pattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
      Matcher matcher = pattern.matcher(uuid);
      return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
    }
  }

  public static double normalizeAngle(double angle) {
    while (angle <= -180) angle += 360;
    while (angle > 180) angle -= 360;
    return angle;
  }

  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static Angle getLookAtAngles(Vec3d start, Vec3d end) {
    return AngleHelper.getAngleFacingInDegrees(end.subtract(start)).normalize();
  }

  public static Angle getLookAtAngles(Vec3d end) {
    return getLookAtAngles(EntityUtils.getEyePos(getLocalPlayer()), end);
  }

  public static Angle getLookAtAngles(Entity entity) {
    return getLookAtAngles(EntityUtils.getOBBCenter(entity));
  }

  public static double scale(
      double x, double from_min, double from_max, double to_min, double to_max) {
    return to_min + (to_max - to_min) * ((x - from_min) / (from_max - from_min));
  }

  public static <T> boolean isInRange(T[] array, int index) {
    return array != null && index >= 0 && index < array.length;
  }

  public static <T> boolean isInRange(Collection<T> list, int index) {
    return list != null && index >= 0 && index < list.size();
  }

  public static <T> T defaultTo(T value, T defaultTo) {
    return value == null ? defaultTo : value;
  }

  public static List<ItemStack> getShulkerContents(ItemStack stack) { // TODO: move somewhere else
    NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
    NBTTagCompound compound = stack.getTagCompound();
    if (compound != null && compound.hasKey("BlockEntityTag", 10)) {
      NBTTagCompound tags = compound.getCompoundTag("BlockEntityTag");
      if (tags.hasKey("Items", 9)) {
        // load in the items
        ItemStackHelper.loadAllItems(tags, contents);
      }
    }
    return contents;
  }

  @Deprecated
  public static class Colors {
    public static final int WHITE = Utils.toRGBA(255, 255, 255, 255);
    public static final int BLACK = Utils.toRGBA(0, 0, 0, 255);
    public static final int RED = Utils.toRGBA(255, 0, 0, 255);
    public static final int GREEN = Utils.toRGBA(0, 255, 0, 255);
    public static final int BLUE = Utils.toRGBA(0, 0, 255, 255);
    public static final int ORANGE = Utils.toRGBA(255, 128, 0, 255);
    public static final int PURPLE = Utils.toRGBA(163, 73, 163, 255);
    public static final int GRAY = Utils.toRGBA(127, 127, 127, 255);
    public static final int DARK_RED = Utils.toRGBA(64, 0, 0, 255);
    public static final int YELLOW = Utils.toRGBA(255, 255, 0, 255);
  }
}
