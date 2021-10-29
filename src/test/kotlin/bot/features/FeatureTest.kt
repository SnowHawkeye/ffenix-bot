package bot.features

import bot.features.core.FeatureDataContract
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
internal class FeatureTest {
    @Mock
    val contract: FeatureDataContract = Mockito.mock(FeatureDataContract::class.java)

    @Spy
    val testFeature = object : Feature() {
        override val featureDataContract: FeatureDataContract
            get() = contract

        override suspend fun Kord.addFeature() {}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should call global data initializer`() = runBlockingTest {
        // WHEN
        testFeature.initializeGlobalData()

        // THEN
        Mockito.verify(contract).initializeGlobalData(testFeature)
    }


    @Mock
    val guild: Guild = Mockito.mock(Guild::class.java)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should call guild data initializer`() = runBlockingTest {
        // WHEN
        testFeature.initializeGuildData(guild)

        // THEN
        Mockito.verify(contract).initializeGuildData(testFeature, guild)
    }
}