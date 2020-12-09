package org.xrpl.xrpl4j.model.transactions;

import java.util.ArrayList;
import java.util.List;

public class AbstractFlagsTest {

  protected static List<Object[]> getBooleanCombinations(int flagCount) {
    // Every combination of 4 booleans
    List<Object[]> params = new ArrayList<>();
    for (int i = 0; i < Math.pow(2, flagCount); i++) {
      String bin = Integer.toBinaryString(i);
      while (bin.length() < flagCount) {
        bin = "0" + bin;
      }

      char[] chars = bin.toCharArray();
      Boolean[] booleans = new Boolean[flagCount];
      for (int j = 0; j < chars.length; j++) {
        booleans[j] = chars[j] == '0';
      }

      params.add(booleans);
    }

    return params;
  }
}
