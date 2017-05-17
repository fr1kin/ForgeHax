# ForgeHax
A Minecraft cheat that runs as a Forge mod

If you want to build the project on your own, you need to either remove all references to JourneyMap or add the latest JourneyMap release into the libs/ folder

To load the core mod in the IDE add this to the VM options: 
-Dfml.coreMods.load=com.matt.forgehax.asm.ForgeHaxCoreMod

I recommend allocating more ram in both environments as the BlockEsp mod requires a lot of space for the vertex buffers. I might get around to fixing this if it becomes too big of an issue.