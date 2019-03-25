package com.callumcarmicheal.simplewebtest

import com.callumcarmicheal.wframe.Server
import org.apache.log4j.BasicConfigurator

private val PORT = 8080

fun main(args: Array<String>) {
    try {
//        BasicConfigurator.configure()

        System.err.println("Starting server!")

        val server = Server(PORT, "com.callumcarmicheal.simplewebtest.controllers")


        System.err.println("Server started on port: $PORT!")
    } catch (e: Exception) {
        System.err.println("Failed to start server!")
        e.printStackTrace()
    }
}