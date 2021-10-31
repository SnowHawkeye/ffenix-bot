package bot.features.guides.data

import dev.kord.common.Color

const val guidesFeatureName = "guides"

// ROLES

const val editGuidesNecessaryRoleName = "Strategist"
val editGuidesNecessaryRoleColor = Color(rgb = 0xD9FCF1)


// COMMANDS

// Top-level
const val accessGuidesCommandName = "guides"
const val accessGuidesCommandDescription = "Display guide categories and guides"

const val modifyGuidesCommandName = "editguides"
const val modifyGuidesCommandDescription = "Add, edit or remove guides"

// Add
const val addGuidesCommandName = "add"
const val addGuidesCommandDescription = "Add a guide or a category"

const val addCategoryCommandName = "category"
const val addCategoryCommandDescription = "Add a category"

const val addGuideCommandName = "guide"
const val addGuideCommandDescription = "Add a guide"

// Edit
const val editGuidesCommandName = "edit"
const val editGuidesCommandDescription = "Edit a guide or a category"

const val editCategoryCommandName = "category"
const val editCategoryCommandDescription = "Edit a category"

const val editGuideCommandName = "guide"
const val editGuideCommandDescription = "Edit a guide"

// Remove
const val removeGuidesCommandName = "remove"
const val removeGuidesCommandDescription = "Remove a guide or a category"

const val removeCategoryCommandName = "category"
const val removeCategoryCommandDescription = "Remove a category"

const val removeGuideCommandName = "guide"
const val removeGuideCommandDescription = "Remove a guide"

// Get
const val getGuideCommandName = "for"
const val getGuideCommandDescription = "Display the requested guide(s)"

const val getGuideCategoriesCommandName = "list"
const val getGuideCategoriesCommandDescription = "Display existing guide categories"


// ARGUMENTS

const val categoryArgumentName = "category"
const val categoryArgumentDescription = "The category of the guide"

const val guideArgumentName = "guide"
const val guideArgumentDescription = "The name of the guide"

const val categoryNameArgumentName = "categoryname"
const val categoryNameArgumentDescription = "The name of the category"

const val categoryNewNameArgumentName = "newcategoryname"
const val categoryNewNameArgumentDescription = "The new name of the category"

const val categoryThumbnailArgumentName = "thumbnail"
const val categoryThumbnailArgumentDescription = "URL of the guide category thumbnail"

const val categoryColorArgumentName = "color"
const val categoryColorArgumentDescription = "The color that will be used in the guides' embeds (format: 0xFFFFFF)"

const val guideTitleArgumentName = "title"
const val guideTitleArgumentDescription = "The title of the guide"

const val guideNewTitleArgumentName = "newtitle"
const val guideNewTitleArgumentDescription = "The new title of the guide"

const val guideDescriptionArgumentName = "description"
const val guideDescriptionArgumentDescription = "The description associated to the guide"

const val guideLinkArgumentName = "link"
const val guideLinkArgumentDescription = "URL of a link associated to the guide"

const val guideImageArgumentName = "image"
const val guideImageArgumentDescription = "URL of an image to display with the guide"


// MESSAGES

fun guideDisplayMessage(guideTitle: String) = "Here is the guide for **$guideTitle**:"
fun categorySuccessfullyAddedMessage(categoryName: String) = "The **$categoryName** category was successfully added."
fun categorySuccessfullyEditedMessage(categoryName: String) =
    "The **$categoryName** category was successfully edited."

fun categorySuccessfullyRemovedMessage(categoryName: String) =
    "The **$categoryName** category was successfully removed."

fun guideSuccessfullyRemovedMessage(guideTitle: String, categoryName: String) =
    "The guide **$guideTitle** was successfully removed in category **$categoryName**."


const val guideCategoriesDisplayMessage = "Here are all the guide categories for this server:\n"
fun guideSelectMenuMessage(categoryName: String) =
    "Here are the guides we found for the **$categoryName** category. Pick one in the drop down menu to display it!"

const val guideSelectMenuCustomId = "guideSelectMenuCustomId"

// Errors
const val genericFailureMessage = "Sorry, an error has occurred..."
const val noCategoriesFoundErrorMessage = "Sorry, there are no guides to display..."
const val noSuchCategoryErrorMessage = "Sorry, no such category was found..."
const val noSuchGuideErrorMessage = "Sorry, no such guide was found for the requested category..."
const val categoryAlreadyExistsErrorMessage =
    "A category with that name already exists! Please use another name."
const val guideAlreadyExistsErrorMessage =
    "A guide with that name already exists in that category! Please use another name."
const val categoryNotEmptyErrorMessage =
    "This category is not empty. Please remove all guides before removing the category."
const val categoryEmptyErrorMessage = "There are no guides in this category."
