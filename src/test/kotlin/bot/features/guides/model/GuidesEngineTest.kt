package bot.features.guides.model

import bot.features.guides.data.GuidesRepository
import bot.features.guides.model.results.*
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class GuidesEngineTest {

    @Mock
    private val mockRepository: GuidesRepository = Mockito.mock(GuidesRepository::class.java)

    @Test
    fun `Should return success with guide categories`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val guide = Guide(title = "guide", author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = "category", guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = GuideCategoriesResult.Success(guideCategories)

        // WHEN
        val result = engine.getGuideCategories(guildId)

        // THEN
        assertEquals(expected, result)

    }

    @Test
    fun `Should return empty when there are no guide categories`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)

        val guideCategories = listOf<GuideCategory>()

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = GuideCategoriesResult.Empty

        // WHEN
        val result = engine.getGuideCategories(guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success with guide category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)

        val guideCategoryName = "category"
        val guideCategory = GuideCategory(name = guideCategoryName, guides = listOf())
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = GuideCategoryResult.Success(guideCategory)

        // WHEN
        val result = engine.getGuideCategory(guideCategoryName, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return that the category does not exist`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)

        val guildId = Snowflake(1)
        val guideCategoryName = "category"
        val guideCategories = listOf<GuideCategory>()

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = GuideCategoryResult.NoSuchCategory

        // WHEN
        val result = engine.getGuideCategory(guideCategoryName, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success with guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"

        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = GuideResult.Success(guide, guideCategory)

        // WHEN
        val result = engine.getGuide(categoryName, guideTitle, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure because category was not found when fetching guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)

        val categoryName = "category"
        val guideTitle = "guide"

        val guideCategories = listOf<GuideCategory>()
        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = GuideResult.NoSuchCategory

        // WHEN
        val result = engine.getGuide(categoryName, guideTitle, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure because guide was not found when fetching guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)

        val categoryName = "category"
        val guideTitle = "guide"

        val guideCategory = GuideCategory(name = categoryName, guides = listOf())
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = GuideResult.NoSuchGuide

        // WHEN
        val result = engine.getGuide(categoryName, guideTitle, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success with category when adding category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newCategoryName = "new"
        val newCategoryThumbnail = "url"
        val newCategoryColor = 0xFFFFFF

        val expectedNewCategory = GuideCategory(
            name = newCategoryName,
            thumbnailUrl = newCategoryThumbnail,
            color = Color(newCategoryColor),
            guides = listOf()
        )

        val expectedCategories = listOf(guideCategory, expectedNewCategory)


        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Success)

        val expected = AddGuideCategoryResult.Success(expectedNewCategory)

        // WHEN
        val result = engine.addCategory(
            categoryName = newCategoryName,
            forGuildId = guildId,
            thumbnailUrl = newCategoryThumbnail,
            colorHex = newCategoryColor
        )

        // THEN
        assertEquals(expected, result)
    }


    @Test
    fun `Should return category exists when trying to add category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newCategoryName = "category"
        val newCategoryThumbnail = "url"

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = AddGuideCategoryResult.CategoryAlreadyExists

        // WHEN
        val result = engine.addCategory(
            categoryName = newCategoryName,
            forGuildId = guildId,
            thumbnailUrl = newCategoryThumbnail,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure exists when trying to add category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)

        val newCategoryName = "category"
        val newCategoryThumbnail = "url"

        val expectedNewCategory = GuideCategory(
            name = newCategoryName,
            thumbnailUrl = newCategoryThumbnail,
            guides = listOf()
        )

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(listOf())
        Mockito.`when`(mockRepository.updateGuideCategories(listOf(expectedNewCategory), guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Failure)

        val expected = AddGuideCategoryResult.Failure

        // WHEN
        val result = engine.addCategory(
            categoryName = newCategoryName,
            forGuildId = guildId,
            thumbnailUrl = newCategoryThumbnail,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success with guide when adding guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newGuideTitle = "new"
        val newGuideAuthor = "author"
        val newGuideDescription = "description"
        val newGuideLink = "link"
        val newGuideImage = "image"

        val expectedNewGuide = Guide(
            title = newGuideTitle,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            author = newGuideAuthor,
            timestamp = instant
        )

        val expectedCategory = guideCategory.copy(guides = listOf(guide, expectedNewGuide))
        val expectedCategories = listOf(expectedCategory)


        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Success)

        val expected = AddGuideResult.Success(expectedNewGuide, expectedCategory)

        // WHEN
        val result = engine.addGuide(
            categoryName = categoryName,
            title = newGuideTitle,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            author = newGuideAuthor,
            timestamp = instant,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding guide whose name already exists`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newGuideAuthor = "author"

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = AddGuideResult.GuideAlreadyExists

        // WHEN
        val result = engine.addGuide(
            categoryName = categoryName,
            title = guideTitle,
            author = newGuideAuthor,
            timestamp = instant,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return no such category when adding guide to non-existing category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guideCategories = listOf<GuideCategory>()

        val newGuideAuthor = "author"

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = AddGuideResult.NoSuchCategory

        // WHEN
        val result = engine.addGuide(
            categoryName = categoryName,
            title = guideTitle,
            author = newGuideAuthor,
            timestamp = instant,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when a problem occurs during guide upload`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newGuideTitle = "newTitle"
        val newGuideAuthor = "author"

        val expectedNewGuide = Guide(title = newGuideTitle, author = newGuideAuthor, timestamp = instant)
        val expectedCategory = guideCategory.copy(guides = listOf(guide, expectedNewGuide))
        val expectedCategories = listOf(expectedCategory)


        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Failure)

        val expected = AddGuideResult.Failure

        // WHEN
        val result = engine.addGuide(
            categoryName = categoryName,
            title = newGuideTitle,
            author = newGuideAuthor,
            timestamp = instant,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success with category when editing category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newCategoryName = "new"
        val newCategoryThumbnail = "url"
        val newCategoryColor = 0xFFFFFF

        val expectedNewCategory = GuideCategory(
            name = newCategoryName,
            thumbnailUrl = newCategoryThumbnail,
            color = Color(newCategoryColor),
            guides = listOf(guide)
        )

        val expectedCategories = listOf(expectedNewCategory)


        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Success)

        val expected = EditGuideCategoryResult.Success(expectedNewCategory)

        // WHEN
        val result = engine.editCategory(
            categoryName = categoryName,
            forGuildId = guildId,
            newCategoryName = newCategoryName,
            thumbnailUrl = newCategoryThumbnail,
            colorHex = newCategoryColor
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return no such category when editing category that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newCategoryName = "new"
        val newCategoryThumbnail = "url"
        val newCategoryColor = 0xFFFFFF

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = EditGuideCategoryResult.NoSuchCategory

        // WHEN
        val result = engine.editCategory(
            categoryName = newCategoryName,
            forGuildId = guildId,
            newCategoryName = newCategoryName,
            thumbnailUrl = newCategoryThumbnail,
            colorHex = newCategoryColor
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when problem occurred while uploading edited category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newCategoryName = "new"
        val newCategoryThumbnail = "url"
        val newCategoryColor = 0xFFFFFF

        val expectedNewCategory = GuideCategory(
            name = newCategoryName,
            thumbnailUrl = newCategoryThumbnail,
            color = Color(newCategoryColor),
            guides = listOf(guide)
        )

        val expectedCategories = listOf(expectedNewCategory)


        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Failure)

        val expected = EditGuideCategoryResult.Failure

        // WHEN
        val result = engine.editCategory(
            categoryName = categoryName,
            forGuildId = guildId,
            newCategoryName = newCategoryName,
            thumbnailUrl = newCategoryThumbnail,
            colorHex = newCategoryColor
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when edited category has a new name that already exists`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName1 = "category1"
        val categoryName2 = "category2"
        val guideTitle = "guide"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory1 = GuideCategory(name = categoryName1, guides = listOf(guide))
        val guideCategory2 = GuideCategory(name = categoryName2, guides = listOf())
        val guideCategories = listOf(guideCategory1, guideCategory2)

        val newCategoryThumbnail = "url"
        val newCategoryColor = 0xFFFFFF

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = EditGuideCategoryResult.CategoryAlreadyExists

        // WHEN
        val result = engine.editCategory(
            categoryName = categoryName1,
            forGuildId = guildId,
            newCategoryName = categoryName2,
            thumbnailUrl = newCategoryThumbnail,
            colorHex = newCategoryColor
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success with guide when editing guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guideAuthor = "A"
        val guide = Guide(title = guideTitle, author = guideAuthor, timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newGuideDescription = "description"
        val newGuideLink = "link"
        val newGuideImage = "image"

        val expectedNewGuide = Guide(
            title = guideTitle,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            author = guideAuthor,
            timestamp = instant
        )

        val expectedCategory = guideCategory.copy(guides = listOf(expectedNewGuide))
        val expectedCategories = listOf(expectedCategory)


        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Success)

        val expected = EditGuideResult.Success(expectedNewGuide, expectedCategory)

        // WHEN
        val result = engine.editGuide(
            categoryName = categoryName,
            title = guideTitle,
            newTitle = null,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return no such category when editing guide for category that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guideAuthor = "A"
        val guide = Guide(title = guideTitle, author = guideAuthor, timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newGuideDescription = "description"
        val newGuideLink = "link"
        val newGuideImage = "image"

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = EditGuideResult.NoSuchCategory

        // WHEN
        val result = engine.editGuide(
            categoryName = "Bad category name",
            title = guideTitle,
            newTitle = null,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return no such guide when editing guide that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guideAuthor = "A"
        val guide = Guide(title = guideTitle, author = guideAuthor, timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newGuideDescription = "description"
        val newGuideLink = "link"
        val newGuideImage = "image"

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = EditGuideResult.NoSuchGuide

        // WHEN
        val result = engine.editGuide(
            categoryName = categoryName,
            title = "bad title",
            newTitle = null,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when an error occurs while editing guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guideTitle = "guide"
        val guideAuthor = "A"
        val guide = Guide(title = guideTitle, author = guideAuthor, timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val newGuideTitle = "new"
        val newGuideDescription = "description"
        val newGuideLink = "link"
        val newGuideImage = "image"

        val expectedNewGuide = Guide(
            title = newGuideTitle,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            author = guideAuthor,
            timestamp = instant
        )

        val expectedCategory = guideCategory.copy(guides = listOf(expectedNewGuide))
        val expectedCategories = listOf(expectedCategory)


        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Failure)

        val expected = EditGuideResult.Failure

        // WHEN
        val result = engine.editGuide(
            categoryName = categoryName,
            title = guideTitle,
            newTitle = newGuideTitle,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when editing guide with a name that already exists`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"

        val guideTitle1 = "guide"
        val guideAuthor1 = "A"
        val guide1 = Guide(title = guideTitle1, author = guideAuthor1, timestamp = instant)

        val guideTitle2 = "guide"
        val guideAuthor2 = "A"
        val guide2 = Guide(title = guideTitle2, author = guideAuthor2, timestamp = instant)

        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide1, guide2))
        val guideCategories = listOf(guideCategory)

        val newGuideDescription = "description"
        val newGuideLink = "link"
        val newGuideImage = "image"

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)

        val expected = EditGuideResult.GuideAlreadyExists

        // WHEN
        val result = engine.editGuide(
            categoryName = categoryName,
            title = guideTitle1,
            newTitle = guideTitle2,
            description = newGuideDescription,
            link = newGuideLink,
            imageUrl = newGuideImage,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when removing empty category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)

        val categoryName = "category"
        val guideCategory = GuideCategory(name = categoryName, guides = listOf())
        val guideCategories = listOf(guideCategory)

        val expectedCategories = listOf<GuideCategory>()

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Success)
        val expected = RemoveGuideCategoryResult.Success(guideCategory)

        // WHEN
        val result = engine.removeCategory(categoryName, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when removing non-empty category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guide = Guide(title = "guide", author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = RemoveGuideCategoryResult.CategoryNotEmpty

        // WHEN
        val result = engine.removeCategory(categoryName, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when removing category that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val categoryName = "category"
        val guide = Guide(title = "guide", author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = RemoveGuideCategoryResult.NoSuchCategory

        // WHEN
        val result = engine.removeCategory("bad name", guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when an error occurs while removing category`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)

        val categoryName = "category"
        val guideCategory = GuideCategory(name = categoryName, guides = listOf())
        val guideCategories = listOf(guideCategory)

        val expectedCategories = listOf<GuideCategory>()

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Failure)
        val expected = RemoveGuideCategoryResult.Failure

        // WHEN
        val result = engine.removeCategory(categoryName, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when removing guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val guideTitle = "guide"
        val categoryName = "category"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val expectedCategories = listOf(guideCategory.copy(guides = listOf()))

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Success)

        val expected = RemoveGuideResult.Success(guide)

        // WHEN
        val result = engine.removeGuide(categoryName, guideTitle, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when removing guide that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val guideTitle = "guide"
        val categoryName = "category"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = RemoveGuideResult.NoSuchGuide

        // WHEN
        val result = engine.removeGuide(categoryName, "bad title", guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when removing guide in category that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val guideTitle = "guide"
        val categoryName = "category"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        val expected = RemoveGuideResult.NoSuchCategory

        // WHEN
        val result = engine.removeGuide("bad name", guideTitle, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when an error occurred while removing guide`() = runBlockingTest {
        // GIVEN
        val engine = GuidesEngine(mockRepository)
        val guildId = Snowflake(1)
        val instant = Instant.fromEpochMilliseconds(924904800000)

        val guideTitle = "guide"
        val categoryName = "category"
        val guide = Guide(title = guideTitle, author = "A", timestamp = instant)
        val guideCategory = GuideCategory(name = categoryName, guides = listOf(guide))
        val guideCategories = listOf(guideCategory)

        val expectedCategories = listOf(guideCategory.copy(guides = listOf()))

        Mockito.`when`(mockRepository.getGuideCategories(guildId)).thenReturn(guideCategories)
        Mockito.`when`(mockRepository.updateGuideCategories(expectedCategories, guildId))
            .thenReturn(GuidesRepository.UploadGuidesResult.Failure)

        val expected = RemoveGuideResult.Failure

        // WHEN
        val result = engine.removeGuide(categoryName, guideTitle, guildId)

        // THEN
        assertEquals(expected, result)
    }

}