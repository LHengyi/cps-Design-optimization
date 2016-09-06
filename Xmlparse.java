package hybrid;

import java.io.*;
import java.util.*;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.*;

public class Xmlparse{
	protected String parsefile = null;
	protected SAXBuilder saxBuilder = new SAXBuilder();
	protected Document document = null;

	public Xmlparse(String name){
		parsefile = name;
	}

	public void parseperiod(Double[] tokens){
		try{
			File inputFile = new File(parsefile);
			if (!inputFile.exists()) {
				System.out.println("error: cannot find 4controller.xml");
				return;
			}

			String xml = "<?xml ...";
			xml = xml.trim().replaceFirst("^([\\W]+)<","<");
			document = saxBuilder.build(inputFile);
			System.out.println("Root element :" + document.getRootElement().getName());
			Element root = document.getRootElement();
			List<Element> propertylist = root.getChildren("property");
			for (Element el : propertylist){
				if (el.getAttributeValue("name").equals("SamplingPeriod1")){
					if (tokens[0] != null) {
						el.getAttribute("value").setValue(tokens[0].toString());
					}
				} else if (el.getAttributeValue("name").equals("SamplingPeriod2")){
					if (tokens[1] != null) {
						el.getAttribute("value").setValue(tokens[1].toString());
					}
				} else if (el.getAttributeValue("name").equals("SamplingPeriod3")){
					if (tokens[2] != null) {
						el.getAttribute("value").setValue(tokens[2].toString());
					}
				} else if (el.getAttributeValue("name").equals("SamplingPeriod4")){
					if (tokens[3] != null) {
						el.getAttribute("value").setValue(tokens[3].toString());
					}
				} //else if (el.getAttributeValue("name").equals("Executiontime1")){
				// 	el.getAttribute("value").setValue("0.0");
				// } else if (el.getAttributeValue("name").equals("Executiontime2")){
				// 	el.getAttribute("value").setValue("0.0");
				// } else if (el.getAttributeValue("name").equals("Executiontime3")){
				// 	el.getAttribute("value").setValue("0.0");
				// } else if (el.getAttributeValue("name").equals("Executiontime4")){
				// 	el.getAttribute("value").setValue("0.0");
				// }
			}
			// List<Element> entitylist = root.getChildren("entity");
			// for (Element ele : entitylist ) {
			// 	String name = ele.getAttributeValue("name");
			// 	if (name.contains("Controller")) {
			// 		propertylist = ele.getChildren("property");
			// 		for(Element mem : propertylsit){
			// 			String pname = mem.getAttributeValue("name");
			// 			if(pname.contains("DecoratorAttributesFor_FixedPriorityScheduler")){
							
			// 			}
			// 		}
			// 	}
			// }
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void parse(String[] tokens, int numberoftokens){
		try{
			File inputFile = new File(parsefile);

			String xml = "<?xml ...";
			xml = xml.trim().replaceFirst("^([\\W]+)<","<");
			document = saxBuilder.build(inputFile);
			System.out.println("Root element :" + document.getRootElement().getName());
			Element root = document.getRootElement();
			List<Element> propertylist = root.getChildren("property");
			for (Element el : propertylist){
				if (el.getAttributeValue("name").equals("End2EndLatency1")){
					el.getAttribute("value").setValue(tokens[0]);
				} else if (el.getAttributeValue("name").equals("End2EndLatency2")){
					el.getAttribute("value").setValue(tokens[1]);
				} else if (el.getAttributeValue("name").equals("End2EndLatency3")){
					el.getAttribute("value").setValue(tokens[2]);
				} else if (el.getAttributeValue("name").equals("End2EndLatency4")){
					el.getAttribute("value").setValue(tokens[3]);
				} else if (el.getAttributeValue("name").equals("Executiontime1")){
					el.getAttribute("value").setValue("0.0");
				} else if (el.getAttributeValue("name").equals("Executiontime2")){
					el.getAttribute("value").setValue("0.0");
				} else if (el.getAttributeValue("name").equals("Executiontime3")){
					el.getAttribute("value").setValue("0.0");
				} else if (el.getAttributeValue("name").equals("Executiontime4")){
					el.getAttribute("value").setValue("0.0");
				}
			}

			List<Element> entitylist = root.getChildren("entity");
			for (Element  el : entitylist){
				if (el.getAttributeValue("name").equals("PtidesPlatform")){
					List<Element> ptidescontents = el.getChildren("entity");
					for (Element content : ptidescontents){
						List<Element> portlist = content.getChildren("port");
						for (Element portelemet : portlist){
							if (portelemet.getAttributeValue("name").equals("out1")){
								propertylist = portelemet.getChildren("property");
									for (Element propertyelement : propertylist){
										if (propertyelement.getAttributeValue("name").equals("actuateAtEventTimestamp")){
											propertyelement.getAttribute("value").setValue("true");
										}
									}
								}
							else if (portelemet.getAttributeValue("name").equals("out2")){
								propertylist = portelemet.getChildren("property");
								for (Element propertyelement : propertylist){
									if (propertyelement.getAttributeValue("name").equals("actuateAtEventTimestamp")){
										propertyelement.getAttribute("value").setValue("true");
									}
								}
							}
							else if (portelemet.getAttributeValue("name").equals("out3")){
								propertylist = portelemet.getChildren("property");
								for (Element propertyelement : propertylist){
									if (propertyelement.getAttributeValue("name").equals("actuateAtEventTimestamp")){
										propertyelement.getAttribute("value").setValue("true");
									}
								}
							}
							else if (portelemet.getAttributeValue("name").equals("out4")){
								propertylist = portelemet.getChildren("property");
								for (Element propertyelement : propertylist){
									if (propertyelement.getAttributeValue("name").equals("actuateAtEventTimestamp")){
										propertyelement.getAttribute("value").setValue("true");
									}
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void saveXML(){
		try{
			XMLOutputter xmlopt = new XMLOutputter();
			FileWriter writer = new FileWriter(parsefile);
			Format fm = Format.getPrettyFormat();
			xmlopt.setFormat(fm);
			xmlopt.output(document, writer);
			writer.close();
		} catch (IOException ioe2){
			ioe2.printStackTrace();
		}
	}

}