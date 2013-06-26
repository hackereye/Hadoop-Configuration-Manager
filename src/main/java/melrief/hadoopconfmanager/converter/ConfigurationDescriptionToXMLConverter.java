package melrief.hadoopconfmanager.converter;

import java.io.OutputStream;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import melrief.hadoopconfmanager.ConfigurationDescription;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfigurationDescriptionToXMLConverter extends ConfigurationDescriptionConverter {

  Document doc;
  Element root;
  HashSet<String> keys;

  private ConfigurationDescriptionToXMLConverter(Document doc) {
    this.doc = doc;
    this.root = this.doc.createElement("configuration");
    this.doc.appendChild(root);
    this.keys = new HashSet<String>();
  }

  public static ConfigurationDescriptionToXMLConverter newInstance()
      throws ParserConfigurationException {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .newDocument();
    return new ConfigurationDescriptionToXMLConverter(doc);
  }

  @Override
  public boolean addConfigurationDescription(ConfigurationDescription<?> conf) {
    String key = conf.getKey();

    if (keys.contains(key)) {
      return false;
    }

    keys.add(key);

    Element prop = this.doc.createElement("property");

    Element name = this.doc.createElement("name");
    name.appendChild(this.doc.createTextNode(key));
    prop.appendChild(name);

    Element value = this.doc.createElement("value");
    value.appendChild(this.doc
        .createTextNode(conf.getDefaultValue().toString()));
    prop.appendChild(value);

    Element type = this.doc.createElement("type");
    type.appendChild(this.doc.createTextNode(conf.getType()));
    prop.appendChild(type);

    Element description = this.doc.createElement("description");
    description.appendChild(this.doc.createTextNode(conf.getDescription()));
    prop.appendChild(description);

    this.root.appendChild(prop);

    return true;
  }

  @Override
  public void write(OutputStream result) {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(
          "{http://xml.apache.org/xslt}indent-amount", "2");
      DOMSource source = new DOMSource(this.doc);
      transformer.transform(source, new StreamResult(result));
    } catch (TransformerException tfe) {
      tfe.printStackTrace();
    }
  }
}
