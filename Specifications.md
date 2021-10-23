# DISCLAIMER

This file is written for a particular DSL that will **not be implemented**.
Better ways to implement commands were found after the writing of this file, which will be used in the bot.
However, the commands listed here reflect the functional intents, and they should all be implemented.
Going forward, this document will be used as a reference, before ultimately being replaced by the documentation
for the actual implementation of the bot commands.

***

# Specifications

## General rules

All commands are prefixed with the __command prefix__ `! `. 
Help for a specific command can be obtained by using the __help prefix__ `? `instead.
If a command has no help, the bot responds with a default message.

If a command has options, they are added with the __option prefix__ `-- `. 
The order of the options does not matter, 
but the argument corresponding to an option must always follow the option.
When incompatible options are put together, or if incorrect / incomplete arguments are given,
the bot will respond with an error message.

In this documentation and in the help commands, __brackets__ `[] `
indicate optional options and arguments.

Some commands require specific permissions to be executed.
When the bot is invited to a server, permission-specific commands are all
available to all users with admin rights. 
Permissions can be given to people with the commands listed in [the dedicated section](#permissions).
The help commands should let you know what commands you are allowed to use.

All persistent data (including commands added with `!command --add `) is saved for a specific server.
To save data, a channel is created within the server with visibility for the bot and admins only.
Visibility can be granted to others with specific [permissions](#permissions).

## Raid

### Scheduling

`!raidschedule [--timezone timezone]`
`!raid [--timezone timezone]`

Displays the current default raid schedule for the specified timezone.
  - If no timezone is specified, the default time zone is used.

`!raidschedule --edit --days days --times times --defaulttimezone timezone [--descriptions descriptions]`

Edits the current default raid schedule.
  - Requires [scheduler permissions](#scheduler).
  - Days must be given with the format `$day1$/$day2$/.../$lastday$` where days are plainly spelled out.
  - Times must have the `HH:MM/HH:MM/.../HH:MM` 24h-format, where times are in the same order as days. 
  - Times will be registered for the given default time zone.
  - Descriptions are given with the `$description1$/$description2$/.../$lastdescription$` format, in the same order as days.

`!raid --add [--on] date --at time [--timezone timezone] [--description description] `

Schedules an extra raid for the specified date at the specified time. 

  - Requires [scheduler permissions](#scheduler).
  - Time must be given with the `HH:MM` 24h-format.
  - If no timezone is specified, the current default time zone is used. 
  - If no default timezone is set, the `--timezone` option becomes mandatory.
  - The date must be given with the `DD/MM/YYYY` format.

`!raid --cancel [--on] date [--because reason]`  
`!raid --cancel --nextraid [--because reason]`

Cancels raids on the specified date.

  - Requires [scheduler permissions](#scheduler).
  - If several raids happen to be planned on that day, they are all removed.
  - Raids cannot be cancelled on a day when no raid is planned.
  - Extra raids can also be cancelled and will be displayed like normal cancelled raids with `!nextraids`.

`!nextraid`
`!nextraids [number]`

Displays the next raid(s)' date(s) and time(s).
- This command also displays cancelled raids between the moment it is used and the last displayed raid. 
- If no number is specified, the next 3 raids are returned.

`!absence`
`!absences`

Lists the upcoming absences (dates and users).

`!absence --add [--on] date [--of @user] [--because reason]`

Adds an absence on the specified date for a certain user.
  - If no user is specified, the message sender is considered to be the absent user.
  - There can only be one absence for a certain user on a certain day.

**NOTE** : Raids are not cancelled automatically upon adding an absence.
  
`!absence --remove [--on] date --of @user`

Removes an absence on the specified date for the specified user.
  - An absence can only be removed by the user themselves, or someone with [scheduler permissions](#scheduler).
  - An absence cannot be removed if it was not planned.

### Guides

`!guides [--categories]` 

Lists the existing guides, grouped up per category.
  - The `--categories` option allows the display of categories only.

`!guides [--in] category` 

Lists the existing guides for the specified category.

`!guides [--in] category --for name`
`!guide [--for] name [--in category]`

Displays the specified guide.
  - The `--in` option of `!guide` is mandatory if several guides have the same name across different categories.

`!guides --add [--description] description --in category [--for name]`

Adds a guide with the specified name and description in the specified category.
  - Requires [strategist permissions](#strategist).
  - If no name is specified for the guide, a generic name is automatically attributed to the category.
  - If the generic name is already taken, a new name is generated (e.g. all >> all2).
  - Guides within the same category must all have different names.
  
`!guides --edit [--description] description [--in category] --for name `  

Edits the specified guide with the given name and description.
  - Requires [strategist permissions](#strategist).
  - The `--in` option is mandatory if several guides have the same name across different categories.
  
`!guides --remove --category category`

Removes the specified category.
  - Requires [strategist permissions](#strategist).
  - The category must be empty for it to be removed. 
  This is a safety measure to avoid deleting several guides by accident.

`!guides --remove name [--in category]`

Removes the specified guide.
  - Requires [strategist permissions](#strategist).
  - The `--in` option is mandatory if several guides have the same name across different categories.


### Achievements
  
`!achievements`   

Lists the existing achievements.

`!achievements [name]`

Displays the details for the specified achievement.

`!achievements --add [--name] name [--description description] [--screenshot link] [--vod link] [--date date]`   

Adds the specified achievement.
- Requires [strategist permissions](#strategist).
- If a date is not added, the current date is set.
- If an achievement with that name already exists, an error message is returned.

`!achievements --edit [--name] name [--description description] [--screenshot link] [--vod link] [--date date]`   

Edits the specified achievement.
- Requires [strategist permissions](#strategist).
- If a date is not added, the current date is set.
- If an achievement with that name already exists, an error message is returned.

`!achievements --remove [--name] name`

Removes the specified achievement.
- Requires [strategist permissions](#strategist).
- If an achievement with that name does not exist, an error message is returned.

## Fun


`!quote [number]`  
`!quote --add quote [--by @user]`  
`!quote [--number] number --edit quote`  
`!quote [--number] number --editauthor @user`  
`!quote [--number] number --remove`

**|||||||| **WITH KORD** ||||||||**
Consider using [Message Commands](https://discord.com/developers/docs/interactions/application-commands#message-commands).

- Quotes are displayed in italics, between quotation marks, followed by the date at which they were added.   
- If an author was specified, it is also displayed.  
- All quotes are attributed a number when created. 
- If no number is specified when browsing quotes, a random one is returned.  
- If an invalid number is provided when browsing, editing or removing a quote,
an error message is returned.  
- The edit and remove commands require the [archivist permissions](#archivist).


`!bonk @user`
`!bonk [@user] --count`
`!bonk --champion`

**|||||||| **WITH KORD** ||||||||**
Consider using [User Commands](https://discord.com/developers/docs/interactions/application-commands#user-commands).


- When a user is bonked, they are attributed the **Horny** role, 
which grants access to the `#horny-jail`channel. Their nickname is also modified.
- The role (and access to the channel) are removed from the user after 24 hours.
- If the channel and/or role do not exist, they are created.  
- The `--count`option returns how many times the mentioned user was bonked. 
When no user is mentioned, the count is returned for the command user.  
- The `--champion`option returns the top bonked person and their bonk count.


`!jail ``!hornyjail `

Lists all users currently imprisoned in `#horny-jail `.

`!free @user `

Frees a bonked user from horny jail. Requires [keeper permissions](#keeper).

`!uwu --makeitstop `

Causes the bot to start talking like a cutie.


## Bot management

### Permissions

Permissions are a way for the bot to know if a user is allowed to use certain commands. 

When a permission is assigned to someone, a corresponding role is created. 
The bot saves a reference to these roles for each server.
If these roles are deleted, the reference must be updated, 
and the new role reassigned to people who had it before.  

Mind that discord allows duplicate role names, so it is not recommended having any role named `bot-xxx`
to avoid confusion.  

Once the bot has created a role, the role can bee assigned by moderators through server settings. 
The bot will check both for the in-house permissions and roles 
to know if someone is allowed to use a command.
If a user has the role, but not the bot permission, the bot's list must be updated accordingly.

`!permission --grant permission --to @user `  
`!permission --remove permission --from @user `

**|||||||| **WITH KORD** ||||||||**
Consider using [User Commands](https://discord.com/developers/docs/interactions/application-commands#user-commands).


Grants or removes the given permission of the given user. 
This command requires at least [manager permissions](#manager).

The permission hierarchy is as follows:

- #### admin
  - This is the only permission that does not have a specified role. 
  It is recognized by the bot through role permissions: anyone with admin permissions for the server
  has admin permissions for the bot.
  - Admins can assign other permissions to other users, either via command or via discord roles.
  - They have access to all commands.

- #### manager
  - Corresponding role name: `bot-manager`.
  - Managers are allowed to grant and remove other people's permissions except for [manager permissions](#manager).
  - It does not give them access to all commands by default, 
  but they can assign themselves the corresponding permissions.

- #### developer
  - Corresponding role name: `bot-developer`.
  - Allows the creation, edition and removal of commands.
  - Grants visibility to bot data.

- #### forbidden
  - Corresponding role name: `bot-forbidden`.
  - Users with this "permission" will be ignored by the bot.
  - It cannot be assigned to admins.
  - The bot should assign this permission to itself.

- #### strategist
  - Corresponding role name: `bot-strategist`.
  - Gives access to some commands listed under the [Fights](#raid) section.

- #### scheduler
  - Corresponding role name: `bot-scheduler`.
  - Gives access to some commands listed under the [Scheduling](#scheduling) section.

- #### archivist
  - Corresponding role name: `bot-archivist`.
  - Grants permission to edit and remove quotes.

- #### keeper
  - Corresponding role name: `bot-keeper`.
  - Grants the keys to horny jail.

`!permission --list [--oneline]`

Lists all existing permissions. Unless the `--oneline` option is used, descriptions are displayed as well.

`!permission --add name [--description description] [--aggregate permission1 permission2 ...]`  
`!permission --remove name `  
`!permission --edit name --description description`

[Manager permissions](#manager) are required to add, remove or edit a custom permission.
Default permissions cannot be edited or removed.

- Custom permissions can be added with an optional description.
- Without the `--aggregate` option, they are all put at the bottom of the permission hierarchy:
their purpose is mainly to be used with custom commands.
- With the `--aggregate` option, the new permission will regroup the rights given by other permissions.
  - None of these permissions can be [admin](#admin) or [forbidden](#forbidden).
  - Only admins can aggregate [manager](#manager) permissions.



### Commands

Adding, editing and removing commands require [developer permissions](#developer) by default.

`!command commandname` 

Alias for `?$commandname$`.

`!command --add commandname --text text [--managedby permission1 permission2 ...]`  
`!command --add commandname --text text [--managedby @user1 @user2 ...]` 

- Adds a simple command that returns the given text when using `!$commandname$`. 
- New commands cannot use an existing command name.
- The `--managedby` option defines who can edit or remove this command. 
  - [Admins](#admin), [managers](#manager) and [developers](#developer) will have these rights no matter what.
- Help for this command will be generated automatically, and will return:
  - The command creator
  - The date of creation
  - Who has editing rights over it (permissions and people)

`!command --edit commandname --text text [--managedby permission1 permission2 ...]`  
`!command --edit commandname --text text [--managedby @user1 @user2 ...]`  
`!command --edit --addmanager permission1 permission2 ...`  
`!command --edit --addmanager @user1 @user2 ...`  
`!command --edit --removemanager permission1 permission2 ...`  
`!command --edit --removemanager @user1 @user2 ...`

Lets anyone with editing rights change the command's text and permissions.

### Utility

`!commands `

Returns a list of all existing commands. Only their names are displayed.

`!ffenixbot`

Returns a description of this bot.

`!help`

Returns an explanation of how to use a command's help.

`!poll title --options options`

- Generates a simple reaction-based poll based on the given options.
- Options must be given with the format `$option1$/$option2$/.../$lastoption$`.
- The bot returns an embed.
  - The title is the title given as argument.
  - All options are listed and assigned a random emoji.
  - The bot reacts to the generated message with all the given emoji.

`!ffenixconfig ` 

Generates a number of custom commands and permissions designed for the FF-Enix discord server.
- `!ffenix`: static description.
- `!act`: free company description.
- `!$membername$`: description, image and quote for all FF-Enix members.

`!resetbotdata --iamabsolutelycertainthatiwanttodothis`

Resets all bot data to what it is when it joins a server. 
- ⚠️**Custom commands and permissions will be lost, 
as well as scheduling information and all other persistent data.** ⚠️

***

## Other ideas

`/remind me/@able`
`/rng $number$` `/d2/6/10/20`
`/catpic`

In the future, currency / gamble system?

