# Guides

`/guides`

Display guide categories and guides. A guide category can have up to 25 guides.

`/editguides`

Add, edit or remove guides. Requires the **Strategist** role.

Guides should be displayed like this:
![](https://cdn.discordapp.com/attachments/904320277842915348/904320295706460180/unknown.png).

***

## Displaying guides

### Get a certain guide

`/guides for [category] [guide]`

Display the requested guide. If no guide is specified, a drop-down menu with the guides in the requested category is
displayed.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `category` | String | The category of the requested guide. | Yes |
| `guide` | String | The name of the requested guide. | No |

### List existing guide categories

`/guides list`

Display all existing guide categories.

***

## Adding guides

Adding a guide requires the **Strategist** role.

### Adding a category

`/editguides add category [name] [thumbnailUrl] [color]`

Add a guide category. Two categories cannot have the same name.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `name` | String | The name of the category to add. | Yes |
| `thumbnail` | String | URL of the thumbnail that will be displayed for all guides of this category. | No |
| `color` | String | The color that will be used in the guides' embeds. | No |

### Adding a guide

`/editguides add guide [category] [title] [description] [link] [image]`

Add a guide to the given category. Two guides cannot have the same name within a category.  
To have a multiline description, you can use the `\n` character.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `category` | String | The name of the category in which the guide will be added. | Yes |
| `title` | String | The title of the guide to add. | Yes |
| `description` | String | The description associated to the guide. | No |
| `link` | String | URL of a link associated to the guide. | No |
| `image` | String | URL of an image to display with the guide. | No |

***

## Editing guides

Editing a guide requires the **Strategist** role.

### Editing a category

`/editguides edit category [name] [newname] [thumbnailUrl] [color]`

Edit a guide category.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `name` | String | The name of the category to edit. | Yes |
| `newname` | String | The new name to give to the category. | No |
| `thumbnail` | String | URL of the thumbnail that will be displayed for all guides of this category. | No |
| `color` | String | The color that will be used in the guides' embeds. | No |

### Editing a guide

`/editguides edit guide [category] [title] [newtitle] [description] [link] [image]`

Edit a guide.  
To have a multiline description, you can use the `\n` character.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `category` | String | The name of the category in which the guide belongs. | Yes |
| `title` | String | The title of the guide to edit. | Yes |
| `newtitle` | String | The new title of the guide. | No |
| `description` | String | The description associated to the guide. | No |
| `link` | String | URL of a link associated to the guide. | No |
| `image` | String | URL of an image to display with the guide. | No |

***

## Removing guides

Removing a guide requires the **Strategist** role.

### Removing a category

`/editguides remove category [category]`

Remove a category. The category must be empty.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `category` | String | The category to remove. | Yes |

### Removing a guide

`/editguides remove guide [category] [title]`

Remove a guide in the given category.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `category` | String | The category to remove. | Yes |
| `title` | String | The title of the guide to remove. | Yes |
