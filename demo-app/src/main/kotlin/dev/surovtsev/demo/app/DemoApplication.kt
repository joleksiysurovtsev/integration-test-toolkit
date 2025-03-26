package dev.surovtsev.demo.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class DemoApplication {
    companion object {
        @JvmStatic
        @Suppress("SpreadOperator")
        fun main(args: Array<String>) {
            runApplication<DemoApplication>(*args)
        }
    }
}
