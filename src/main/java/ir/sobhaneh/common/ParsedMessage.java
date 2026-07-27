//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.common;

import java.util.List;

public class ParsedMessage {

    private final String command;
    private final String[] args;
    private final String json;

    public ParsedMessage(String command, String[] args, String json) {
        this.command = command;
        this.args = args;
        this.json = json;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public String getJson() {
        return json;
    }

    public boolean hasJson() {
        return json != null;
    }

    public String getArg(int index) {
        if (index < 0 || index >= args.length) {
            return null;
        }
        return args[index];
    }

    @Override
    public String toString() {
        return "ParsedMessage{command='" + command + "', args=" + args + ", json=" + json + "}";
    }
}
