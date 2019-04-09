package com.bigteamseventeen.wpd2_ah.milestones.console;

public interface IConsoleCommand {
    
    /**
     * Get command name
     * @return
     */
    public String getCommand();

    /**
     * Get command description
     * @return
     */
    public String getDescription();

    /**
     * Get a parameter hint <text>
     * @return
     */
    public String getParameterHint();

    /**
     * Process the user command
     * @param processor
     * @param command
     */
    public void process(TerminalProcessor processor, String[] args);
}