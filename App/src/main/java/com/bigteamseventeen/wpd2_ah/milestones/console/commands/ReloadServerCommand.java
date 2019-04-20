package com.bigteamseventeen.wpd2_ah.milestones.console.commands;

import com.bigteamseventeen.wpd2_ah.milestones.Main;
import com.bigteamseventeen.wpd2_ah.milestones.console.IConsoleCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.TerminalProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReloadServerCommand implements IConsoleCommand {
    static final Logger logger = LogManager.getLogger();

    @Override public String getCommand() {
        return "server/reload";
    }

    @Override public String getDescription() {
        return "Tell's the server to rescan for any changed web path's and discover new";
    }

    @Override public String getParameterHint() {
        return null;
    }

    @Override public void process(TerminalProcessor processor, String[] args) {
        logger.info("ReloadServerCommand: Reloading all route's.");
        Main.getServer().ReloadRouter();
    }
}