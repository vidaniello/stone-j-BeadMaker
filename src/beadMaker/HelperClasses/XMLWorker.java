package beadMaker.HelperClasses;

import processing.data.XML;

public class XMLWorker extends core.XmlHelper {

	public XML[] configXML = new XML[1];
	public XML[] projectXML = new XML[1];

	static final String configFilePath = System.getProperty("user.dir") + "\\config\\_default_config.xml";


	//------------------------------------------------------------
	//CONSTRUCTOR
	//------------------------------------------------------------
	public XMLWorker() {
		configXML = GetXMLFromFile(configFilePath);
		projectXML = GetXMLFromFile(GetAbsoluteFilePathStringFromXml("defaultProjectFilePath", configXML));
	}
}
