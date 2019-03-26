package com.bigteamseventeen.g1.milestones;

import java.sql.Connection;

import com.callumcarmicheal.wframe.Server;
import com.callumcarmicheal.wframe.database.querybuilder.CType;
import com.callumcarmicheal.wframe.database.querybuilder.QueryResults;

import org.apache.log4j.BasicConfigurator;
import org.sqlite.core.DB;

public class Main {
    private static final String CONTROLLERSPACKAGE = "com.bigteamseventeen.g1.milestones.controllers";
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting BigTeamSeventeen WPDB2 Group 1: Milestones");
    }
}
