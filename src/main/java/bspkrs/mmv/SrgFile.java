/*
 * Copyright (C) 2014 Alex "immibis" Campbell, bspkrs
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
 *
 * Modified version of SrgFile.java from BON
 */
package bspkrs.mmv;

/*
Copyright (C) 2014 bspkrs
Portions Copyright (C) 2014 Alex "immibis" Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SrgFile {

  // All maps should be inter-connected to reference a single set of objects
  public final Map<String, ClassSrgData> srgClassName2ClassData =
    new TreeMap<String, ClassSrgData>(); // full/pkg/ClassSrgName -> ClassSrgData
  public final Map<String, Set<ClassSrgData>> srgPkg2ClassDataSet =
    new TreeMap<String, Set<ClassSrgData>>(); // full/pkg -> Set<ClassSrgData>
  public final Map<String, FieldSrgData> srgFieldName2FieldData =
    new TreeMap<String, FieldSrgData>(); // field_12345_a -> FieldSrgData
  public final Map<String, MethodSrgData> srgMethodName2MethodData =
    new TreeMap<String, MethodSrgData>(); // func_12345_a -> MethodSrgData
  public final Map<ClassSrgData, Set<MethodSrgData>> class2MethodDataSet =
    new TreeMap<ClassSrgData, Set<MethodSrgData>>();
  public final Map<ClassSrgData, Set<FieldSrgData>> class2FieldDataSet =
    new TreeMap<ClassSrgData, Set<FieldSrgData>>();
  public final Map<String, ClassSrgData> srgMethodName2ClassData =
    new TreeMap<String, ClassSrgData>(); // func_12345_a -> ClassSrgData
  public final Map<String, ClassSrgData> srgFieldName2ClassData =
    new TreeMap<String, ClassSrgData>(); // field_12345_a -> ClassSrgData

  public static String getLastComponent(String s) {
    String[] parts = s.split("/");
    return parts[parts.length - 1];
  }

  public SrgFile(File f, ExcFile excFile, StaticMethodsFile staticMethods) throws IOException {
    Scanner in = new Scanner(new BufferedReader(new FileReader(f)));
    try {
      while (in.hasNextLine()) {
        if (in.hasNext("CL:")) {
          // CL: a net/minecraft/util/EnumChatFormatting
          in.next(); // skip CL:
          String obf = in.next();
          String deobf = in.next();
          String srgName = getLastComponent(deobf);
          String pkgName = deobf.substring(0, deobf.lastIndexOf('/'));

          ClassSrgData classData = new ClassSrgData(obf, srgName, pkgName, in.hasNext("#C"));
  
          if (!srgPkg2ClassDataSet.containsKey(pkgName)) {
            srgPkg2ClassDataSet.put(pkgName, new TreeSet<ClassSrgData>());
          }
          srgPkg2ClassDataSet.get(pkgName).add(classData);

          srgClassName2ClassData.put(pkgName + "/" + srgName, classData);
  
          if (!class2MethodDataSet.containsKey(classData)) {
            class2MethodDataSet.put(classData, new TreeSet<MethodSrgData>());
          }
  
          if (!class2FieldDataSet.containsKey(classData)) {
            class2FieldDataSet.put(classData, new TreeSet<FieldSrgData>());
          }
        } else if (in.hasNext("FD:")) {
          // FD: aql/c net/minecraft/block/BlockStoneBrick/field_94408_c #C
          in.next(); // skip FD:
          String[] obf = in.next().split("/");
          String obfOwner = obf[0];
          String obfName = obf[1];
          String deobf = in.next();
          String srgName = getLastComponent(deobf);
          String srgPkg = deobf.substring(0, deobf.lastIndexOf('/'));
          String srgOwner = getLastComponent(srgPkg);
          srgPkg = srgPkg.substring(0, srgPkg.lastIndexOf('/'));

          FieldSrgData fieldData =
            new FieldSrgData(obfOwner, obfName, srgOwner, srgPkg, srgName, in.hasNext("#C"));

          srgFieldName2FieldData.put(srgName, fieldData);
          class2FieldDataSet
            .get(srgClassName2ClassData.get(srgPkg + "/" + srgOwner))
            .add(fieldData);
          srgFieldName2ClassData.put(srgName, srgClassName2ClassData.get(srgPkg + "/" + srgOwner));
        } else if (in.hasNext("MD:")) {
          // MD: aor/a (Lmt;)V net/minecraft/block/BlockHay/func_94332_a
          // (Lnet/minecraft/client/renderer/texture/IconRegister;)V #C
          in.next(); // skip MD:
          String[] obf = in.next().split("/");
          String obfOwner = obf[0];
          String obfName = obf[1];
          String obfDescriptor = in.next();
          String deobf = in.next();
          String srgName = getLastComponent(deobf);
          String srgPkg = deobf.substring(0, deobf.lastIndexOf('/'));
          String srgOwner = getLastComponent(srgPkg);
          srgPkg = srgPkg.substring(0, srgPkg.lastIndexOf('/'));
          String srgDescriptor = in.next();

          MethodSrgData methodData =
            new MethodSrgData(
              obfOwner,
              obfName,
              obfDescriptor,
              srgOwner,
              srgPkg,
              srgName,
              srgDescriptor,
              in.hasNext("#C"));

          srgMethodName2MethodData.put(srgName, methodData);
          class2MethodDataSet
            .get(srgClassName2ClassData.get(srgPkg + "/" + srgOwner))
            .add(methodData);
          srgMethodName2ClassData.put(srgName, srgClassName2ClassData.get(srgPkg + "/" + srgOwner));

          // Hack in the missing parameter data
          ExcData toAdd =
            new ExcData(
              srgOwner, srgName, srgDescriptor, new String[0], staticMethods.contains(srgName));
          ExcData existing = excFile.srgMethodName2ExcData.get(srgName);

          if ((existing == null)
            || (existing.getParameters().length < toAdd.getParameters().length)) {
            excFile.srgMethodName2ExcData.put(srgName, toAdd);
            for (String parameter : toAdd.getParameters()) {
              excFile.srgParamName2ExcData.put(parameter, toAdd);
            }
          }
        } else {
          in.nextLine();
        }
      }
    } finally {
      in.close();
    }
  }
}
