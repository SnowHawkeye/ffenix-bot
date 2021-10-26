package bot.features.data_contract

import bot.features.Feature
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import kotlin.test.assertEquals

internal class StoragePathHelperTest {

    @Mock
    val dataSource: FeatureDataContractDataSource = Mockito.mock(FeatureDataContractDataSource::class.java)

    private val testFeature = object : Feature() {
        override val name: String = "TestFeature"
        override val featureDataContract: FeatureDataContract
            get() = RequiresData.global(
                dataSource = dataSource,
                createNewData = { "" },
                updateExistingData = { "" },
            )

        override suspend fun Kord.addFeature() {}
    }

    @Test
    fun `Should return correct global repository path`() {
        // WHEN
        val result = StoragePathHelper.globalStorageRepositoryPath(testFeature)

        // THEN
        assertEquals(
            expected = "/data/global/testfeature/",
            actual = result
        )
    }

    @Test
    fun `Should return correct guild repository path`() {
        // GIVEN
        val guildId = Snowflake(1000000000)

        // WHEN
        val result = StoragePathHelper.guildStorageRepositoryPath(testFeature, guildId)

        // THEN
        assertEquals(
            expected = "/data/guild/1000000000/testfeature/",
            actual = result
        )
    }

    @Test
    fun `Should return correct global data path`() {
        // WHEN
        val result = StoragePathHelper.globalStorageDataPath(testFeature)

        // THEN
        assertEquals(
            expected = "/data/global/testfeature/testfeature.json",
            actual = result
        )
    }

    @Test
    fun `Should return correct guild data path`() {
        // GIVEN
        val guildId = Snowflake(1000000000)

        // WHEN
        val result = StoragePathHelper.guildStorageDataPath(testFeature, guildId)

        // THEN
        assertEquals(
            expected = "/data/guild/1000000000/testfeature/testfeature.json",
            actual = result
        )
    }
}