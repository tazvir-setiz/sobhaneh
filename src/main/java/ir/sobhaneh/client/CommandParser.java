//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.client;

public class CommandParser {
    private static final String JSON_START_MARKER = "{";
    private static final String SPACE = " ";
    private static final String WHITESPACE_PATTERN = "\\s+";
    private static final String[] EMPTY_ARGS = new String[0];

    private final String command;
    private final String[] args;
    private final String json;

    public CommandParser(String line) {
        String trimmedLine = line.trim();

        this.json = extractJson(trimmedLine);
        String commandPart = removeJson(trimmedLine, json);

        this.command = extractCommand(commandPart);
        this.args = extractArgs(commandPart);
    }

    private String extractJson(String line) {
        int jsonIndex = line.indexOf(JSON_START_MARKER);
        if (jsonIndex == -1) {
            return null;
        }
        return line.substring(jsonIndex).trim();
    }

    private String removeJson(String line, String json) {
        if (json == null) {
            return line;
        }
        int jsonIndex = line.indexOf(JSON_START_MARKER);
        return line.substring(0, jsonIndex).trim();
    }

    private String extractCommand(String commandPart) {
        int firstSpace = commandPart.indexOf(SPACE);
        if (firstSpace == -1) {
            return commandPart;
        }
        return commandPart.substring(0, firstSpace);
    }

    private String[] extractArgs(String commandPart) {
        int firstSpace = commandPart.indexOf(SPACE);
        if (firstSpace == -1) {
            return EMPTY_ARGS;
        }
        String rest = commandPart.substring(firstSpace + 1).trim();
        if (rest.isEmpty()) {
            return EMPTY_ARGS;
        }
        return rest.split(WHITESPACE_PATTERN);
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

    fl
}