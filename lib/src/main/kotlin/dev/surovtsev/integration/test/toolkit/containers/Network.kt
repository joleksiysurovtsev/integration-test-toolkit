package dev.surovtsev.integration.test.toolkit.containers

import org.testcontainers.containers.Network

class Network private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: Network? = null

        @Synchronized
        fun getInstance(): Network = INSTANCE ?: createNetwork().also { INSTANCE = it }

        private fun createNetwork(): Network = Network.newNetwork()
    }
}
