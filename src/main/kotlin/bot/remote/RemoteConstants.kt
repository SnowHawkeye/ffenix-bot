package bot.remote

// GENERAL
internal const val DROPBOX_API_TOKEN_KEY = "DROPBOX_FFENIX_BOT_API_TOKEN"
internal const val EMPTY_BASE_URL = "http://localhost/"
internal const val DROPBOX_API_BASE_URL = "https://api.dropboxapi.com/2/"
internal const val DROPBOX_CONTENT_BASE_URL = "https://content.dropboxapi.com/2/"

// SUFFIXES
internal const val FILES_SUFFIX = "files/"
internal const val CREATE_FOLDER_SUFFIX = "create_folder_v2"
internal const val DOWNLOAD_SUFFIX = "download"
internal const val UPLOAD_SUFFIX = "upload"

// PATHS
internal const val CREATE_FOLDER_PATH = DROPBOX_API_BASE_URL + FILES_SUFFIX + CREATE_FOLDER_SUFFIX
internal const val DOWNLOAD_FILE_PATH = DROPBOX_CONTENT_BASE_URL + FILES_SUFFIX + DOWNLOAD_SUFFIX
internal const val UPLOAD_FILE_PATH = DROPBOX_CONTENT_BASE_URL + FILES_SUFFIX + UPLOAD_SUFFIX

// HEADERS
internal const val AUTHORIZATION_HEADER_KEY = "Authorization"
internal const val ARGUMENT_HEADER_KEY = "Dropbox-API-Arg"
internal const val JSON_CONTENT_TYPE_HEADER = "Content-type: application/json"
internal const val TEXT_CONTENT_TYPE_HEADER = "Content-type: text/plain; charset=dropbox-cors-hack"

// HTTP CODES
internal const val CONFLICT_CODE = 409
