/*
 * Copyright (C) 2014 bspkrs
 * Portions Copyright (C) 2014 Alex "immibis" Campbell
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ExcFile {

  public final Map<String, ExcData> srgMethodName2ExcData;
  public final Map<String, ExcData> srgParamName2ExcData;

  public ExcFile(File f) throws IOException {
    srgMethodName2ExcData = new HashMap<String, ExcData>();
    srgParamName2ExcData = new HashMap<String, ExcData>();
    // example lines:
    // net/minecraft/util/ChunkCoordinates=CL_00001555
    // net/minecraft/world/chunk/storage/AnvilChunkLoader.func_75816_a(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/Chunk;)V=net/minecraft/world/MinecraftException,java/io/IOException|p_75816_1_,p_75816_2_
    // net/minecraft/world/biome/BiomeGenMutated.func_150571_c(III)I=|p_150571_1_,p_150571_2_,p_150571_3_
    // net/minecraft/world/chunk/storage/AnvilChunkLoader.func_75818_b()V=|
    // net/minecraft/server/MinecraftServer.func_145747_a(Lnet/minecraft/util/IChatComponent;)V=|p_145747_1_

    Scanner in = new Scanner(new FileReader(f));
    try {
      while (in.hasNextLine()) {
        if (in.hasNext("#")) {
          in.nextLine();
          continue;
        }

        in.useDelimiter("\\.");
        String srgOwner = in.next();
        in.useDelimiter("\\(");
  
        if (!in.hasNext()) {
          if (in.hasNextLine()) {
            in.nextLine();
          } else {
            break;
          }
        }

        String srgName = in.next().substring(1);
        in.useDelimiter("=");
        String descriptor = in.next();
        in.useDelimiter("\\|");
        String excs = in.next().substring(1);
        String params = in.nextLine().substring(1);

        ExcData toAdd =
          new ExcData(
            srgOwner,
            srgName,
            descriptor,
            (excs.length() > 0 ? excs.split(",") : new String[0]),
            (params.length() > 0 ? params.split(",") : new String[0]));

        ExcData existing = srgMethodName2ExcData.get(srgName);

        if ((existing == null)
          || (existing.getParameters().length < toAdd.getParameters().length)) {
          srgMethodName2ExcData.put(srgName, toAdd);
  
          for (String parameter : toAdd.getParameters()) {
            srgParamName2ExcData.put(parameter, toAdd);
          }
        }
      }
    } finally {
      in.close();
    }
  }
}
