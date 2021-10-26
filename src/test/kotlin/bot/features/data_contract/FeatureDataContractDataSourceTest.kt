package bot.features.data_contract

import bot.features.Feature
import bot.remote.client.StorageApiClient
import bot.remote.service.JsonAllNotNull
import com.google.gson.annotations.SerializedName
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import kotlinx.coroutines.runBlocking
import org.mockito.Mock
import org.mockito.Mockito
import utils.logging.Log

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
internal class FeatureDataContractDataSourceTest

fun main() = runBlocking {
    testGuildDataCreation()
}

private val dataSource = FeatureDataContractDataSource(StorageApiClient.service())

suspend fun testGlobalDataCheckNoData() {
    val testFeatureNoData = object : Feature() {
        override val name: String = "TestFeature"
        override val featureDataContract: FeatureDataContract
            get() = RequiresData.global(
                dataSource = dataSource,
                createNewData = { "" },
                updateExistingData = { "" },
            )

        override suspend fun Kord.addFeature() {}
    }

    val result = dataSource.checkForExistingGlobalData<String>(testFeatureNoData)
    Log.info(result, "FeatureDataContractDataSourceTest.testGlobalDataCheckExistingData()")
}

@JsonAllNotNull
data class KeyValue(
    @SerializedName("key") val key: String,
)

suspend fun testGlobalDataCheckExistingData() {
    val testFeatureWithData = object : Feature() {
        override val name: String = "TestFeatureWithData"
        override val featureDataContract: FeatureDataContract
            get() = RequiresData.global(
                dataSource = dataSource,
                createNewData = { KeyValue("newData") },
                updateExistingData = { KeyValue("updatedData") },
            )

        override suspend fun Kord.addFeature() {}
    }

    val result = dataSource.checkForExistingGlobalData<KeyValue>(testFeatureWithData)
    Log.info(result, "FeatureDataContractDataSourceTest.testGlobalDataCheckExistingData()")

}

suspend fun testGuildDataCheckNoData() {
    val testGuildFeatureNoData = object : Feature() {
        override val name: String = "testGuildFeatureNoData"
        override val featureDataContract: FeatureDataContract
            get() = RequiresData.global(
                dataSource = dataSource,
                createNewData = { "" },
                updateExistingData = { "" },
            )

        override suspend fun Kord.addFeature() {}
    }

    val guildId = Snowflake(1000000000)
    val result = dataSource.checkForExistingGuildData<KeyValue>(testGuildFeatureNoData, guildId)
    Log.info(result, "FeatureDataContractDataSourceTest.testGuildDataCheckNoData()")

}

suspend fun testGuildDataCheckExistingData() {
    val testGuildFeatureWithData = object : Feature() {
        override val name: String = "testGuildFeatureWithData"
        override val featureDataContract: FeatureDataContract
            get() = RequiresData.guild(
                dataSource = dataSource,
                createNewData = { KeyValue("newData") },
                updateExistingData = { KeyValue("updatedData") },
            )

        override suspend fun Kord.addFeature() {}
    }

    val guildId = Snowflake(1000000000)
    val result = dataSource.checkForExistingGuildData<KeyValue>(testGuildFeatureWithData, guildId)
    Log.info(result, "FeatureDataContractDataSourceTest.testGuildDataCheckExistingData()")

}

val testFeatureGlobalDataCreation = object : Feature() {
    override val name: String = "testFeatureGlobalDataCreation"
    override val featureDataContract: FeatureDataContract
        get() = RequiresData.global(
            dataSource = dataSource,
            createNewData = { KeyValue("newData") },
            updateExistingData = { KeyValue("updatedData") },
        )

    override suspend fun Kord.addFeature() {}
}

val testFeatureGuildDataCreation = object : Feature() {
    override val name: String = "testFeatureGuildDataCreation"
    override val featureDataContract: FeatureDataContract
        get() = RequiresData.guild(
            dataSource = dataSource,
            createNewData = { KeyValue("newData") },
            updateExistingData = { KeyValue("updatedData") },
        )

    override suspend fun Kord.addFeature() {}
}

@Mock
val guild: Guild = Mockito.mock(Guild::class.java)

suspend fun testGlobalDataCreation() {
    val guildId = Snowflake(1000000000)
    Mockito.`when`(guild.id).thenReturn(guildId)
    testFeatureGlobalDataCreation.initializeGlobalData()
    testFeatureGlobalDataCreation.initializeGuildData(guild)
}

suspend fun testGuildDataCreation() {
    val guildId = Snowflake(1000000000)
    Mockito.`when`(guild.id).thenReturn(guildId)
    testFeatureGuildDataCreation.initializeGlobalData()
    testFeatureGuildDataCreation.initializeGuildData(guild)
}

val testFeatureAllData = object : Feature() {
    override val name: String = "testFeatureAllData"
    override val featureDataContract: FeatureDataContract
        get() = RequiresData.globalAndGuild(
            dataSource = dataSource,
            createNewGlobalData = { KeyValue("newGlobalData") },
            updateExistingGlobalData = { KeyValue("updatedGlobalData") },
            createNewGuildData = { KeyValue("newGuildData") },
            updateExistingGuildData = { KeyValue("updatedGlobalData") },
        )

    override suspend fun Kord.addFeature() {}
}

suspend fun testAllDataCreation() {
    val guildId = Snowflake(1000000000)
    Mockito.`when`(guild.id).thenReturn(guildId)
    testFeatureAllData.initializeGlobalData()
    testFeatureAllData.initializeGuildData(guild)
}
