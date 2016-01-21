package dictionary.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by harshit on 21/1/16.{}
 */
public class ShellCommands {

    private ShellCommands() {
    }

    private static ShellCommands shellCommands;

    public synchronized static ShellCommands getShellCommands() {
        if (shellCommands == null) {
            shellCommands = new ShellCommands();
        }
        return shellCommands;
    }

    String executeCommand(String command) {
        try {
            command = java.net.URLDecoder.decode(command, "UTF-8");
            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    command
            };

            Process proc = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            System.out.println("Here is the standard output of the command:\n");
            String s = null;
            StringBuilder stringBuilder = new StringBuilder("Output\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
                stringBuilder.append(s).append("\n");

            }
            stringBuilder.append("\nError\n");
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                stringBuilder.append(s).append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        ShellCommands shellCommands = new ShellCommands();
        System.out.print(shellCommands.executeCommand("ps aux | sort -rk 3,3 | head -n 6"));
    }
}
