package com.callumcarmicheal.simplewebtest.controllers

import com.callumcarmicheal.wframe.Get

class IndexController {


    @Get("")
    fun index() {
        println("IndexController -> Index called!")

    }
}