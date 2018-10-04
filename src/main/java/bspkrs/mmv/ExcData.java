/*
 * Copyright (C) 2014 bspkrs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bspkrs.mmv;

/*
Copyright (C) 2014 bspkrs
Portions Copyright (C) 2014 Alex "immibis" Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcData implements Comparable<ExcData> {
  private final String srgOwner;
  private final String srgName;
  private final String descriptor;
  private final String[] exceptions;
  private final String[] parameters;
  private final String[] paramTypes;

  public ExcData(
      String srgOwner,
      String srgName,
      String descriptor,
      String[] exceptions,
      String[] parameters) {
    this.srgOwner = srgOwner;
    this.srgName = srgName;
    this.descriptor = descriptor;
    this.exceptions = exceptions;
    paramTypes = splitMethodDesc(descriptor);
    this.parameters = parameters;
  }

  public ExcData(
      String srgOwner, String srgName, String descriptor, String[] exceptions, boolean isStatic) {
    this.srgOwner = srgOwner;
    this.srgName = srgName;
    this.descriptor = descriptor;
    this.exceptions = exceptions;
    paramTypes = splitMethodDesc(descriptor);
    parameters = genParamNames(ExcData.getSrgId(srgName), paramTypes, isStatic);
  }

  public String getSrgClassOwner() {
    return srgOwner;
  }

  public String getSrgMethodName() {
    return srgName;
  }

  public String getDescriptor() {
    return descriptor;
  }

  public String[] getExceptions() {
    return exceptions;
  }

  public String[] getParameters() {
    return parameters;
  }

  public String[] getParamTypes() {
    return paramTypes;
  }

  public boolean contains(String s) {
    if (srgName.contains(s)) return true;
    else for (String param : parameters) if (param.contains(s)) return true;

    return false;
  }

  @Override
  public int compareTo(ExcData o) {
    return srgName.compareTo(o.srgName);
  }

  public static String[] splitMethodDesc(String desc) {
    // \[*L[^;]+;|\[[ZBCSIFDJ]|[ZBCSIFDJ]
    int beginIndex = desc.indexOf('(');
    int endIndex = desc.lastIndexOf(')');
    if (((beginIndex == -1) && (endIndex != -1)) || ((beginIndex != -1) && (endIndex == -1))) {
      System.err.println(beginIndex);
      System.err.println(endIndex);
      throw new RuntimeException();
    }
    String x0;
    if ((beginIndex == -1) && (endIndex == -1)) {
      x0 = desc;
    } else {
      x0 = desc.substring(beginIndex + 1, endIndex);
    }
    Pattern pattern = Pattern.compile("\\[*L[^;]+;|\\[[ZBCSIFDJ]|[ZBCSIFDJ]");
    Matcher matcher = pattern.matcher(x0);

    ArrayList<String> listMatches = new ArrayList<String>();

    while (matcher.find()) {
      listMatches.add(matcher.group());
    }

    return listMatches.toArray(new String[listMatches.size()]);
  }

  public static String[] genParamNames(String srgId, String[] paramTypes, boolean isStatic) {
    boolean skip2 =
        (paramTypes.length >= 4)
            && paramTypes[0].equals("Ljava/lang/String;")
            && paramTypes[1].equals('I')
            && paramTypes[0].equals(paramTypes[2])
            && paramTypes[1].equals(paramTypes[3]);

    String[] ret = new String[paramTypes.length];
    int idOffset = isStatic ? 0 : 1;
    if (skip2) idOffset += 2;

    for (int i = 0; i < paramTypes.length; i++) {
      ret[i] = "p_" + srgId + "_" + (i + idOffset) + "_";
      if (paramTypes[i].equals("D") || paramTypes[i].equals("J")) idOffset++;
    }

    return ret;
  }

  public static String getSrgId(String srgName) {
    Pattern pattern = Pattern.compile("func_(i?[0-9]+)_");
    Matcher matcher = pattern.matcher(srgName);
    if (matcher.find()) return matcher.group(1);
    return srgName;
  }

  @Override
  public String toString() {
    return String.format(
        "  Owner: %s\n  SRG Name: %s\n  Descriptor: %s\n  Exceptions: %s\n  Parameters: %s\n  Param Types: %s",
        srgOwner,
        srgName,
        descriptor,
        Arrays.toString(exceptions),
        Arrays.toString(parameters),
        Arrays.toString(paramTypes));
  }
}
