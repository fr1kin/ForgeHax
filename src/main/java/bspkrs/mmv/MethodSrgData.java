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

public class MethodSrgData extends MemberSrgData implements Comparable<MethodSrgData> {
  
  private final String obfDescriptor;
  private final String srgDescriptor;
  
  public MethodSrgData(
      String obfOwner,
      String obfName,
      String obfDescriptor,
      String srgOwner,
      String srgPkg,
      String srgName,
      String srgDescriptor,
      boolean isClientOnly) {
    super(obfOwner, obfName, srgOwner, srgPkg, srgName, isClientOnly);
    this.obfDescriptor = obfDescriptor;
    this.srgDescriptor = srgDescriptor;
  }
  
  public String getObfDescriptor() {
    return obfDescriptor;
  }
  
  public String getSrgDescriptor() {
    return srgDescriptor;
  }
  
  @Override
  public int compareTo(MethodSrgData o) {
    if (o != null) {
      return getSrgName().compareTo(o.getSrgName());
    } else {
      return 1;
    }
  }
}
