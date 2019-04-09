package com.bigteamseventeen.wpd2_ah.milestones.console.commands;

import java.util.Map;

import com.bigteamseventeen.wpd2_ah.milestones.console.IConsoleCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.TerminalProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HelpCommand implements IConsoleCommand {
    final static Logger logger = LogManager.getLogger();

    @Override public String getCommand() {
        return "help";
    }

    @Override public String getDescription() {
        return "Displays all available commands";
    }

    @Override public String getParameterHint() {
        return null;
    }

    @Override public void process(TerminalProcessor processor, String[] args) {
        String output = "Available Commands:\n";

        // Get the list of commands
        Map<String, IConsoleCommand> commandMap = processor.getCommands();
        for (Map.Entry<String,IConsoleCommand> entry : commandMap.entrySet()) {
            IConsoleCommand command = entry.getValue();
            String hint = command.getParameterHint();

            if (hint == null)
                 output += String.format("  %s: %s\n", command.getCommand(), command.getDescription());
            else output += String.format("  %s: %s\n    < %s >\n", command.getCommand(), command.getDescription(), hint);
        }

        // Write our output
        logger.trace(output.substring(0, output.length()-1));
    }
}