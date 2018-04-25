# ForgeHax
A Minecraft cheat that runs as a Forge mod

Latest builds available here: 

[![Build Status](http://jenkins.daporkchop.net/job/Minecraft/job/ForgeHax/badge/icon)](http://jenkins.daporkchop.net/job/Minecraft/job/ForgeHax/)

Since ForgeGradle has updated to 2.3, Java 8 Version 131 (lastest as of writing this) is required. Make sure your JAVA_HOME is set to this version of the JDK. If you don't, setupDecompWorkspace will fail at recompileMc.

You should allocate more ram when running `setupDecompWorkspace` (around 4GB should be ok)

I recommend allocating more ram in both environments as the BlockEsp mod requires a lot of space for the vertex buffers. I might get around to fixing this if it becomes too big of an issue.

# FAQ
_How do I install ForgeHax?_

Download [Minecraft Forge](https://files.minecraftforge.net/) and put the ForgeHax jar into .minecraft/mods

_How do I use commands?_

You use commands by typing `.` chat.

Example: `.help` in chat will print a list of all mods in ForgeHax.

_How do I enter an argument that contains spaces?_

Use quotes.

Example: `.chatbot spam add spam "This will be treated as one argument"`

_How do I see a list of mods?_

Type `.mods <search>` in chat. The search argument is optional.

_How do I toggle a mod?_

Type `.<mod name> enabled 1` to enable, and `.<mod name> enabled 0` to disable.

Example: `.step enabled 1` will enable step hack.

_How do I see a list of commands for a mod?_

Type `.<mod name>` and it should show a list of settings (if any), their current value, and their description.

Example: `.step`

_How do I see a list of options for a command?_

After the command add `-?` or `--help`. Almost every command should have help text for its options by default.

Example: `.step -?` or `.step --help` or `.step enabled -?` etc

_How do I use the Markers mod?_

`.markers options add stone` Will add stone and all its variants to the block list

`.markers options add 1 -i` Will add stone by its block ID

`.markers options add stone -r 255 -g 0 -b 0` Will add stone and give it the color red.

`.markers options add stone -m 1` Will add stone with the meta variant of 1 (Granite)

`.markers options add shulker --regex` Will add any block that contains the word "shulker" in its name

`.markers options remove stone` Will remove stone from Markers

_How do I use the SpamBot mod?_

`.chatbot spam add test` Will add a new entry called "test"

`.chatbot spam add test "Test text"` Will append "Test text" to the test entry message list.

`.chatbot spam add test --type SEQUENTIAL` Will set the spam type to sequential. (Use `.spambot add -?` to see acceptable arguments)

`.chatbot spam add test --keyword !test` Will set the trigger keyword to "!test"
 
`.chatbot spam add test --trigger REPLY` Will set the trigger mode to "REPLY" (Use `.spambot add -?` to see acceptable arguments)
 
`.chatbot spam add test --enabled true` Will enable the spam entry 
 
`.chatbot spam import test import_test.txt` Will import messages from the file "import_test.txt" inside .minecraft/forgehax

`.chatbot spam export test export_test.txt` Will export messages from the entry into a text file under /forgehax.
 
`.chatbot spam remove test` Will remove the test entry and all its contents
