package bot.features.guides.model.results

import bot.features.guides.model.Guide
import bot.features.guides.model.GuideCategory

sealed class EditGuideResult {
    object NoSuchCategory : EditGuideResult()
    object NoSuchGuide : EditGuideResult()
    object GuideAlreadyExists : EditGuideResult()
    object Failure : EditGuideResult()
    data class Success(val guide: Guide, val category: GuideCategory) : EditGuideResult()
}
