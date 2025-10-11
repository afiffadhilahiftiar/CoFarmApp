package com.example.contractfarmingapp.utils;

import java.util.HashMap;
import java.util.Map;

public class EmvQrisParser {

    public static Map<String, String> parseEMV(String emvData) {
        Map<String, String> result = new HashMap<>();
        int index = 0;

        while (index + 4 <= emvData.length()) {
            try {
                String tag = emvData.substring(index, index + 2);
                int length = Integer.parseInt(emvData.substring(index + 2, index + 4));
                int valueStart = index + 4;
                int valueEnd = valueStart + length;

                if (valueEnd > emvData.length()) break;

                String value = emvData.substring(valueStart, valueEnd);
                result.put(tag, value);
                index = valueEnd;
            } catch (Exception e) {
                break;
            }
        }

        return result;
    }
}
