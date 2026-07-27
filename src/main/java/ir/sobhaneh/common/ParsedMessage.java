package ir.sobhaneh.common;

import java.util.List;

public class ParsedMessage {

    private final String command;
    private final List<String> args;
    private final String json;

    public ParsedMessage(String command, List<String> args, String json) {
        this.command = command;
        this.args = args;
        this.json = json;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getArgs() {
        return args;
    }

    public String getJson() {
        return json;
    }

    public boolean hasJson() {
        return json != null;
    }

    public String getArg(int index) {
        if (index < 0 || index >= args.size()) {
            return null;
        }
        return args.get(index);
    }

    @Override
    public String toString() {
        return "ParsedMessage{command='" + command + "', args=" + args + ", json=" + json + "}";
    }
}
