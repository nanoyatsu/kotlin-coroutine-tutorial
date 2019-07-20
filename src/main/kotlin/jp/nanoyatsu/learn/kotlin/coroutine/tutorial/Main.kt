package jp.nanoyatsu.learn.kotlin.coroutine.tutorial

import kotlinx.coroutines.*

fun main() {
    first()
    bridging()
    bridging2()
    waiting()
    structuredConcurrency()
    refactor()
    scopeBuilder()
    coroutinesAreLight()
    globalCoroutinesAreLikeDaemonThreads()
}


// https://kotlinlang.org/docs/reference/coroutines/basics.html#your-first-coroutine
fun first() {
    // GlobalScope = アプリと同じ寿命のスコープ
    GlobalScope.launch {
        delay(1000L) // スレッドを止めない(->ノンブロッキング)待機
        println("World!")
    }
    println("Hello,")
    Thread.sleep(2000L) // スレッドを止める(->ブロッキング)待機

    // 「launchとThread，delayとsleepを入れ替えても動くで」
    // 「けど、片側だけ変えると『delayはコルーチン用のノンブロック系処理なんやが？』とかって怒られるで」
}

// https://kotlinlang.org/docs/reference/coroutines/basics.html#bridging-blocking-and-non-blocking-worlds
fun bridging() {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello? ")
    runBlocking {
        delay(2000L)
    }
    // first()と同じ動作
    // 「runBlockingはメインスレッドを止める」（その意味は一体）（ブロッキングじゃん）
}

fun bridging2() = runBlocking {
    GlobalScope.launch {
        delay(1000L)
        println("World!!")
    }
    println("Hello,")
    delay(2000L)
    // こう書いても良い
    // 「中断関数を書く」というのがこれに当たるらしい
}

// https://kotlinlang.org/docs/reference/coroutines/basics.html#waiting-for-a-job
fun waiting() = runBlocking {
    val job = GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello-job-wait,")
    job.join()  // <- 「子コルーチンが終わるまで待つ」
    // 神機能じゃん・・・これじゃん・・・
    // 「ｘｘ秒待つ(他の処理も止める/止めない)」で「お前それ意味あんの？」としてからのこれですよ
}

// https://kotlinlang.org/docs/reference/coroutines/basics.html#structured-concurrency
fun structuredConcurrency() = runBlocking {
    launch {
        delay(1000L)
        println("concurrency-World!")
    }
    println("Hello-structure,")
    // 「書いたコルーチン、それぞれ完了したかどうか判断して管理すんのしんどくない？」
    // 「外側のコルーチンは内側のコルーチンが終わるまで終わらんからこれでもええで」（これって機能としては嬉しいのかな）（安全ではありそう）
}

// https://kotlinlang.org/docs/reference/coroutines/basics.html#scope-builder
fun scopeBuilder() = runBlocking {
    // this: CoroutineScope
    launch {
        delay(200L)
        println("02. Task from runBlocking")
    }
    coroutineScope {
        launch {
            delay(500L)
            println("03. Task from nested launch")
        }
        delay(100L)
        println("01. Task from coroutine scope")
    }
    println("04. Coroutine scope is over") // こいついちばん遅いのマジ？？
    // delayの長さしか見てないと04->01->02->03になりそうなもん coroutineScope{}がwait付きみたいな感じなんか？？
    // "latter(coroutineScope) does not block the current thread while waiting for all children to complete."
    // 「CoroutineScopeは->全部の子が完了する/まで待つ間/今のスレッド/ブロックしない」ブロックしないんならなんで04が後なの・・・
}

// https://kotlinlang.org/docs/reference/coroutines/basics.html#extract-function-refactoring
fun refactor() = runBlocking {
    launch { doWorld() }
    println("Hello,")
}

suspend fun doWorld() {
    delay(1000L)
    println("refactor-World!")
    // 「実際に書くときはこうしようや」の図 suspendするとdelay()とか使えるわけですわ (訳)suspend : 一時停止する
}

// https://kotlinlang.org/docs/reference/coroutines/basics.html#coroutines-are-light-weight
fun coroutinesAreLight() = runBlocking {
    val jobs = arrayListOf<Job>()
    repeat(100_000) {
        val job = launch {
            delay(1000L)
            print(".")
        }
        jobs.add(job)
    }
    jobs.forEach { it.join() } // 改行入れたかったのでいじった（やりかた妥当かわからん）（さらにlaunchでくくってjoin()でもうごく）
    print("\n")
}

// https://kotlinlang.org/docs/reference/coroutines/basics.html#global-coroutines-are-like-daemon-threads
fun globalCoroutinesAreLikeDaemonThreads() = runBlocking {
    GlobalScope.launch {
        repeat(1000) {
            println("repeat : $it")
            delay(500L)
        }
    }
    delay(1300L)
    // 1000回printかと思ったら3回でした！！の話
    // なんかスコープの種類が重要なんだな・・・？（わかってない）というかんじ
    // だってstructuredConcurrencyのときと違うのGlobalScope指定してるかどうかだもんな　ムズカシイネ
}