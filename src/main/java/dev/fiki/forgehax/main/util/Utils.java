package dev.fiki.forgehax.main.util;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.math.AngleHelper;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

public class Utils implements Common {

  public static <E extends Enum<?>> String[] toArray(E[] o) {
    String[] output = new String[o.length];
    for (int i = 0; i < output.length; i++) {
      output[i] = o[i].name();
    }
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
    while (angle <= -180) {
      angle += 360;
    }
    while (angle > 180) {
      angle -= 360;
    }
    return angle;
  }

  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  public static Angle getLookAtAngles(Vector3d start, Vector3d end) {
    return AngleHelper.getAngleFacingInDegrees(end.subtract(start)).normalize();
  }

  public static Angle getLookAtAngles(Vector3d end) {
    return getLookAtAngles(EntityUtils.getEyePos(getLocalPlayer()), end);
  }

  public static Angle getLookAtAngles(Entity entity) {
    return getLookAtAngles(EntityUtils.getOBBCenter(entity));
  }

  public static double scale(
      double x, double from_min, double from_max, double to_min, double to_max) {
    return to_min + (to_max - to_min) * ((x - from_min) / (from_max - from_min));
  }

  public static <T> boolean isInRange(Collection<T> list, int index) {
    return list != null && index >= 0 && index < list.size();
  }

  public static <T> T defaultTo(T value, T defaultTo) {
    return value == null ? defaultTo : value;
  }

  public static List<ItemStack> getShulkerContents(ItemStack stack) { // TODO: move somewhere else
    NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
    CompoundNBT compound = stack.getTag();
    if (compound != null && compound.contains("BlockEntityTag", 10)) {
      CompoundNBT tags = compound.getCompound("BlockEntityTag");
      if (tags.contains("Items", 9)) {
        // load in the items
        ItemStackHelper.loadAllItems(tags, contents);
      }
    }
    return contents;
  }

  public static String createRegexFromGlob(String glob) {
    StringBuilder out = new StringBuilder("^");
    for (int i = 0; i < glob.length(); ++i) {
      final char c = glob.charAt(i);
      switch (c) {
        case '*':
          out.append(".*");
          break;
        case '?':
          out.append('.');
          break;
        case '.':
          out.append("\\.");
          break;
        case '\\':
          out.append("\\\\");
          break;
        default:
          out.append(c);
      }
    }
    out.append('$');
    return out.toString();
  }
}
