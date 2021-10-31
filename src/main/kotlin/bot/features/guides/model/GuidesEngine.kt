package bot.features.guides.model

import bot.features.core.typealiases.GuildId
import bot.features.guides.data.GuidesDataStructure
import bot.features.guides.data.GuidesRepository
import bot.features.guides.model.results.*
import dev.kord.common.Color
import kotlinx.datetime.Instant

class GuidesEngine(
    private val repository: GuidesRepository,
) {

    fun makeInitialDataStructure(): GuidesDataStructure {
        return GuidesDataStructure(listOf())
    }

    suspend fun getGuideCategories(forGuildId: GuildId): GuideCategoriesResult {
        val categories = repository.getGuideCategories(forGuildId)
        return if (categories.isNotEmpty()) {
            GuideCategoriesResult.Success(categories)
        } else GuideCategoriesResult.Empty
    }

    suspend fun getGuideCategory(categoryName: String, forGuildId: GuildId): GuideCategoryResult {
        val categories = repository.getGuideCategories(forGuildId)
        val category = categories.find { it.name == categoryName } ?: return GuideCategoryResult.NoSuchCategory
        return GuideCategoryResult.Success(category)
    }

    suspend fun getGuide(categoryName: String, guideTitle: String, forGuildId: GuildId): GuideResult {
        val categories = repository.getGuideCategories(forGuildId)
        val category = categories.find { it.name == categoryName } ?: return GuideResult.NoSuchCategory
        val guide = category.guides.find { it.title == guideTitle } ?: return GuideResult.NoSuchGuide
        return GuideResult.Success(guide, category)
    }

    suspend fun addCategory(
        categoryName: String,
        forGuildId: GuildId,
        thumbnailUrl: String? = null,
        colorHex: Int? = null,
    ): AddGuideCategoryResult {
        val categories = repository.getGuideCategories(forGuildId)
        if (categories.any { it.name == categoryName }) return AddGuideCategoryResult.CategoryAlreadyExists

        val color = if (colorHex != null) Color(colorHex) else null
        val newCategory = GuideCategory(
            name = categoryName,
            thumbnailUrl = thumbnailUrl,
            color = color,
            guides = listOf()
        )

        val updatedCategories = categories.toMutableList().apply { add(newCategory) }
        return when (repository.updateGuideCategories(updatedCategories, forGuildId)) {
            GuidesRepository.UploadGuidesResult.Failure -> AddGuideCategoryResult.Failure
            GuidesRepository.UploadGuidesResult.Success -> AddGuideCategoryResult.Success(newCategory)
        }
    }

    suspend fun addGuide(
        categoryName: String,
        title: String,
        author: String,
        timestamp: Instant,
        forGuildId: GuildId,
        description: String? = null,
        link: String? = null,
        imageUrl: String? = null,
    ): AddGuideResult {
        val categories = repository.getGuideCategories(forGuildId)
        val category = categories.find { it.name == categoryName } ?: return AddGuideResult.NoSuchCategory
        if (category.guides.any { it.title == title }) return AddGuideResult.GuideAlreadyExists

        val newGuide = Guide(
            title = title,
            description = description,
            link = link,
            imageUrl = imageUrl,
            author = author,
            timestamp = timestamp
        )

        val updatedGuides = category.guides.toMutableList().apply { add(newGuide) }
        val updatedCategory = category.copy(guides = updatedGuides)
        val updatedCategories = categories.toMutableList().apply { remove(category); add(updatedCategory) }

        return when (repository.updateGuideCategories(updatedCategories, forGuildId)) {
            GuidesRepository.UploadGuidesResult.Failure -> AddGuideResult.Failure
            GuidesRepository.UploadGuidesResult.Success -> AddGuideResult.Success(newGuide, updatedCategory)
        }
    }

    suspend fun editCategory(
        categoryName: String,
        forGuildId: GuildId,
        newCategoryName: String? = null,
        thumbnailUrl: String? = null,
        colorHex: Int? = null,
    ): EditGuideCategoryResult {
        val categories = repository.getGuideCategories(forGuildId)
        val category = categories.find { it.name == categoryName } ?: return EditGuideCategoryResult.NoSuchCategory
        if (categories.any { it.name == newCategoryName }) return EditGuideCategoryResult.CategoryAlreadyExists


        val color = if (colorHex != null) Color(colorHex) else null
        val newCategory = category.copy(
            name = newCategoryName ?: categoryName,
            thumbnailUrl = thumbnailUrl ?: category.thumbnailUrl,
            color = color ?: category.color,
        )

        val updatedCategories = categories.toMutableList().apply { remove(category); add(newCategory) }
        return when (repository.updateGuideCategories(updatedCategories, forGuildId)) {
            GuidesRepository.UploadGuidesResult.Failure -> EditGuideCategoryResult.Failure
            GuidesRepository.UploadGuidesResult.Success -> EditGuideCategoryResult.Success(newCategory)
        }
    }

    suspend fun editGuide(
        categoryName: String,
        title: String,
        forGuildId: GuildId,
        newTitle: String? = null,
        description: String? = null,
        link: String? = null,
        imageUrl: String? = null,
    ): EditGuideResult {
        val categories = repository.getGuideCategories(forGuildId)
        val category = categories.find { it.name == categoryName } ?: return EditGuideResult.NoSuchCategory
        val guide = category.guides.find { it.title == title } ?: return EditGuideResult.NoSuchGuide
        if (category.guides.any { it.title == newTitle }) return EditGuideResult.GuideAlreadyExists

        val updatedGuide = guide.copy(
            title = newTitle ?: guide.title,
            description = description ?: guide.description,
            link = link ?: guide.link,
            imageUrl = imageUrl ?: guide.imageUrl,
        )

        val updatedGuides = category.guides.toMutableList().apply { remove(guide); add(updatedGuide) }
        val updatedCategory = category.copy(guides = updatedGuides)
        val updatedCategories = categories.toMutableList().apply { remove(category); add(updatedCategory) }

        return when (repository.updateGuideCategories(updatedCategories, forGuildId)) {
            GuidesRepository.UploadGuidesResult.Failure -> EditGuideResult.Failure
            GuidesRepository.UploadGuidesResult.Success -> EditGuideResult.Success(updatedGuide, updatedCategory)
        }
    }

    suspend fun removeCategory(
        categoryName: String,
        forGuildId: GuildId,
    ): RemoveGuideCategoryResult {
        val categories = repository.getGuideCategories(forGuildId)
        val category = categories.find { it.name == categoryName } ?: return RemoveGuideCategoryResult.NoSuchCategory
        if (category.guides.isNotEmpty()) return RemoveGuideCategoryResult.CategoryNotEmpty
        val updatedCategories = categories.toMutableList().apply { remove(category) }

        return when (repository.updateGuideCategories(updatedCategories, forGuildId)) {
            GuidesRepository.UploadGuidesResult.Failure -> RemoveGuideCategoryResult.Failure
            GuidesRepository.UploadGuidesResult.Success -> RemoveGuideCategoryResult.Success(category)
        }

    }

    suspend fun removeGuide(
        categoryName: String,
        title: String,
        forGuildId: GuildId,
    ): RemoveGuideResult {
        val categories = repository.getGuideCategories(forGuildId)
        val category = categories.find { it.name == categoryName } ?: return RemoveGuideResult.NoSuchCategory
        val guide = category.guides.find { it.title == title } ?: return RemoveGuideResult.NoSuchGuide
        val updatedCategory = category.copy(guides = category.guides.toMutableList().apply { remove(guide) })
        val updatedCategories = categories.toMutableList().apply { remove(category); add(updatedCategory) }

        return when (repository.updateGuideCategories(updatedCategories, forGuildId)) {
            GuidesRepository.UploadGuidesResult.Failure -> RemoveGuideResult.Failure
            GuidesRepository.UploadGuidesResult.Success -> RemoveGuideResult.Success(guide)
        }
    }

    companion object {
        fun instance() = GuidesEngine(GuidesRepository())
    }
}