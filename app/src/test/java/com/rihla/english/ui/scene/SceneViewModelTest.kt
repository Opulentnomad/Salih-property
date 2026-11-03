package com.rihla.english.ui.scene

import androidx.lifecycle.SavedStateHandle
import com.rihla.english.core.journey.Beat
import com.rihla.english.core.journey.JourneyProgress
import com.rihla.english.core.journey.JourneyRepository
import com.rihla.english.core.journey.Naturalness
import com.rihla.english.core.journey.Script
import com.rihla.english.util.AppClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SceneViewModelTest {
    private val repo = FakeRepo()

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(id: String) = SceneViewModel(SavedStateHandle(mapOf(SceneViewModel.ARG_SCENE_ID to id)), repo, Clock)

    private fun SceneViewModel.playAll() {
        var guard = 0
        while (state.value!!.summary == null && guard++ < 100) {
            val play = state.value!!.play
            val beat = play.beat
            if (beat is Beat.Reply && play.picked == null) {
                choose(beat.options.indexOfFirst { it.naturalness == Naturalness.NATURAL })
            }
            next()
        }
    }

    @Test fun `finishing a scene saves progress, notebook and summary`() = runTest {
        val scene = Script.chapters[0].scenes[0]
        val vm = vm(scene.id)
        vm.playAll()
        val s = vm.state.value!!
        assertNotNull(s.summary)
        assertFalse(s.chapterCompleted)
        assertEquals(Script.chapters[0].scenes[1].id, s.nextSceneId)
        assertTrue(scene.id in repo.state.value.completedScenes)
        assertEquals(scene.chunks.size, repo.state.value.notebook.size)
        assertEquals(2, repo.state.value.day(Clock.todayEpochDay()).seconds / 60)
    }

    @Test fun `saving a phrase mid-scene writes it immediately`() = runTest {
        val vm = vm(Script.chapters[0].scenes[0].id)
        while (vm.state.value!!.play.beat !is Beat.Line) vm.next()
        vm.saveChunk()
        assertEquals(1, repo.state.value.notebook.size)
    }

    @Test fun `last scene of a chapter completes the chapter`() = runTest {
        Script.chapters[0].scenes.forEach { sc -> vm(sc.id).playAll() }
        assertEquals(3, repo.state.value.completedScenes.size)
        val last = vm(Script.chapters[0].scenes.last().id).also { it.playAll() }
        assertTrue(last.state.value!!.chapterCompleted)
    }
}

private object Clock : AppClock {
    private var calls = 0L
    // Each scene "takes" two minutes: the first read is the start, later reads are 120 s on.
    override fun nowMillis(): Long = if (calls++ % 2 == 0L) 1_000_000L else 1_120_000L
    override fun todayEpochDay(): Long = 20_000L
    override fun fractionOfDay(): Double = 0.5
}

private class FakeRepo : JourneyRepository {
    val state = MutableStateFlow(JourneyProgress())
    override val progress: Flow<JourneyProgress> = state
    override suspend fun update(transform: (JourneyProgress) -> JourneyProgress) { state.value = transform(state.value) }
}
