package com.callumcarmicheal.wframe


// create a custom Annotation
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Get(val value: String)

// create a custom Annotation
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Post(val value: String)

//// create a custom Annotation
//@Retention(AnnotationRetention.RUNTIME)
//@Target(AnnotationTarget.CLASS)
//annotation class Controller()
