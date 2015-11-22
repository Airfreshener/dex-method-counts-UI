package info.persistent.dex.controllers;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Vasiliy on 22.11.2015
 */
public class PreferenceController {

    private static PreferenceController pc = null;
    private File appDataFile;

    private PreferenceController(){
        appDataFile = new File(System.getenv("APPDATA") + File.separator + "DexMethodCountsApp.xml");
    }

    public static PreferenceController getInstance(){
        if(pc == null)
            pc = new PreferenceController();
        return pc;
    }

    public String getString(String name, String def){
        return "";
    }

    public void setString(String name, String value){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(appDataFile);
            doc.getDocumentElement().normalize();
            doc.createTextNode("property");
            doc.createTextNode("property");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
