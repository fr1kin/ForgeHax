# ForgeHax
A Minecraft cheat that runs as a Forge mod

Since ForgeGradle has updated to 2.3, Java 8 Version 131 (lastest as of writing this) is required. Make sure your JAVA_HOME is set to this version of the JDK. If you don't, setupDecompWorkspace will fail at recompileMc.

If you want to build the project on your own, you need to either remove all references to JourneyMap or add the latest [JourneyMap](http://journeymap.info/Download) release into the libs/ folder

You should allocate more ram when running `setupDecompWorkspace` (around 4GB should be ok)

I recommend allocating more ram in both environments as the BlockEsp mod requires a lot of space for the vertex buffers. I might get around to fixing this if it becomes too big of an issue.
