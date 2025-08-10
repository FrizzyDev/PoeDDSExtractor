# PoeDDSExtractor
## About
Working on a personal project of mine for Path of Exile, I wanted to be able to easily extract textures within the .dds files Path of Exile ships with. After a ton of research and experimentation,
and with the help of tools like [LibGGPK3](https://github.com/aianlinb/LibGGPK3/tree/main) and Microsoft's [texconv.exe](https://github.com/microsoft/DirectXTex/wiki/texconv), PoeDBBExtractor is able
to work with both to extract specified .dds files for PoE's UI and convert them to .png to then extract each texture stored on the original .dds file. The entire process keeps the path and naming structure relatively intact.
The user can specify what .dds files they want and even specify what textures from that .dds file they want. If all UI/Interface .dds files and textures are required, the library can extract everything, but this is a very lengthy process
( around 2 hours with my testing ).
Paths for .dds files and texture names can be found within the Content.gppk uiimages.txt file. This txt file contains the texture path and names, what .dds files those textures are stored on, and the x1, x2, y1, y2 coordinates of each texture.

Sounds great! There is a few caveats to this process. While extracting UI/Interface .dds files and textures is pretty straight forward, divination card textures and UI textures is all that can be deterministically extracted. Other .dds files unrelated to ui/interface can be extracted, but there is no coordinate information like there is for interface textures.
The second caveat is the command line tool from LibGGPK3 is inherently slow, and does not allow for batch commands. This means you can only extract one file at a time. In my testing, extracting all UI .dds files takes around an hour and a half. Long time!
<del>The third caveat is this tool only works with the standalone PoE client. The current LibGGPK3 command line tool does not support _index.bin files that is distributed with the steam version, so I am unable to make calls to that tool for steam distros. </del>
I have written a custom .exe with the LibGGPK3 binaries ( "ExtractGGPK.exe" ) that can now handle the extraction of both .dds files and .bank files from the Content.ggpk file, or the _.index.bin file. 
Regarding bank file extraction, the banks do not need to be extracted from the _.index.bin file, and instead requested banks are located by GGPK2 on disk.
## Usage
Now time for the fun!
PoeDDSExtractor usage for extracting everything can look like this:

```java
//GGPK.java still exists, but use GGPK2.java instead, the process is more streamlined and easier to follow.

//Download LibGGPK3 and texconv.exe to desired folder here using ToolsUnpacker
//Extract LibGGPK3 to desired folder here using ToolsUnpacker
//When instantiating GGPK, it'll attempt to export the ExtractGGPK.exe needed for bank files.

Path gppkLocation = ...
Path contentLocation = ...
Path outputLocation = ...
Path convertBatLocation = ...
Path texConvLocation = ...

GGPK2 ggpk = new GGPK2( ggpkLocation, contentLocation, false, true );
List < DDSFile > ddsFiles = ggpk.extract( outputLocation, buildWantedFiles() );

DDSConverter2 converter = new DDSConverter2( convertBatLocation, texConvLocation, true );
ddsFiles = converter.convert( ddsFiles );

DDSExtractor2 extractor = new DDSExtractor2( ggpk.getuiImagesTxtFile(), true  );
ddsFiles = extractor.extractSubTextures( ddsFiles );
//Do what you want after this. Each DDSFile in the list will now contain the .dds file, converted .png file, list of textures in the .dds file, and a list of extracted textures.
```
## Some more usage information
There's methods in GGPK2 to extract specific files, as well as in DDSExtractor2 to extract specific textures. The catch is you have to specify the paths your self. You can use VisualLibGGPK3 to browse the Content.ggpk file to get these paths yourself.
GGPK2, DDSConverter2, and DDSExtractor2 all contain an overwrite boolean flag, in which if it is set to false, any previously extracted .dds files, converted .dds files, or extracted textures will be skipped. This is useful if you run into errors ( hopefully not, but you never know)
so you don't have to redo what has already been done. GGPK2 will eventually have a process to run the extraction tool multiple times at the same time and that is what the duplicate flag is for, but it is not implemented. To expand on that a bit, LibGGPK3 is not thread safe and also locks
the Content.ggpk file when in use, so attempting to interact with multiple instances fails. 

## Going forward
I'm pretty positive there is still some texture extraction issues to iron out, but there is a lot of files, so I am testing for a lot of potential cases. Additionally, while I can't currently speed up the .dds file extraction process, I am looking into speeding up everything else surrounding it.
If anyone has ideas, fixes, or recommendations, I am always all ears and appreciate it. I'd like to add that I am not a professional developer and I do this in my spare time as a hobby. I do not promise there isn't any questionable code in the library, especially since I've asked myself WTF? why did I do that?
multiple times already, but such is life, as is learning.

I would like to dig in and see if I can figure out how to decode the .dds format PoE uses straight from Java. That'll cut out texconv.exe and maybe speed up a portion of the library. [TwelveMonkeys](https://github.com/haraldk/TwelveMonkeys) supports multiple .dds formats except for the one PoE uses ( of course ). Maybe at some
point I'll spend some time working towards adding a plugin. I'm also considering building a UI.

