package com.bigteamseventeen.wpd2_ah.milestones.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bigteamseventeen.wpd2_ah.milestones.Main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecrell.terminalconsole.SimpleTerminalConsole;

public class TerminalProcessor extends SimpleTerminalConsole {
    final static Logger logger_con = LogManager.getLogger("console");
    final static Logger logger_tp = LogManager.getLogger();

    Map<String, IConsoleCommand> commands;

    public TerminalProcessor() {
        commands = new HashMap<String, IConsoleCommand>();
    }

    @Override
    protected boolean isRunning() {
        // TODO: Return true if your application is still running
        return true; // We dont expect to close down, atleast not yet
    }

    @Override
    protected void runCommand(String command) {
        // Our storage for the commands
        List<String> commandList = new ArrayList<>();

        // Use a regex to split the command into usable input
        String regex = "\"([^\"]*)\"|(\\S+)";
        Matcher m = Pattern.compile(regex).matcher(command);
        while (m.find()) {
            if (m.group(1) != null) 
                 commandList.add(m.group(1));
            else commandList.add(m.group(2));
        }

        // No input, skip it.
        if (commandList.size() == 0) return;

        // Create the array containing the command items
        String[] commands = new String[commandList.size()-1];
        System.arraycopy(commandList.toArray(), 1, commands, 0, commandList.size()-1);

        // Check if we have that command
        if (this.commands.containsKey(commandList.get(0).toLowerCase())) {
            // Invoke the method
            IConsoleCommand cmd = this.commands.get(commandList.get(0).toLowerCase());

            // Process the request
            cmd.process(this, commands);
        } else {
            logger_con.info("§cFailed to find command, type help to get a list of commands§r");
        }
    }

    @Override
    public void shutdown() {
        // CTRL+C was pressed or recieved SIG_INT
        if (Main.getServer() != null)
            Main.getServer().stop();
    }

    /**
     * Get console logger
     * @return
     */
    public Logger console() {
        return logger_con;
    }

    /**
     * Register a command
     * @param command
     */
    public TerminalProcessor registerCommand(IConsoleCommand command) {
        this.commands.put(command.getCommand().toLowerCase(), command);
        return this;
    }

    /**
     * Get a list of commands registered
     * @return
     */
    public Map<String, IConsoleCommand> getCommands() {
        return this.commands;
    }
}