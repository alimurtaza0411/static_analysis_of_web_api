package edu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import edu.analysis.ServiceInterfaceAnalysis;

public class Utility {

    public static String TreetoXml(TreeModel model) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();

        Document doc = impl.createDocument(null, null, null);
        Element root = createTree(doc, model, model.getRoot());
        doc.appendChild(root);

        DOMSource domSource = new DOMSource(doc);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sw.toString();
    }

    public static Element createTree(Document doc, TreeModel model, Object node) {
        Element el = doc.createElement(node.toString());
        for (int i = 0; i < model.getChildCount(node); i++) {
            Object child = model.getChild(node, i);
            el.appendChild(createTree(doc, model, child));
        }
        return el;
    }

      
    public static String readFile(String fileName, String keyWord) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            if (keyWord==null) {
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
            } else {
                while (line != null) {
                    if (line.equals(keyWord))
                        return "TRUE";
                    line = br.readLine();
                }
                return "FALSE";
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    public static void writeFile(String fileName, String content, boolean ifAppend) {
        FileOutputStream fop = null;
        File file;

        try {
            file = new File(fileName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            fop = new FileOutputStream(file, ifAppend);

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
    }

    public static HashMap readConfiguration() {
        String fileName = "Configurations/systemParameters.xml";
        HashMap map = new HashMap();
        String heights = readXMLFile("SystemVariables", "Heights", fileName);
        String maxNumberOfParameters = readXMLFile("SystemVariables", "MaxNumberOfParameters", fileName);
        String totalNumberofAcceptablePaths = readXMLFile("SystemVariables", "TotalNumberofAcceptablePaths", fileName);
        String experimentsValueDeviation = readXMLFile("SystemVariables", "ExperimentsValueDeviation", fileName);
        String normalformTransitionKernelVarianceForGroup = readXMLFile("SystemVariables", "NormalformTransitionKernelVarianceForGroup", fileName);
        String normalformTransitionKernelVarianceWithinGroup = readXMLFile("SystemVariables", "NormalformTransitionKernelVarianceWithinGroup", fileName);
        String uniformTransitionKernalVarianceForGroup = readXMLFile("SystemVariables", "UniformTransitionKernalVarianceForGroup", fileName);
        String uniformTransitionKernalVarianceWithinGroup = readXMLFile("SystemVariables", "UniformTransitionKernalVarianceWithinGroup", fileName);
        String totalnubmerOfAttempts = readXMLFile("SystemVariables", "TotalnubmerOfAttempts", fileName);
        String weightMinToll = readXMLFile("SystemVariables", "WeightMinToll", fileName);
        String markovBlanketThreshHold = readXMLFile("SystemVariables", "MarkovBlanketThreshHold", fileName);
        String groupCountingFactor = readXMLFile("SystemVariables", "GroupCountingFactor", fileName);
        String initialGroupProbabilityFactor = readXMLFile("SystemVariables", "InitialGroupProbabilityFactor", fileName);
        String initialParameterProbabilityFactor = readXMLFile("SystemVariables", "InitialParameterProbabilityFactor", fileName);
        String groupGap = readXMLFile("SystemVariables", "GroupGap", fileName);
        String debug = readXMLFile("SystemVariables", "Debug", fileName);
        String searchMethoGenerateExpereimentData = readXMLFile("SystemVariables", "SearchMethoGenerateExpereimentData", fileName);
        String deviationFactor = readXMLFile("SystemVariables", "DeviationFactor", fileName);
        String bruteForce = readXMLFile("SystemVariables", "BruteForce", fileName);
        String logInterval = readXMLFile("SystemVariables", "LogInterval", fileName);
        String realService = readXMLFile("SystemVariables", "RealService", fileName);

        map.put("Heights", heights);
        map.put("MaxNumberOfParameters", maxNumberOfParameters);
        map.put("TotalNumberofAcceptablePaths", totalNumberofAcceptablePaths);
        map.put("ExperimentsValueDeviation", experimentsValueDeviation);
        map.put("NormalformTransitionKernelVarianceForGroup", normalformTransitionKernelVarianceForGroup);
        map.put("NormalformTransitionKernelVarianceWithinGroup", normalformTransitionKernelVarianceWithinGroup);
        map.put("UniformTransitionKernalVarianceForGroup", uniformTransitionKernalVarianceForGroup);
        map.put("UniformTransitionKernalVarianceWithinGroup", uniformTransitionKernalVarianceWithinGroup);
        map.put("TotalnubmerOfAttempts", totalnubmerOfAttempts);
        map.put("WeightMinToll", weightMinToll);
        map.put("MarkovBlanketThreshHold", markovBlanketThreshHold);
        map.put("GroupCountingFactor", groupCountingFactor);
        map.put("InitialGroupProbabilityFactor", initialGroupProbabilityFactor);
        map.put("InitialParameterProbabilityFactor", initialParameterProbabilityFactor);
        map.put("GroupGap", groupGap);
        map.put("Debug", debug);
        map.put("SearchMethoGenerateExpereimentData", searchMethoGenerateExpereimentData);
        map.put("DeviationFactor", deviationFactor);
        map.put("BruteForce", bruteForce);
        map.put("LogInterval", logInterval);
        map.put("RealService", realService);

        return map;
    }

    public static String readXMLFile(String serviceName, String attribute, String fileName) {
        String result = null;
        if (serviceName != null) {
            try {
                //File fXmlFile = new File("Track/sampleValues.xml");
                File fXmlFile = new File(fileName);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);

               
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName(serviceName);
                if (nList.getLength() > 0) {
                    Node nNode = nList.item(0);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (eElement.getElementsByTagName(attribute).getLength() > 0) {
                            result = eElement.getElementsByTagName(attribute).item(0).getTextContent();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    public List<String> traverseTree(DefaultMutableTreeNode node, List<String> allParameters) {
        int childCount = node.getChildCount();
        //List<String> allParameters = new ArrayList<>();
        allParameters.add(node.toString());
        //System.out.println("---" + node.toString() + "---");

        for (int i = 0; i < childCount; i++) {

            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            if (childNode.getChildCount() > 0) {
                traverseTree(childNode, allParameters);
            } else {
                allParameters.add(childNode.toString());
                //allParameters = result + childNode.toString()+",";
            }
        }
        allParameters.add(node.toString());
        return allParameters;
    }

    public static List<String> traverseTreeGetPair(DefaultMutableTreeNode node, List<String> allParameters) {
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);

            if (childNode.getParent() != null && childNode.getParent() != childNode && childNode.getChildCount() != 0) {
                String parentString = childNode.getParent().toString();
                String childString = childNode.toString();
                if (parentString != null) {
                    String nonBOParent = null;
                    String nonBO2Child = null;
                    nonBOParent = readXMLFile("OpenShipping", parentString, "Configurations/NonBOs.xml");
                    nonBO2Child = readXMLFile("OpenShipping", childString, "Configurations/NonBOs.xml");
                    if (nonBOParent == null && nonBO2Child == null) {
                        String meaningfulNameParent = readXMLFile("OpenShip", parentString, "Configurations/PredefinedBOs.xml");
                        String meaningfulNameChild = readXMLFile("OpenShip", childString, "Configurations/PredefinedBOs.xml");
                        

                        if (meaningfulNameParent == null) {
                            meaningfulNameParent = parentString;
                        }
                        if (meaningfulNameChild == null) {
                            meaningfulNameChild = childString;
                        }

                        String pairString = meaningfulNameParent + " -> " + meaningfulNameChild + ";";
                        allParameters.add(pairString);
                    }
                }
            }
            if (childNode.getChildCount() > 0) {
                traverseTreeGetPair(childNode, allParameters);
            }
        }
        String parentString = node.toString();
        String nonBOParent = readXMLFile("OpenShip", parentString, "Configurations/NonBOs.xml");
        if (nonBOParent == null) {
            String meaningfulNameParent = readXMLFile("OpenShip", parentString, "Configurations/PredefinedBOs.xml");
            if (meaningfulNameParent == null) {
                meaningfulNameParent = parentString;
            }
            allParameters.add(meaningfulNameParent);
        }
        return allParameters;
    }



    public static void main(String arg[]) {

        HashMap map = new HashMap();
        map.put(21, "Twenty One");
        map.put(21, "Twenty two");
        map.put("31", "Thirty One");
        map.put(31, 234.5);

        Iterator<Integer> keySetIterator = map.keySet().iterator();
        while (keySetIterator.hasNext()) {
            Integer key = keySetIterator.next();
            System.out.println("key: " + key + " value: " + map.get(key));
        }

        Double value = (Double) map.get(31);
        if (value > 200) {
            System.out.println("key: 21" + value);
        }

    }


    /* Get a list of unprocessed files */
    public static ArrayList<File> getUnprocessedFile(String filePath, String ext) {
        File dir = new File(filePath);
        FileFilter fileFilter = new WildcardFileFilter("*." + ext);
        File[] files = dir.listFiles(fileFilter);
        ArrayList<File> ｕnprocessedFiles = new ArrayList<File>();
        
        if (files.length > 0) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);            
            for (File file: files){
                String processedFiles = "";                
                try {
                    processedFiles = Utility.readFile("output/processedWSDL.txt", file.getName());
                } catch (IOException ex) {
                }                
                if (processedFiles.equals("FALSE")) {
                    ｕnprocessedFiles.add(file);
                } else {  //exit the loop when hiting the first processed? 
                    break;
                }
            }
        }
        return ｕnprocessedFiles;
    }
}
