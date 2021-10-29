package bot.features.quotes.data

import java.time.Instant
import java.util.*

const val quotesFeatureName = "quotes"

const val initialQuoteText = "Hah. None of that cheek, or I will take you across my knee."
const val initialQuoteNumber = -1
const val initialQuoteAuthor = "Y'shtola"
val initialQuoteDate = Date.from(Instant.parse("2019-07-02T10:15:30.00Z"))!!


// COMMANDS

const val quotesCommandName = "quote"
const val quotesCommandDescription = "Display, add or edit quotes"

const val getQuoteCommandName = "number"
const val getQuoteCommandDescription = "Display the quote associated to a certain number"

const val getRandomQuoteCommandName = "random"
const val getRandomQuoteCommandDescription = "Display a random quote"

const val addQuoteCommandName = "add"
const val addQuoteCommandDescription = "Add a quote to this server's list"

const val editQuoteCommandGroupName = "edit"
const val editQuoteCommandGroupDescription = "Edit an existing quote"

const val editQuoteTextCommandName = "text"
const val editQuoteTextCommandDescription = "Edit the text of a quote"

const val editQuoteAuthorCommandName = "author"
const val editQuoteAuthorCommandDescription = "Edit the author of a quote"

const val removeQuoteCommandName = "remove"
const val removeQuoteCommandDescription = "Remove the designated quote"

const val quoteMessageCommandName = "Make a quote"


// ARGUMENTS

const val quoteNumberArgumentName = "number"
const val quoteNumberArgumentDescription = "The number of the requested quote"

const val quoteTextArgumentName = "text"
const val quoteTextArgumentDescription = "The quote to add (no need for quotation marks)"

const val quoteAuthorArgumentName = "author"
const val quoteAuthorArgumentDescription = "The author of the quote to add"


// MESSAGES

const val quoteAddFailureMessage = "Sorry, your quote could not be uploaded..."
const val quoteAddSuccessMessage = "New quote was added successfully!"

const val quoteGetFailureMessage = "Sorry, no quote with that number was found..."

const val quoteRemoveFailureMessage =
    "Sorry, could not remove the requested quote... Make sure a quote with that number exists!"

fun quoteRemoveMessageSuccess(quoteMessage: String) =
    "The following quote was removed successfully: $quoteMessage"

const val quoteEditFailureMessage = "Sorry, your quote could not be edited..."
const val quoteEditTextSuccessMessage = "Quote text edited successfully!"
const val quoteEditAuthorSuccessMessage = "Quote author edited successfully!"
