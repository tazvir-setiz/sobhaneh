//in the name og ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.protocol;

import java.util.Arrays;

public class CommandParser {
    private CommandParser(){}
    public static Command parse(String line){
        if(line == null || line.isBlank()) return new Command(CommandType.UNKNOWN, new String[0]);
        String[] args = line.split(" ");
        CommandType type;
        switch (args[0]) {
            case "register":
                type = CommandType.REGISTER;
                break;
            case "login":
                type = CommandType.LOGIN;
                break;
            case "create-host":
                type = CommandType.CREATE_HOST;
                break;
            case "create-workspace":
                type = CommandType.CREATE_WORKSPACE;
                break;
            case "connect-workspace":
                type = CommandType.CONNECT_WORKSPACE;
                break;
            case "check":
                type = CommandType.CHECK;
                break;
            default:
                type = CommandType.UNKNOWN;
                break;
        }
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        return new Command(type,commandArgs);
    }
}
