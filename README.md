# ForgeHax
![](logo.png)

[![](https://img.shields.io/github/downloads/fr1kin/ForgeHax/total)](https://github.com/fr1kin/ForgeHax/releases)
[![](https://img.shields.io/matrix/forgehax:nerdsin.space.svg?label=%23forgehax%3Anerdsin.space&logo=matrix)](https://matrix.to/#/#forgehax:nerdsin.space)

![Build Status](https://github.com/fr1kin/ForgeHax/actions/workflows/continuous_integration.yml/badge.svg?branch=1.16)

A Minecraft cheat that runs as a Forge mod.

## Installing

1. Download the latest version of [Minecraft Forge](https://files.minecraftforge.net/) for the corresponding 
ForgeHax Minecraft version (this is important if you want to run older versions of ForgeHax).
2. Download the latest ForgeHax build by going to the [releases](https://github.com/fr1kin/ForgeHax/releases) section.
Do NOT install the jar that contains `sources`. That one contains the source code and isn't compiled.
3. Place the ForgeHax jar into the `.minecraft/mods/` directory. If you want to organize by Minecraft version, 
you can place it under `.minecraft/mods/{mc.version}` where `mc.version` is 
the version of Minecraft running (ex: `.minecraft/mods/1.12.2`). NOTE: This is will not work for 1.13+ version! You can
only put the mod jar in the `/mods` folder!
4. Launch Minecraft using the Forge profile. ForgeHax should now be loaded.

## Wiki

If you need any help, please check the [ForgeHax Wiki](https://github.com/fr1kin/ForgeHax/wiki) before submitting an issue.

## Building
ForgeHax uses [Lombok](https://projectlombok.org/) to help eliminate boilerplate code and provide some useful features like
extension methods. If you import ForgeHax into your IDE, make sure you install the [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok)
for your IDE. Otherwise, a lot of code maybe marked as errors when it is actually fine.

To build ForgeHax, you only need to run `./gradlew build`. Make sure gradle is run with JDK8. Newer versions of the JDK
may not be supported by the javac plugin yet.

#### Common build issues

##### gradle build fails when IntelliJ IDEA is running / missing symbol error

Sometimes a fresh build will fail when IntelliJ IDEA is open. This is because the IDE has a file handle open on the javac
plugin jar, and for some reason Lombok is unable to also read the jar at the same time. The result is that lombok will
disable itself, which causes the entire build to fail with 'missing symbol' errors.

Fix: Close IntelliJ IDEA and run ./gradlew build from the terminal. The issue is probably just IntelliJ indexing a
newly added jar. So once it's indexed, you can build with IntelliJ without any issues.

## FAQ

Read the FAQ on the [wiki](https://github.com/fr1kin/ForgeHax/wiki/FAQ)

###### Credit to Rain#4705 for the logo
