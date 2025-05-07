package org.tfv.deskflow.client.util

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class SingletonThreadExecutor(val threadName: String) : AbstractDisposable() {
  private val threadLock = Any()

  private var thread: Thread? = null
  private val threadFactory = ThreadFactory { r ->
    synchronized(threadLock) {
      if (thread == null)
        thread = Thread(r, threadName)

      return@ThreadFactory thread
    }
  }

  val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1, threadFactory)

  val isExecutorThread: Boolean
    get() = Thread.currentThread() == thread
  
  @Suppress("DiscouragedApi")
  fun scheduleAtFixedRate(runnable: Runnable, period: Long, initialDelay: Long = 0, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
    executor.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit)
  }
  
  fun scheduleWithFixedDelay(runnable: Runnable, delay: Long, initialDelay: Long = 0, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
    executor.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit)
  }
  
  fun schedule(runnable: Runnable, delay: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
    executor.schedule(runnable, delay, timeUnit)
  }
  
  fun <T> submit(runnable: () -> T): Future<T>? {
    synchronized(threadLock) {
      if (executor.isShutdown) {
        return null
      }
    }
    return executor.submit<T>(runnable)
  }

  fun <T> submit(callable: Callable<T>): Future<T>? {
    synchronized(threadLock) {
      if (executor.isShutdown) {
        return null
      }
    }
    return executor.submit(callable)
  }

  fun shutdown() {
    synchronized(threadLock) {
      if (executor.isShutdown) {
        return
      }
      executor.shutdownNow()
    }
  }

  override fun onDispose() {
    shutdown()
  }

}