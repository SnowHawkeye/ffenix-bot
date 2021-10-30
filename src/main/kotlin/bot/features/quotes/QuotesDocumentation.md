# Quotes

`/quote`

Display or add quotes.

`/editquote`

Edit or remove quotes. Requires the **Archivist** role.

"*Hah. None of that cheek, or I will take you across my knee.*" - Y'shtola **(Jun 02, 2019)**

***

## Displaying quotes

### Get a certain quote

`/quote number [number]`

Display the quote associated to a certain number.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `number` | Integer | The number of the requested quote. | Yes |

### Get a random quote

`/quote random`

Display a random quote.

***

## Adding a new quote

`/quote add [text] [author]`

Add a quote to the bot's known list of quotes for this server.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `text` | String | The quote to add. No need for quotation marks. | Yes |
| `author` | String | The author of the quote. | Yes |

### Adding with a message command

The `Make quote` message command adds the designated message as a quote.

***

## Editing an existing quote

`/editquote edit text [number] [newText]`

Edit the text of a quote. Requires the **Archivist** role.

`/editquote edit author [number] [newAuthor]`

Edit the author of a quote. Requires the **Archivist** role.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `number` | Integer | The number of the requested quote. | Yes |
| `newText` | String | The new quote text. | Yes |
| `newAuthor` | String | The new quote author. | Yes |

***

## Removing an existing quote

`/editquote remove [number]`

Remove the designated quote. Requires the **Archivist** role.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `number` | Integer | The number of the requested quote. | Yes |





