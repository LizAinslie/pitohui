package dev.lizainslie.pitohui.util.task

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Starts a repeating job that executes a task every [interval].
 *
 * @param interval The time interval in milliseconds between consecutive task executions.
 * @param dispatcher The [CoroutineDispatcher] on which the task will be executed. Defaults to [Dispatchers.Main].
 * @param task The suspending function (the work) to be executed periodically.
 *
 * @return The [Job] which can be cancelled to stop the repeating task.
 */
fun startRepeatingTask(
    interval: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    task: suspend () -> Unit
): Job {
    // It's recommended to use an appropriate CoroutineScope (e.g., applicationScope, viewModelScope)
    // and Dispatcher for your application context (e.g., Dispatchers.IO for network/disk work).
    return CoroutineScope(dispatcher).launch {
        while (isActive) { // Check if the coroutine scope is still active
            try {
                // Execute the task
                task()
            } catch (e: Exception) {
                // Handle exceptions in the task (e.g., logging)
                println("Error during periodic task: $e")
            }
            // Delay for the specified interval before the next iteration
            delay(interval)
        }
    }
}

fun startRepeatingTask(
    interval: kotlin.time.Duration,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    task: suspend () -> Unit
) =
    startRepeatingTask(interval.inWholeMilliseconds, dispatcher, task)

fun startRepeatingTask(
    interval: java.time.Duration,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    task: suspend () -> Unit
) =
    startRepeatingTask(interval.toMillis(), dispatcher, task)

