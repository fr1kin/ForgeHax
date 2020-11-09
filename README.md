# ForgeHax
[![](https://img.shields.io/badge/download-1.12.2%20latest-blue.svg?logo=java)](https://jenkins.nhackindustries.com/job/ForgeHax/job/master/lastSuccessfulBuild/)
[![](https://img.shields.io/badge/download-1.15.2%20latest-blue.svg?logo=java)](https://jenkins.nhackindustries.com/job/ForgeHax/job/1.15/lastSuccessfulBuild/artifact/build/libs/)
[![](https://img.shields.io/badge/download-1.16.4%20latest-blue.svg?logo=java)](https://jenkins.nhackindustries.com/job/ForgeHax/job/1.16/lastSuccessfulBuild/artifact/build/libs/)

[![Build Status](https://jenkins.nhackindustries.com/buildStatus/icon?job=ForgeHax/1.16)](https://jenkins.nhackindustries.com/job/ForgeHax/job/1.16)
[![](https://img.shields.io/matrix/forgehax:nerdsin.space.svg?label=%23forgehax%3Anerdsin.space&logo=matrix)](https://matrix.to/#/#forgehax:nerdsin.space)

A Minecraft cheat that runs as a Forge mod.

## Installing

1. Download the latest version of [Minecraft Forge](https://files.minecraftforge.net/) for the corresponding ForgeHax Minecraft version (this is important if you want to run older versions of ForgeHax).
2. Download the latest ForgeHax build. Do NOT install the jar that ends with `-sources.jar`. That one contains the source code and isn't compiled.
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

Read the FAQ on the [wiki](https://github.com/fr1kin/ForgeHax/wiki/FAQ)
