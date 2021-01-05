; -- Example1.iss --
; Demonstrates copying 3 files and creating an icon.

; SEE THE DOCUMENTATION FOR DETAILS ON CREATING .ISS SCRIPT FILES!

[Setup]
AppName=Nostalgic Pixels Bead Maker
AppVersion=0.9
DefaultDirName={pf}\Nostalgic Pixels Bead Maker
DefaultGroupName=Nostalgic Pixels Bead Maker
UninstallDisplayIcon={app}\BeadMaker.exe
Compression=lzma2
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
OutputBaseFilename=InstallBeadMaker
OutputDir=.

[Files]
Source: "jre-8u191-windows-x64.exe"; DestDir: "{app}\java_installer"

Source: "BeadMaker.exe"; DestDir: "{app}"

Source: "..\config\_default_config.xml"; DestDir: "{app}\config"
Source: "..\config\default_project.pbp"; DestDir: "{app}\config"

Source: "..\pallettes\_default_pallette.xml"; DestDir: "{app}\pallettes"
Source: "..\pallettes\____lego_pallette.xml"; DestDir: "{app}\pallettes"
Source: "..\pallettes\____generic_lego_pallette.xml"; DestDir: "{app}\pallettes"

Source: "..\images\inigo_montoya.png"; DestDir: "{app}\images"
Source: "..\images\dog.png"; DestDir: "{app}\images"
Source: "..\images\Eevee GBA Green-Red.png"; DestDir: "{app}\images"
Source: "..\images\Kirby Puckett Baseball Card.png"; DestDir: "{app}\images"
Source: "..\images\Dabbing Unicorn.png"; DestDir: "{app}\images"



[Run]
Filename: "{app}\java_installer\jre-8u191-windows-x64.exe"; Description: "Install Java (64-bit)"; Flags: postinstall skipifsilent


[Icons]
Name: "{group}\Bead Maker"; Filename: "{app}\BeadMaker.exe"
