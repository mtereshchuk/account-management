package com.mtereshchuk.account

import com.mtereshchuk.account.controller.AccountController
import com.mtereshchuk.account.service.AccountServiceImpl
import io.javalin.Javalin
import io.javalin.json.JavalinJackson

/**
 * @author mtereshchuk
 */
const val STATIC_FILES = "/static"
const val DEFAULT_PORT = 8080

object App {
    fun create(): Javalin {
        val app = Javalin.create {
            it.jsonMapper(JavalinJackson())
            it.staticFiles.add(STATIC_FILES)
        }

        val service = AccountServiceImpl()
        val controller = AccountController(service)
        controller.registerRoutes(app)

        return app
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val port = args.firstOrNull()?.toInt() ?: DEFAULT_PORT
        create().start(port)
    }
}
