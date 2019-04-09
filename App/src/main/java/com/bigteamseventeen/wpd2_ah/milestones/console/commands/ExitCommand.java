package com.bigteamseventeen.wpd2_ah.milestones.console.commands;

import com.bigteamseventeen.wpd2_ah.milestones.console.IConsoleCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.TerminalProcessor;

public class ExitCommand implements IConsoleCommand {
    @Override public String getCommand() {
        return "exit";
    }

    @Override public String getDescription() {
        return "Exist the application";
    }

    @Override public String getParameterHint() {
        return null;
    }

    @Override public void process(TerminalProcessor processor, String[] args) {
        processor.shutdown();
    }
}