package com.bigteamseventeen.wpd2_ah.milestones;

import com.callumcarmicheal.wframe.Server;
import com.callumcarmicheal.wframe.ViewUtil;

public class ViewUtility extends ViewUtil {
    /**
     * Checks if the server is debugging
     * @return
     */
    public boolean isDebugging() {
        // Return the isDebugging 
        return Server.IsDebugging();
    }
}