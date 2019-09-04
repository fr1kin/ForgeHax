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

public class MemberSrgData {

  private final String obfOwner;
  private final String obfName;
  private final String srgOwner;
  private final String srgPkg;
  private final String srgName;
  private final boolean isClientOnly;

  public MemberSrgData(
    String obfOwner,
    String obfName,
    String srgOwner,
    String srgPkg,
    String srgName,
    boolean isClientOnly) {
    this.obfOwner = obfOwner;
    this.obfName = obfName;
    this.srgOwner = srgOwner;
    this.srgPkg = srgPkg;
    this.srgName = srgName;
    this.isClientOnly = isClientOnly;
  }

  public String getObfOwner() {
    return obfOwner;
  }

  public String getObfName() {
    return obfName;
  }

  public String getSrgOwner() {
    return srgOwner;
  }

  public String getSrgName() {
    return srgName;
  }

  public boolean isClientOnly() {
    return isClientOnly;
  }

  public String getSrgPkg() {
    return srgPkg;
  }

  public boolean contains(String s) {
    return srgName.contains(s) || obfName.contains(s);
  }
}
