package dev.surovtsev.demo.app.service

import org.springframework.stereotype.Service

@Service
class GreetingService {
    
    fun getGreeting(name: String): String {
        return "Hello, $name!"
    }
}