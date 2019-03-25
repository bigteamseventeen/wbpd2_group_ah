package com.callumcarmicheal.wframe


import com.callumcarmicheal.wframe.library.Tuple
import com.callumcarmicheal.wframe.library.Tuple3
import com.sun.net.httpserver.HttpServer
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.InvocationTargetException

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.InetSocketAddress
import java.util.ArrayList

class Server {
    private val __THREAD_COUNT = 4
    private var controllersPackage: String? = null
    private var server: HttpServer? = null
    private var started = false
    private val router = HashMap<String, ControllerMethodPair>()

    private enum class RequestType { GET, POST }
    private inner class ControllerMethodPair {
        var GetInstance: Any? = null
        var Get: Method? = null
        var PostInstance: Any? = null
        var Post: Method? = null
    }

    constructor(Port: Int, Package: String) {
        controllersPackage = Package

        setupRouter()
        server = HttpServer.create(InetSocketAddress(Port), 0)
    }

    private fun setupRouter() {
        println("Server: Indexing Controllers")

        val reflections = Reflections(
            ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(controllersPackage))
                .setScanners(
                    SubTypesScanner(false),
                    TypeAnnotationsScanner(),
                    MethodAnnotationsScanner()))

        val getMethods = reflections.getMethodsAnnotatedWith(Get::class.java)
        val postMethods = reflections.getMethodsAnnotatedWith(Post::class.java)
        val paths = HashMap<String, Tuple<RequestType, Method>>()
        val classes = HashMap<Class<*>, ArrayList<Tuple3<String, Method, RequestType>>>()
        val instances = HashMap<Class<*>, Any>()

        getMethods.forEach {
            val c    = it.declaringClass
            val g    = it.getAnnotation(Get::class.java)
            val path = g.value

            if (!Modifier.isPublic(it.modifiers)) {
                System.err.println("AdvancedServer: ERROR Method needs to be public!")
                System.err.println("    Route: GET $path")
                System.err.println("    at " + Package(it.toGenericString()))
                System.exit(1)
            }

            if (paths.containsKey(path)) {
                val rt = paths[path]!!

                if (rt.x === RequestType.GET) {
                    System.err.println("AdvancedServer: WARNING Duplicate value's resolution")
                    System.err.println("    Request Type: GET")
                    System.err.println("    Methods are conflicting for value: $path")
                    System.err.println("    Method 1: " + Package(it.toGenericString()))
                    System.err.println("    Method 2: " + Package(rt.y!!.toGenericString()))
                    System.exit(1)
                }
            } else {
                paths[path] = Tuple(RequestType.GET, it)
            }

            if (!classes.containsKey(c))
                classes[c] = ArrayList()

            classes[c]!!.add(Tuple3(path, it, RequestType.GET))
        }

        postMethods.forEach {
            val m = it
            val c = m.declaringClass
            val p = m.getAnnotation(Post::class.java)
            val path = p.value

            if (!Modifier.isPublic(m.modifiers)) {
                System.err.println("AdvancedServer: ERROR Method needs to be public!")
                System.err.println("    Route: POST $path")
                System.err.println("    at " + m.toGenericString())
                System.exit(1)
            }

            if (paths.containsKey(path)) {
                val rt = paths[path]!!

                if (rt.x === RequestType.POST) {
                    System.err.println("AdvancedServer: WARNING Duplicate value's resolution")
                    System.err.println("    Request Type: POST")
                    System.err.println("    Methods are conflicting for value: $path")
                    System.err.println("    Method 1: " + Package(m.toGenericString()))
                    System.err.println("    Method 2: " + Package(rt.y!!.toGenericString()))
                    System.exit(1)
                }
            } else {
                paths[path] = Tuple(RequestType.POST, m)
            }

            if (!classes.containsKey(c))
                classes[c] = ArrayList()

            classes[c]!!.add(Tuple3(path, m, RequestType.POST))
        }


        // We are now generate value controller list.
        for (k in classes.keys) {
            val arrayList = classes[k]
            var inst: Any? = null

            if (!instances.containsKey(k)) {
                try {
                    val ctor = k.getDeclaredConstructor()
                    inst = ctor.newInstance() as Any
                    instances[k] = inst
                } catch (e: NoSuchMethodException) {
                    System.err.println("Failed to find constructor or create instance for controller class.")
                    System.err.println("    " + k.canonicalName)
                    e.printStackTrace()
                    System.exit(1)
                } catch (e: InstantiationException) {
                    System.err.println("Failed to find constructor or create instance for controller class.")
                    System.err.println("    " + k.canonicalName)
                    e.printStackTrace()
                    System.exit(1)
                } catch (e: IllegalAccessException) {
                    System.err.println("Failed to find constructor or create instance for controller class.")
                    System.err.println("    " + k.canonicalName)
                    e.printStackTrace()
                    System.exit(1)
                } catch (e: InvocationTargetException) {
                    System.err.println("Failed to find constructor or create instance for controller class.")
                    System.err.println("    " + k.canonicalName)
                    e.printStackTrace()
                    System.exit(1)
                }

            }

            if (inst == null)
                inst = instances[k]

            if (arrayList != null) {
                for (t in arrayList) {
                    val path: String? = t.x
                    val method: Method? = t.y
                    val type: Server.RequestType? = t.z
                    var cmp: ControllerMethodPair? = null

                    if (router.containsKey(t.x)) {
                        cmp = router.get(t.x)
                        if (type == null) {
                            System.err.println("AdvancedServer: WARNING Ignoring request due to unexpected null object")
                            System.err.println("    The request type is null")
                            System.err.println("    at " + Package(method!!.toGenericString()))
                        } else {
                            when (t.z) {
                                RequestType.GET -> if (cmp!!.Get != null) {
                                    System.err.println("AdvancedServer: WARNING Duplicate method resolution")
                                    System.err.println("    Methods are conflicting for value: " + t.x)
                                    System.err.println("    Method 1: " + Package(cmp.Get!!.toGenericString()))
                                    System.err.println("    Method 2: " + Package(method!!.toGenericString()))
                                    System.exit(1)
                                }
                                RequestType.POST -> if (cmp!!.Post != null) {
                                    System.err.println("AdvancedServer: WARNING Duplicate method resolution")
                                    System.err.println("    Methods are conflicting for value: " + t.x)
                                    System.err.println("    Method 1: " + Package(cmp.Post!!.toGenericString()))
                                    System.err.println("    Method 2: " + Package(method!!.toGenericString()))
                                    System.exit(1)
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("Failed to query methods of class")
                System.exit(1)
            }
        }
    }

    fun Package(s: String): String {
        return s.replace("\\B\\w+(\\.[a-z])".toRegex(), "$1")
    }
}