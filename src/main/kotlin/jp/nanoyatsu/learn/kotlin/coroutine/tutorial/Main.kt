package jp.nanoyatsu.learn.kotlin.coroutine.tutorial

import kotlinx.coroutines.*

fun main(args :Array<String>){
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    Thread.sleep(2000L)
}