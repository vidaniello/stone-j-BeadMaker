Directions for exporting:

1. Export runnable JAR from Eclipse
	a. Extract required libraries into generated JAR
2. Run Launch4j (open Pixelperfect_Launch4jConfig.xml) to create and EXE wrapper for the JAR
3. Run Inno Setup Compiler to wrap all files into a single installer EXE (Compile)

The XCF file is a gimp file with a layer for each icon size. Export the icons from Inkscape at each resolution (16, 32, 64, 128, 256) as PNG, then create a new GIMP file that's 256x256px. Then, drag each PNG into GIMP. They will automatically import on the ir own layers. Then, export the GIMP file as .ico file.

(https://superuser.com/questions/491180/how-do-i-embed-multiple-sizes-in-an-ico-file)

Launch4j EXEs will only work with a "BMP" splashscreen. PNGs WILL NOT WORK.