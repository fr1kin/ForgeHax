# ForgeHax
[![](https://img.shields.io/badge/download-1.12.2%20latest-blue.svg?logo=java)](https://jenkins.nhackindustries.com/job/ForgeHax/job/master/lastSuccessfulBuild/)
[![](https://img.shields.io/badge/download-1.13.2%20latest-blue.svg?logo=java)](https://jenkins.nhackindustries.com/job/ForgeHax/job/1.13/lastSuccessfulBuild/artifact/build/libs/)
[![Build Status](https://jenkins.nhackindustries.com/buildStatus/icon?job=ForgeHax/master)](https://jenkins.nhackindustries.com/job/ForgeHax/job/master)
[![](https://img.shields.io/matrix/forgehax:nerdsin.space.svg?label=%23forgehax%3Anerdsin.space&logo=matrix)](https://matrix.to/#/#forgehax:nerdsin.space)

A Minecraft cheat that runs as a Forge mod.

## Installing

1. Download the latest version of [Minecraft Forge](https://files.minecraftforge.net/) for the corresponding ForgeHax Minecraft version (this is important if you want to run older versions of ForgeHax).
2. Download the [latest ForgeHax build](https://jenkins.nhackindustries.com/job/ForgeHax/job/master/lastSuccessfulBuild/). Do NOT install the jar that ends with `-sources.jar`. That one contains the source code and isn't compiled.
3. Place the ForgeHax jar into the `.minecraft/mods/` directory. If you want to organize by Minecraft version, you can place it under `.minecraft/mods/{mc.version}` where `mc.version` is the version of Minecraft running (ex: `.minecraft/mods/1.12.2`).
4. Launch Minecraft using the Forge profile. ForgeHax should now be loaded.

## Wiki

If you need any help, please check the [ForgeHax Wiki](https://github.com/fr1kin/ForgeHax/wiki) before submitting an issue.

## Known Issues

World Downloader Forge is a mod known to wreak havoc on ForgeHax's asm tweaker. There is some race condition it has with Mixin causing ForgeHax to only successfully transform classes sometimes. Luckly this is the only known mod known to cause this issue, and other Mixin based tweakers work fine with ForgeHax (i.e Liteloader, Future). 

## Building
Java 8 Version 131+ is required. Make sure your JAVA_HOME is set to this version of the JDK. If you don't, setupDecompWorkspace will fail at recompileMc.

You should allocate more ram when running `setupDecompWorkspace` (around 4GB should be ok)

I recommend allocating more ram in both environments as the Markers mod requires a lot of memory for the vertex buffers. I might get around to fixing this if it becomes too big of an issue.

#### Common build issues

##### config.properties tokens are not applied

Sometimes when building, the config.properties resource will not be tokenized. This will break the tweaker because it wont know what mapping version to use. This can be fixed by cleaning your gradle and IDE build output and running setupDecompWorkspace again.

## FAQ
#### How do I install ForgeHax?

Download [Minecraft Forge](https://files.minecraftforge.net/) and put the ForgeHax jar into .minecraft/mods

#### How do I use commands?

You use commands by typing `.` chat.

Example: `.help` in chat will print a list of all mods in ForgeHax.

#### How do I enter an argument that contains spaces?

Use quotes.

Example: `.chatbot spam add spam "This will be treated as one argument"`

#### How do I see a list of mods?

Type `.mods <search>` in chat. The search argument is optional.

#### How do I toggle a mod?

Type `.<mod name> enabled 1` to enable, and `.<mod name> enabled 0` to disable.

Example: `.step enabled 1` will enable step hack.

#### How do I see a list of commands for a mod?

Type `.<mod name>` and it should show a list of settings (if any), their current value, and their description.

Example: `.step`

#### How do I see a list of options for a command?

After the command add `-?` or `--help`. Almost every command should have help text for its options by default.

Example: `.step -?` or `.step --help` or `.step enabled -?` etc

#### How do I use the Markers mod?

`.markers options add stone` Will add stone and all its variants to the block list

`.markers options add 1 -i` Will add stone by its block ID

`.markers options add stone -r 255 -g 0 -b 0` Will add stone and give it the color red.

`.markers options add stone -m 1` Will add stone with the meta variant of 1 (Granite)

`.markers options add shulker --regex` Will add any block that contains the word "shulker" in its name

`.markers options remove stone` Will remove stone from Markers

#### How do I use the SpamBot mod?

`.chatbot spam add test` Will add a new entry called "test"

`.chatbot spam add test "Test text"` Will append "Test text" to the test entry message list.

`.chatbot spam add test --type SEQUENTIAL` Will set the spam type to sequential. (Use `.spambot add -?` to see acceptable arguments)

`.chatbot spam add test --keyword !test` Will set the trigger keyword to "!test"
 
`.chatbot spam add test --trigger REPLY` Will set the trigger mode to "REPLY" (Use `.spambot add -?` to see acceptable arguments)
 
`.chatbot spam add test --enabled true` Will enable the spam entry 
 
`.chatbot spam import test import_test.txt` Will import messages from the file "import_test.txt" inside .minecraft/forgehax

`.chatbot spam export test export_test.txt` Will export messages from the entry into a text file under /forgehax.
 
`.chatbot spam remove test` Will remove the test entry and all its contents
