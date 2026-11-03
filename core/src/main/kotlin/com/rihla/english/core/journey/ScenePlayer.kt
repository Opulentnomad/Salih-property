package com.rihla.english.core.journey

import com.rihla.english.core.model.ErrorPattern

/** Immutable playback state of one scene. */
data class SceneState(
    val scene: Scene,
    val beatIndex: Int = 0,
    /** Option picked at the current reply beat (null until the learner chooses). */
    val picked: ReplyOption? = null,
    /** First-attempt naturalness per reply beat index. */
    val firstReplies: Map<Int, Naturalness> = emptyMap(),
    val shadowsDone: Int = 0,
    val slips: List<ErrorPattern> = emptyList(),
    val savedChunkIds: Set<String> = emptySet(),
) {
    val beat: Beat? get() = scene.beats.getOrNull(beatIndex)
    val isFinished: Boolean get() = beatIndex >= scene.beats.size
    val progress: Float get() = if (scene.beats.isEmpty()) 1f else beatIndex.toFloat() / scene.beats.size

    /** True when the learner may move to the next beat. */
    val canContinue: Boolean
        get() = when (val b = beat) {
            null -> false
            is Beat.Reply -> picked != null && picked.naturalness != Naturalness.UNNATURAL
            else -> true
        }

    val naturalFirstReplies: Int get() = firstReplies.values.count { it == Naturalness.NATURAL }
}

/** Pure state machine for a scene: the UI drives it, unit tests verify it. */
object ScenePlayer {

    fun start(scene: Scene) = SceneState(scene)

    fun next(state: SceneState): SceneState {
        if (!state.canContinue) return state
        val shadow = if (state.beat is Beat.Shadow) 1 else 0
        return state.copy(beatIndex = state.beatIndex + 1, picked = null, shadowsDone = state.shadowsDone + shadow)
    }

    /** "Can't speak right now": move past a shadowing beat without counting it. */
    fun skipShadow(state: SceneState): SceneState =
        if (state.beat is Beat.Shadow) state.copy(beatIndex = state.beatIndex + 1, picked = null) else state

    fun choose(state: SceneState, optionIndex: Int): SceneState {
        val reply = state.beat as? Beat.Reply ?: return state
        val option = reply.options.getOrNull(optionIndex) ?: return state
        val first = if (state.beatIndex in state.firstReplies) state.firstReplies
        else state.firstReplies + (state.beatIndex to option.naturalness)
        val slips = if (option.naturalness == Naturalness.UNNATURAL && option.pattern != null) state.slips + option.pattern else state.slips
        return state.copy(picked = option, firstReplies = first, slips = slips)
    }

    fun saveChunk(state: SceneState, chunkId: String): SceneState =
        state.copy(savedChunkIds = if (chunkId in state.savedChunkIds) state.savedChunkIds - chunkId else state.savedChunkIds + chunkId)
}
