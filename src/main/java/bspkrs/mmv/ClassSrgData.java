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

public class ClassSrgData implements Comparable<ClassSrgData> {
  
  public static enum SortType {
    PKG,
    OBF
  }
  
  private final String obfName;
  private final String srgName;
  private String srgPkgName;
  private final boolean isClientOnly;
  
  public static SortType sortType = SortType.PKG;
  
  public ClassSrgData(String obfName, String srgName, String srgPkgName, boolean isClientOnly) {
    this.obfName = obfName;
    this.srgName = srgName;
    this.srgPkgName = srgPkgName;
    this.isClientOnly = isClientOnly;
  }
  
  public String getObfName() {
    return this.obfName;
  }
  
  public String getSrgName() {
    return this.srgName;
  }
  
  public String getSrgPkgName() {
    return this.srgPkgName;
  }
  
  public ClassSrgData setSrgPkgName(String pkg) {
    this.srgPkgName = pkg;
    return this;
  }
  
  public boolean isClientOnly() {
    return isClientOnly;
  }
  
  public String getFullyQualifiedSrgName() {
    return srgPkgName + "/" + srgName;
  }
  
  @Override
  public int compareTo(ClassSrgData o) {
    if (sortType == SortType.PKG) {
      if (o != null) {
        return getFullyQualifiedSrgName().compareTo(o.getFullyQualifiedSrgName());
      } else {
        return 1;
      }
    } else if (o != null) {
      if (obfName.length() != o.obfName.length()) {
        return obfName.length() - o.obfName.length();
      } else {
        return obfName.compareTo(o.obfName);
      }
    } else {
      return 1;
    }
  }
  
  public boolean contains(String s) {
    return srgName.contains(s) || obfName.contains(s) || this.srgPkgName.contains(s);
  }
}
