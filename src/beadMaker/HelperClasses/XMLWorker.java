package beadMaker.HelperClasses;

import java.io.File;

import processing.data.XML;

public class XMLWorker extends core.XmlHelper {

	public XML[] configXML = new XML[1];
	public XML[] projectXML = new XML[1];

	String configFilePath;


	//------------------------------------------------------------
	//CONSTRUCTOR
	//------------------------------------------------------------
	public XMLWorker(boolean useAppData, String appDataFolderName) {
		super(useAppData, appDataFolderName);
		if (useAppData) {
			configFilePath = System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "config" + File.separator + "_default_config.xml";
		} else {
			configFilePath = System.getProperty("user.dir") + 								  File.separator + "config" + File.separator + "_default_config.xml";
		}
		configXML = GetXMLFromFile(configFilePath);
		projectXML = GetXMLFromFile(GetAbsoluteFilePathStringFromXml("defaultProjectFilePath", configXML, useAppData, appDataFolderName));
	}
}
	