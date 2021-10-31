package bot.features.guides.model.results

import bot.features.guides.model.Guide

sealed class RemoveGuideResult {
    object NoSuchCategory : RemoveGuideResult()
    object NoSuchGuide : RemoveGuideResult()
    object Failure : RemoveGuideResult()
    data class Success(val category: Guide) : RemoveGuideResult()
}
