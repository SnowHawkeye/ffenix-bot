# Info commands

`/info`

Manage info commands that can be triggered by writing `![commandname]` in a channel.  
Requires the **Herald** role.

***

## Using info commands

`![commandname]`

Brings up the text for the given info command.

***

## Managing info commands

### Adding info commands

`/info add [commandname] [commandtext]`

Add an info command with the given text.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `commandname` | String | The name of the info command to add. | Yes |
| `commandtext` | String | The text that will be displayed in response to the command. | Yes |

### Editing info commands

`/info edit [commandname] [nextext]`

Edit an info command by replacing its text.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `commandname` | String | The name of the info command to edit. | Yes |
| `newtext` | String | The new text to display in response to the command. | Yes |

### Remove info commands

`/info remove [commandname]`

Remove an existing info command.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `commandname` | String | The name of the info command to remove. | Yes |