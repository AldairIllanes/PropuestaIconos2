package com.example.propuestaiconos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform