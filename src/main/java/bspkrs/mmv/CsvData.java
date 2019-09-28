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

public class CsvData implements Comparable<CsvData> {
  
  private final String srgName;
  private String mcpName;
  private final int side;
  private String comment;
  private boolean needsQuoted;
  
  public CsvData(String srgName, String mcpName, int side, String comment) {
    this.srgName = srgName;
    this.mcpName = mcpName;
    this.side = side;
    
    if (comment.contains(",")
        || (!comment.isEmpty()
        && comment.charAt(0) == '"'
        && comment.charAt(comment.length() - 1) == '"')) {
      needsQuoted = true;
      if (comment.charAt(0) == '"' && comment.charAt(comment.length() - 1) == '"') {
        this.comment = comment.substring(1, comment.length() - 1);
      } else {
        this.comment = comment;
      }
    } else {
      this.comment = comment;
      needsQuoted = false;
    }
  }
  
  public String toCsv() {
    return srgName
        + ","
        + mcpName
        + ","
        + side
        + ","
        + (needsQuoted ? "\"" + comment + "\"" : comment);
  }
  
  public String getSrgName() {
    return srgName;
  }
  
  public String getMcpName() {
    return mcpName;
  }
  
  public CsvData setMcpName(String mcpName) {
    this.mcpName = mcpName;
    return this;
  }
  
  public int getSide() {
    return side;
  }
  
  public String getComment() {
    return comment;
  }
  
  public CsvData setComment(String comment) {
    this.comment = comment;
    return this;
  }
  
  @Override
  public int compareTo(CsvData o) {
    if (o != null) {
      return this.srgName.compareTo(o.srgName);
    }
    
    return 1;
  }
  
  public boolean contains(String s) {
    return this.mcpName.contains(s) || this.comment.contains(s);
  }
}
