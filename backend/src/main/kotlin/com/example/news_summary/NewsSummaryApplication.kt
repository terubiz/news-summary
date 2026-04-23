package com.example.news_summary

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableAsync
class NewsSummaryApplication

fun main(args: Array<String>) {
    runApplication<NewsSummaryApplication>(*args)
}
