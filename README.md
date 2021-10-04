# Bedroom
Due to the pandemic people have started working remotely, _working from the bedroom couldn't get any easier!_
Or... maybe it just did. Bedroom is an open source Java application developed in order to aid call center 
agents in keeping track of their orders and orders per hour to meet their quotas effectively.

### Background
This was my first Java program as a self taught high school student, it contains zero dependencies with all 
the visuals created using the Swing API. It was originally created for me and my friends working at a call 
center to keep track of our orders/hr without doing the math ourselves, enabling us to share it quicker.

---

## Table of contents
* [Prerequisites](https://github.com/swiftsatchel/bedroom#prerequisites)
* [How to set up](https://github.com/swiftsatchel/bedroom#how-to-set-up)
* [How to use](https://github.com/swiftsatchel/bedroom#how-to-use)
   * [Keyboard shortcuts](https://github.com/swiftsatchel/bedroom#keyboard-shortcuts)
* [Compiling from source](https://github.com/swiftsatchel/bedroom#compiling-from-source)
* [License](https://github.com/swiftsatchel/bedroom#license)

## Prerequisites
**For running and/or compiling the program** the newest [JDK](https://www.adoptium.net) is recommended, while 
the minimum required version is stated under the release you are trying to run (Ex: For Bedroom 3, Java 16+ is 
required.)

## How to set up
Download the .jar file from the Releases section of the version you want to use, and double click it like any 
other application.

**Optionally, a start script** could be used to reduce Bedroom's memory usage. For Windows, a .bat file can 
be made in Bedroom's location containing ```start javaw -Xmx32M -Xms16M -jar bedroom-3.jar``` (replacing 
_bedroom-3_ with the correct file name) and then making a shortcut to it. To keep a command prompt opened, 
remove the w in ```javaw```. The arguments ```-Xmx``` and ```-Xms``` set the maximum and initial amounts of 
memory the [JVM](https://en.wikipedia.org/wiki/Java_virtual_machine) can allocate to itself respectively.

## How to use
Upon opening Bedroom you can set your clock in and out times, if you ever mess up on these dialogs you may 
close them to go back (closing the clock in time dialog will close Bedroom.) You should then see your Set 
Break and Add Order buttons, with information on the right about your current shift. Bedroom saves your
shift's performance when it closes, storing your final orders per hour with the ending date of your shift.

### _Keyboard shortcuts:_
* **Adding/removing orders:** _Up Arrow_ & _Down Arrow_ respectively.
* **Open Set Break dialog:** _Number Row 0_
* **Exit/go to previous select time dialog:** _Escape_
* **Accept time in select time dialog:** _Enter_
* **Open Performance History Chart:** _Back-slash_ (```\```)
   * _Right-clciking_ on or above a date's bar will open an option to delete this date's data.
* **Open Settings:** _Backspace_ or _Delete_
   * Holding _Shift_ while dragging the color sliders will make them all the same value.
* Any default Swing shortcuts.

_These shortcuts are meant to be unobtrusive to work applications,
hence their seemingly random keyboard placements._

## Compiling from source
_This is not supported, there could be loss of data or other bugs with things currently being experimented on._

After downloading the source code, extract the folder inside and delete the original zipped file. Then, open the 
extracted folder with your Terminal/Command Prompt and running gradle's build command.

* **On Windows** you can cd into the folder's directory in Command Prompt and run ```gradlew build```.
* **On macOS** or other UNIX operating systems, (on macOS you may need to run ```chmod +x gradlew``` before this
works) cd into the folder's directory and run ```./gradlew build```. 

Once finished, the resulting files will be in the ```build``` folder. The .jar will be in ```build > libs``` and 
gradle's default run scripts will be in ```build > bin```.

## License
Bedroom is licensed under the GPLv3 license, more information can be seen in the 
[license](https://www.github.com/swiftsatchel/bedroom/blob/v3.0-dev/LICENSE) file.
