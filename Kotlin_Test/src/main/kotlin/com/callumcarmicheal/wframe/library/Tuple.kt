package com.callumcarmicheal.wframe.library

class Tuple<T1, T2> {
    var x: T1? = null
    var y: T2? = null

    constructor() {}
    constructor(x: T1, y: T2) {
        this.x = x
        this.y = y
    }
}

class Tuple3<T1, T2, T3> {
    var x: T1? = null
    var y: T2? = null
    var z: T3? = null

    constructor() {}
    constructor(x: T1, y: T2, z: T3) {
        this.x = x
        this.y = y
        this.z = z
    }
}
