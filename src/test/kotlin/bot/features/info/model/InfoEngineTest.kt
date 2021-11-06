package bot.features.info.model

import bot.features.info.data.InfoRepository
import bot.features.info.model.results.AddInfoCommandResult
import bot.features.info.model.results.EditInfoCommandResult
import bot.features.info.model.results.RemoveInfoCommandResult
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class InfoEngineTest {

    @Mock
    val mockRepository: InfoRepository = Mockito.mock(InfoRepository::class.java)

    @Test
    fun `Should return info commands`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommand1 = InfoCommand(commandName = "name1", commandText = "text1")
        val infoCommand2 = InfoCommand(commandName = "name2", commandText = "text2")
        val infoCommand3 = InfoCommand(commandName = "name3", commandText = "text3")
        val infoCommands = listOf(infoCommand1, infoCommand2, infoCommand3)

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)

        // WHEN
        val result = engine.getInfoCommands(guildId)

        // THEN
        assertEquals(infoCommands, result)
    }

    @Test
    fun `Should return success when adding info command`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommands = listOf<InfoCommand>()

        val expectedInfoCommand = InfoCommand(commandName = "commandName", commandText = "commandText")
        val expectedInfoCommands = listOf(expectedInfoCommand)
        val expected = AddInfoCommandResult.Success(expectedInfoCommand)

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)
        Mockito.`when`(mockRepository.updateInfoCommands(expectedInfoCommands, guildId))
            .thenReturn(InfoRepository.UploadInfoCommandsResult.Success)

        // WHEN
        val result = engine.addInfoCommand(
            commandName = "commandName",
            commandText = "commandText",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while adding info command`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommands = listOf<InfoCommand>()

        val expectedInfoCommand = InfoCommand(commandName = "commandName", commandText = "commandText")
        val expectedInfoCommands = listOf(expectedInfoCommand)
        val expected = AddInfoCommandResult.Failure

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)
        Mockito.`when`(mockRepository.updateInfoCommands(expectedInfoCommands, guildId))
            .thenReturn(InfoRepository.UploadInfoCommandsResult.Failure)

        // WHEN
        val result = engine.addInfoCommand(
            commandName = "commandName",
            commandText = "commandText",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding an info command that already exists`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommands = listOf(InfoCommand(commandName = "commandName", commandText = "commandText"))

        val expected = AddInfoCommandResult.CommandAlreadyExists

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)

        // WHEN
        val result = engine.addInfoCommand(
            commandName = "commandName",
            commandText = "commandText",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when editing an info command`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val oldInfoCommand = InfoCommand(commandName = "commandName", commandText = "commandText")
        val infoCommands = listOf(oldInfoCommand)
        val updatedInfoCommand = InfoCommand(commandName = "commandName", commandText = "newCommandText")
        val updatedInfoCommands = listOf(updatedInfoCommand)

        val expected = EditInfoCommandResult.Success(oldInfoCommand, updatedInfoCommand)

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)
        Mockito.`when`(mockRepository.updateInfoCommands(updatedInfoCommands, guildId))
            .thenReturn(InfoRepository.UploadInfoCommandsResult.Success)

        // WHEN
        val result = engine.editInfoCommand(
            commandName = "commandName",
            newCommandText = "newCommandText",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while updating an info command`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommands = listOf(InfoCommand(commandName = "commandName", commandText = "commandText"))
        val updatedInfoCommand = InfoCommand(commandName = "commandName", commandText = "newCommandText")
        val updatedInfoCommands = listOf(updatedInfoCommand)

        val expected = EditInfoCommandResult.Failure

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)
        Mockito.`when`(mockRepository.updateInfoCommands(updatedInfoCommands, guildId))
            .thenReturn(InfoRepository.UploadInfoCommandsResult.Failure)

        // WHEN
        val result = engine.editInfoCommand(
            commandName = "commandName",
            newCommandText = "newCommandText",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to update an info command that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommands = listOf(InfoCommand(commandName = "otherCommandName", commandText = "commandText"))

        val expected = EditInfoCommandResult.CommandDoesNotExist
        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)

        // WHEN
        val result = engine.editInfoCommand(
            commandName = "commandName",
            newCommandText = "newCommandText",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when removing an info command`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommand = InfoCommand(commandName = "commandName", commandText = "commandText")
        val infoCommands = listOf(infoCommand)
        val updatedInfoCommands = listOf<InfoCommand>()

        val expected = RemoveInfoCommandResult.Success(infoCommand)

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)
        Mockito.`when`(mockRepository.updateInfoCommands(updatedInfoCommands, guildId))
            .thenReturn(InfoRepository.UploadInfoCommandsResult.Success)

        // WHEN
        val result = engine.removeInfoCommand(
            commandName = "commandName",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while removing an info command`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommand = InfoCommand(commandName = "commandName", commandText = "commandText")
        val infoCommands = listOf(infoCommand)
        val updatedInfoCommands = listOf<InfoCommand>()

        val expected = RemoveInfoCommandResult.Failure

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)
        Mockito.`when`(mockRepository.updateInfoCommands(updatedInfoCommands, guildId))
            .thenReturn(InfoRepository.UploadInfoCommandsResult.Failure)

        // WHEN
        val result = engine.removeInfoCommand(
            commandName = "commandName",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to remove an info command that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = InfoEngine(mockRepository)
        val guildId = Snowflake(1)

        val infoCommands = listOf<InfoCommand>()
        val expected = RemoveInfoCommandResult.CommandDoesNotExist

        Mockito.`when`(mockRepository.getInfoCommands(guildId)).thenReturn(infoCommands)

        // WHEN
        val result = engine.removeInfoCommand(
            commandName = "commandName",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }
}