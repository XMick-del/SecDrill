package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.TacticalSoundEffect
import com.example.audio.TacticalSoundManager
import com.example.model.DrillDifficulty
import com.example.model.SecurityDomain
import com.example.model.TacticalTier
import com.example.model.TacticalViewMode
import com.example.viewmodel.TacticalDrillViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SecPlus Drill", appName)
    }

    @Test
    fun `difficulty selection filters questions and scales point scoring`() {
        TacticalSoundManager.setMuted(false)
        val viewModel = TacticalDrillViewModel()

        // Select EASY difficulty (100 base points)
        viewModel.selectDifficulty(DrillDifficulty.EASY)
        assertEquals(DrillDifficulty.EASY, viewModel.uiState.value.selectedDifficulty)
        assertTrue(viewModel.uiState.value.filteredDeck.all { it.difficulty == DrillDifficulty.EASY })

        val easyQuestion = viewModel.uiState.value.currentQuestion
        viewModel.selectOption(easyQuestion.correctIndex)
        viewModel.executeEvaluation()
        assertEquals(100, viewModel.uiState.value.scorePoints)

        // Select MEDIUM difficulty (200 base points + 1x30 streak bonus = 230 points -> total 330)
        viewModel.selectDifficulty(DrillDifficulty.MEDIUM)
        assertEquals(DrillDifficulty.MEDIUM, viewModel.uiState.value.selectedDifficulty)
        assertTrue(viewModel.uiState.value.filteredDeck.all { it.difficulty == DrillDifficulty.MEDIUM })

        val mediumQuestion = viewModel.uiState.value.currentQuestion
        viewModel.selectOption(mediumQuestion.correctIndex)
        viewModel.executeEvaluation()
        assertEquals(330, viewModel.uiState.value.scorePoints)

        // Select HARD difficulty (350 base points + 2x50 streak bonus = 450 points -> total 780)
        viewModel.selectDifficulty(DrillDifficulty.HARD)
        assertEquals(DrillDifficulty.HARD, viewModel.uiState.value.selectedDifficulty)
        assertTrue(viewModel.uiState.value.filteredDeck.all { it.difficulty == DrillDifficulty.HARD })

        val hardQuestion = viewModel.uiState.value.currentQuestion
        viewModel.selectOption(hardQuestion.correctIndex)
        viewModel.executeEvaluation()
        assertEquals(780, viewModel.uiState.value.scorePoints)
    }

    @Test
    fun `tactical sound manager synthesizes valid PCM buffers and supports mute toggle`() {
        TacticalSoundEffect.entries.forEach { effect ->
            val pcm = TacticalSoundManager.synthesizeWaveform(effect)
            assertTrue("Expected non-empty PCM buffer for $effect", pcm.isNotEmpty())
        }

        val viewModel = TacticalDrillViewModel()
        val initialAudio = viewModel.uiState.value.isAudioEnabled
        viewModel.toggleAudioFeedback()
        assertEquals(!initialAudio, viewModel.uiState.value.isAudioEnabled)
        if (!viewModel.uiState.value.isAudioEnabled) {
            viewModel.toggleAudioFeedback()
        }
        assertTrue(viewModel.uiState.value.isAudioEnabled)
    }

    @Test
    fun `proficiency dashboard mode switching, series filtering, and snapshot logging work`() {
        val viewModel = TacticalDrillViewModel()
        assertEquals(TacticalViewMode.DRILL_DECK, viewModel.uiState.value.viewMode)

        viewModel.setViewMode(TacticalViewMode.PROFICIENCY_DASHBOARD)
        assertEquals(TacticalViewMode.PROFICIENCY_DASHBOARD, viewModel.uiState.value.viewMode)

        viewModel.toggleChartDomainSeries("2.0")
        assertFalse(viewModel.uiState.value.enabledChartDomains.contains("2.0"))
        viewModel.enableAllChartDomains()
        assertEquals(5, viewModel.uiState.value.enabledChartDomains.size)

        val beforeAvg = viewModel.uiState.value.overallProficiencyIndex
        viewModel.logCalibrationSnapshot()
        assertTrue(viewModel.uiState.value.overallProficiencyIndex >= beforeAvg)

        viewModel.drillSpecificDomain(SecurityDomain.DOM_3)
        assertEquals(TacticalViewMode.DRILL_DECK, viewModel.uiState.value.viewMode)
        assertEquals(SecurityDomain.DOM_3, viewModel.uiState.value.selectedDomain)
    }

    @Test
    fun `accessing advanced tier or AI infiltration triggers paywall intercept protocol and alert sound`() {
        val viewModel = TacticalDrillViewModel()
        assertFalse(viewModel.uiState.value.hasOmegaClearance)
        assertNull(viewModel.uiState.value.activePaywallReason)

        viewModel.attemptSelectTier(TacticalTier.TIER_2)
        assertNotNull(viewModel.uiState.value.activePaywallReason)
        assertEquals(TacticalSoundEffect.PAYWALL_INTERCEPT.code, viewModel.uiState.value.lastSoundEventCode)

        viewModel.authorizeClearanceAndProceed()
        assertTrue(viewModel.uiState.value.hasOmegaClearance)
        assertNull(viewModel.uiState.value.activePaywallReason)
        assertEquals(TacticalTier.TIER_2, viewModel.uiState.value.selectedTier)
        assertEquals(TacticalSoundEffect.CLEARANCE_GRANTED.code, viewModel.uiState.value.lastSoundEventCode)
    }
}
