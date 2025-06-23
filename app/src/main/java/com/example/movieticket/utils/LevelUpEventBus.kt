package com.example.movieticket.utils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class LevelUpEventBus @Inject constructor() {
    private val _flow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val levelUpFlow = _flow.asSharedFlow()

    suspend fun emitLevelUp(level: String) = _flow.emit(level)
}