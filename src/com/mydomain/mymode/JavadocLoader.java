package com.mydomain.mymode;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JavadocLoader {
	public static void loadJavaDoc(TreeMap<String, String> jdocMap, File p5Ref) {

		// Pattern pat = Pattern.compile("\\w+");
		try {
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File file) {
					if (file.getName().contains("-")) {
						return false;
					}
					return true;
				}
			};
			mapMethod(jdocMap, p5Ref, fileFilter);// Thos method fills the
													// jdocMap recursively
			System.out.println("JDoc loaded " + jdocMap.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void mapMethod(TreeMap<String, String> jdocMap, File p5Ref,
			FileFilter fileFilter) throws IOException, MalformedURLException {
		if (p5Ref.isDirectory()) {
			for (File docFile : p5Ref.listFiles(fileFilter)) {
				mapMethod(jdocMap, docFile, fileFilter);
			}
		} else {

			Document doc;
			doc = Jsoup.parse(p5Ref, null);
			Elements elms = doc.getElementsByAttributeValue("name",
					"method_detail");
			if (elms.size() > 0) {
				elms = elms.first().siblingElements();
				String msg = "";
				String className = doc.getElementsByTag("title").text();
				String methodName = "";
				// System.out.println(methodName);
				for (Iterator<Element> it = elms.iterator(); it.hasNext();) {
					Element ele = (Element) it.next();
					int counter = 0;
					if (ele.tagName() == "a") {
						String s = ele.attr("name");
						int diff = s.lastIndexOf(')') - s.indexOf('(');
						methodName = s.substring(0, s.indexOf('(') + 1);
						if (diff != 1) {
							counter = 1;
							for (int i = 0; i < s.length(); i++) {
								if (s.charAt(i) == ',') {
									counter++;
								}
							}
						}
						methodName += counter + ")";
					}
					if (ele.tagName() == "ul") {
						msg = "<html><body> <strong><div style=\"width: 300px; text-justification: justify;\"></strong><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"ref-item\">";
						msg += ele.toString();
						for (Element el : ele.children().first().children()) {
							// if(!el.tagName().equals("h4")){
							// if(el.tagName() != "pre"){
							// msg += el.toString();
							// }
							// else{
							// msg += el.toString();//TODO should the class be
							// added also ?
							// }
							// }
						}
						msg += "</table></div></html></body></html>";
						jdocMap.put(className + methodName, msg);// Currently
																	// the key =
																	// "Classname.methodName(<no. of parameters>)"
																	// , value =
																	// "<documentation>"
					}
					// mat.replaceAll("");
					// msg = msg.replaceAll("img src=\"", "img src=\""
					// + p5Ref.toURI().toURL().toString() + "/");
					// System.out.println(ele.text());
				}
			}
		}
	}
}