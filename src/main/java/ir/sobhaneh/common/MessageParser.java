//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageParser {

    public static ParsedMessage parse(String line) {
        if (line == null) {
            return null;
        }

        line = line.trim();
        if (line.isEmpty()) {
            return new ParsedMessage("", null, null);
        }

        int jsonStart = line.indexOf('{');
        String head;
        if (jsonStart >= 0) {
            head = line.substring(0, jsonStart).trim();
        } else head = line;
        String json;
        if(jsonStart >= 0) {
            json = line.substring(jsonStart).trim();
        } else json = null;

        String[] tokens = head.isEmpty() ? new String[0] : head.split("\\s+");

        String command = tokens.length > 0 ? tokens[0] : "";
        if(tokens.length > 1) {
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
        }else tokens = new  String[0];

        return new ParsedMessage(command, tokens, json);
    }

    public static String build(String command, String... args) {
        StringBuilder sb = new StringBuilder(command);
        for (String arg : args) {
            sb.append(' ').append(arg);
        }
        return sb.toString();
    }

    public static String build(String command, String[] args, String json) {
        StringBuilder sb = new StringBuilder(command);
        for (String arg : args) {
            sb.append(' ').append(arg);
        }
        if (json != null && !json.isEmpty()) {
            sb.append(' ').append(json);
        }
        return sb.toString();
    }
}
