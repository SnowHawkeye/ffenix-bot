# Polls

`/poll`

Create a simple emoji-based poll. The poll is displayed as an embed that looks like this:

![](https://cdn.discordapp.com/attachments/904320277842915348/904704677122310184/unknown.png)

The emojis are picked at random. The bot will then react to its own message with those emojis.  
If a maximum number of answers is set, the bot will remove any extra answers.

IMPORTANT NOTES : 
- This feature requires the bot to have the MANAGE_MESSAGES permission.
If it does not, reaction numbers cannot be enforced.
- If the bot is rebooted, reaction numbers will not be enforced for all polls that were created prior to the reboot.


***

## Make a poll

`/poll [question] [options] [maxanswers] [thumbnail]`

Creates a poll with the given arguments.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `question` | String | The subject of this poll. | Yes |
| `options` | String | The options for the poll (max 30). Options must be separated by a slash `/`. | Yes |
| `maxanswers` | Int | The maximum number of answers a user can give. By default, there is no limit. Must be strictly greater than 0. | No |
| `thumbnail` | String | The URL of a thumbnail for this poll. | No |
