; -- Example1.iss --
; Demonstrates copying 3 files and creating an icon.

; SEE THE DOCUMENTATION FOR DETAILS ON CREATING .ISS SCRIPT FILES!

[Setup]
SetupIconFile=PixelPerfectIcon.ico
AppName=Nostalgic Pixels Pixel Perfect
AppVersion=2.0
DefaultDirName={pf}\Nostalgic Pixels Pixel Perfect
DefaultGroupName=Nostalgic Pixels Pixel Perfect
UninstallDisplayIcon={app}\PixelPerfect.exe
Compression=lzma2
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
OutputBaseFilename=InstallPixelPerfect
OutputDir=.
;If you want to associate a program with an extension just add this instruction in your iss file
;https://stackoverflow.com/questions/26536030/file-association-in-inno-setup
ChangesAssociations = yes

[Files]
;Source: "jre-8u281-windows-x64.exe"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\java_installer"

Source: "PixelPerfect.exe"; DestDir: "{app}"

Source: "..\config\_default_config.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\config"
Source: "..\config\default_project.pbp"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\config"

Source: "..\pallettes\_default_pallette_withSorting.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\pallettes"
Source: "..\pallettes\____Biggie_Beads.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\pallettes"
Source: "..\pallettes\__neutrals.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\pallettes"
Source: "..\pallettes\_default_pallette_LThanda.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\pallettes"
Source: "..\pallettes\____primary_white_black_perler.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\pallettes"
Source: "..\pallettes\____obama_hope_pallette.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\pallettes"
Source: "..\pallettes\____rainbow_pallette.xml"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\pallettes"

Source: "..\images\inigo_montoya.png"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\images"
Source: "..\images\dog.png"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\images"
Source: "..\images\Eevee GBA Green-Red.png"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\images"
Source: "..\images\Kirby Puckett Baseball Card.png"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\images"
Source: "..\images\Dabbing Unicorn.png"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\images"

Source: "..\icon\BeadMakerIcon.png"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\icon"

Source: "..\LUTs\default.png"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\LUTs"

Source: "..\log\log.txt"; DestDir: "{userappdata}\Nostalgic Pixels Pixel Perfect\log"

[Run]
;https://stackoverflow.com/questions/4188914/the-requested-operation-requires-elevation-message-after-setup
;Filename: "{app}\java_installer\jre-8u191-windows-x64.exe"; Description: "Install Java (64-bit)"; Flags: postinstall skipifsilent
;Filename: "{userappdata}\Nostalgic Pixels Pixel Perfect\java_installer\jre-8u281-windows-x64.exe"; Description: "Install Java (64-bit)"; Flags: postinstall skipifsilent shellexec
Filename: "https://java.com/download/"; Flags: shellexec runascurrentuser postinstall unchecked; Description: "Would you like to download and install Java?  Java 8 is required to run Pixel Perfect.  Some systems may already have Java installed.  If Pixel Perfect does not start up, re-run this installer and check this box to download and install Java."

[Icons]
Name: "{group}\Pixel Perfect"; Filename: "{app}\PixelPerfect.exe"

;If you want to associate a program with an extension just add this instruction in your iss file
;https://stackoverflow.com/questions/26536030/file-association-in-inno-setup
[Registry]
Root: HKCR; Subkey: ".pbp";                                            ValueData: ".pbp_auto_file";          Flags: uninsdeletekey; ValueType: string;  ValueName: ""
;Root: HKCR; Subkey: ".pbp_auto_file";                                  ValueData: "NostalgicPixelsPixelPerfect";          Flags: uninsdeletevalue; ValueType: string;  ValueName: ""
;Root: HKCR; Subkey: "NostalgicPixelsPixelPerfect";                     ValueData: "Program NostalgicPixelsPixelPerfect";  Flags: uninsdeletekey;   ValueType: string;  ValueName: ""
;Root: HKCR; Subkey: "NostalgicPixelsPixelPerfect\DefaultIcon";         ValueData: "{app}\PixelPerfect.exe,0";               		                   ValueType: string;  ValueName: ""
Root: HKCR; Subkey: ".pbp_auto_file\shell\open\command";  ValueData: """{app}\PixelPerfect.exe"" ""%1""";  	 Flags: uninsdeletekey;		                     ValueType: string;  ValueName: ""

[Code]
procedure CurUninstallStepChanged (CurUninstallStep: TUninstallStep);
begin
    case CurUninstallStep of                   
		usPostUninstall:
			begin
				DelTree(ExpandConstant('{userappdata}\Nostalgic Pixels Pixel Perfect'), True, True, True);
			end;
	end;
end;
