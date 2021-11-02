# Scheduling

`/nextraids`

Display the upcoming raids.

`/schedule`

Edit the current raid schedule, exceptionally add or cancel raids, or manage absences.  
Requires the **Scheduler** role.


***

## Displaying the raid schedule

### Get a list of valid timezones

`/timezones`

Return a list of timezone IDs. It can also be found [here](https://gist.github.com/arpit/1035596).

### Display the default raid schedule

`/nextraids default [timezone]`

Display the default raid schedule, regardless of punctual events.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `timezone` | String | The ID of the timezone to output raids in. Use `/timezones` for a list of valid IDs. | No |

### Display a number of raids

`/nextraids bynumber [howmany] [timezone]`

Display a number of upcoming raids, including cancelled and exceptional raids, as well as absences up until then.  
If no number is specified, the next three raids are displayed. If no timezone is specified, the default timezone is
used.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `howmany` | Int | The number of raids to display. Must be 1 or more.| No |
| `timezone` | String | The ID of the timezone to output raids in. Use `/timezones` for a list of valid IDs. | No |

### Display raids until a certain date

`/nextraids until [date] [timezone]`

Display upcoming raids until a certain date, including cancelled and exceptional raids, as well as absences up until
then.  
If no timezone is specified, the default timezone is used.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `date` | String | The limit date. Must be in the `DD/MM/YYYY` format. | Yes |
| `timezone` | String | The ID of the timezone to output raids in. Use `/timezones` for a list of valid IDs. | No |

***

## Editing the default raid schedule

### Set the default raid schedule's timezone

`/schedule default timezone [timezone]`

Set the default timezone for the default raid schedule. Defaults to CET.  
This defines the timezone displayed by default when using `/nextraids`.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `timezone` | String | The ID of the timezone to output raids in. Use `/timezones` for a list of valid IDs. | Yes |

### Adding a day to the default raid schedule

`/schedule default add [day] [time] [timezone] [comment] `

Add a new raid to the default schedule.  
There can only be one raid on a certain day starting at a certain time.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `day` | Multiple-choice string | The day of the week when the raid happens. | Yes |
| `time` | String | The time of the day when the raid starts. Must be in the `HH:MM` 24h format. | Yes |
| `timezone` | String | The ID of the timezone the times are entered in. Use `/timezones` for a list of valid IDs. | Yes |
| `comment` | String | A comment to append to the raid. | No |

### Removing a day from the default raid schedule

`/schedule default remove [day] [time] [timezone]`

Remove a raid from the default schedule.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `day` | Multiple-choice string | The day of the week when the raid happens. | Yes |
| `time` | String | The time of the day when the raid starts. Must be in the `HH:MM` 24h format. | Yes |
| `timezone` | String | The ID of the timezone the times are entered in. Use `/timezones` for a list of valid IDs. | Yes |

## Exceptions to the raid schedule

### Cancel a default raid

`/schedule cancel on [date] [time] [timezone] [comment]`

Cancel one of the default schedule's raids for a specific date.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `date` | String | The day of the raid. Must be in the `DD/MM/YYYY` format. | Yes |
| `time` | String | The time of the day when the raid starts. Must be in the `hh:mm` 24h format. | Yes |
| `timezone` | String | The ID of the timezone the times are entered in. Use `/timezones` for a list of valid IDs. | Yes |
| `comment` | String | A comment to append to the cancellation. | No |

### Revert a cancellation

`/schedule cancel revert [date] [time] [timezone]`

Revert a default raid cancellation.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `date` | String | The day of the raid. Must be in the `DD/MM/YYYY` format. | Yes |
| `time` | String | The time of the day when the raid starts. Must be in the `HH:MM` 24h format. | Yes |
| `timezone` | String | The ID of the timezone the times are entered in. Use `/timezones` for a list of valid IDs. | Yes |

### Add an exceptional raid

`/schedule exception add [date] [time] [timezone] [comment]`

Add an exceptional raid on the given date at the given time.  
Exceptional raids can overlap with default raids.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `date` | String | The day of the raid. Must be in the `DD/MM/YYYY` format. | Yes |
| `time` | String | The time of the day when the raid starts. Must be in the `HH:MM` 24h format. | Yes |
| `timezone` | String | The ID of the timezone the times are entered in. Use `/timezones` for a list of valid IDs. | Yes |
| `comment` | String | A comment to append to the raid. | No |

### Cancel an exceptional raid

`/schedule exception cancel [date] [time] [timezone]`

Cancel the specified exceptional raid.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `date` | String | The day of the raid. Must be in the `DD/MM/YYYY` format. | Yes |
| `time` | String | The time of the day when the raid starts. Must be in the `HH:MM` 24h format. | Yes |
| `timezone` | String | The ID of the timezone the times are entered in. Use `/timezones` for a list of valid IDs. | Yes |

***

## Absences

### Adding an absence

`/schedule absence add [who] [date] [comment]`

Add an absence on the given date for the mentioned user.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `who` | User | The person who will be absent. | Yes |
| `date` | String | The day of the absence. Must be in the `DD/MM/YYYY` format. | Yes |
| `comment` | String | A comment to append to the absence. | No |

### Removing an absence

`/schedule absence remove [who] [date]`

Remove an absence on the given date for the mentioned user.

| Argument | Type | Description | Required? |
|:---:|:---:|:---|:---:|
| `who` | User | The person who will be absent. | Yes |
| `date` | String | The day of the absence. Must be in the `DD/MM/YYYY` format. | Yes |
