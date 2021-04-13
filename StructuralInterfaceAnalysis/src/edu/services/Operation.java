package edu.services;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.IOException;
import edu.entities.EntityPair;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.entities.BusinessEntity;
import static edu.Utility.readFile;
import static edu.Utility.writeFile;
import edu.analysis.Combination;
import edu.analysis.ValidCombination;

public class Operation {

    public Operation(String name) {
        this.name = name;
        //this.entities = new ArrayList<BusinessEntity>();
        this.serviceBEDataModel = new ServiceBEDataModel();
        this.outputServiceBEDataModel = new ServiceBEDataModel();
    }

    //public ArrayList<BusinessEntity> getEntities() {
    //    return entities;
    //}
    private ServiceBEDataModel serviceBEDataModel; //the BEDataMOdel inherent in inputs
    private ServiceBEDataModel outputServiceBEDataModel; //the BEDataMOdel inherent in outputs
    private int numberOfInputParameters;
    private ArrayList<String> inputParameterString;
    private ArrayList<String> outputParameterString;
    private ArrayList<Parameter> compulsoryInputParameterList;
    private ArrayList<Parameter> simpleInputParameterList;
    private ValidCombination accecptedParameterSets;
    //private ArrayList<ArrayList<Parameter>> groups;
    private ArrayList<Group> groups;
    

    public ArrayList<Group> getGroups() {
        if (groups==null)
            formGroups();
        return groups;
    }

    private void formGroups() {
        if (groups== null);
            groups = new ArrayList<Group>();
            
        if (this.simpleInputParameterList != null && this.simpleInputParameterList.size() > 0) {
            Parameter previousParent = this.getSimpleInputParameterList().get(0).getParentParameter();
            int counter = 0;
            int groupNumber = 1;            
            Group group = new Group(groupNumber);
            
            for (Parameter parameter : this.simpleInputParameterList) {
                counter++;
                Parameter currentParent = parameter.getParentParameter();
                if (currentParent.equals(previousParent)) {
                    group.addParameter(parameter);
                    parameter.setGroupBelongTo(group);
                    if (this.simpleInputParameterList.size()== counter && group.getParameters()!= null && group.getParameters().size()>0)  // the last one
                        groups.add(group);
                } else {  // a new group
                    if (group.getParameters() != null && group.getParameters().size() > 0) {
                        groups.add(group);
                        groupNumber++;
                        group = new Group(groupNumber);
                        parameter.setGroupBelongTo(group);
                        group.addParameter(parameter);
                    }
                }
                previousParent = currentParent;
            }
        }
    }

    public void setLevelsAndChildren() {
        for (Parameter temParameter : this.complexInputParameterList) {
            if (temParameter.getParameterUniqueIDinTree() == 1) {
                temParameter.setLevel(1);
            } else {
                temParameter.setLevel(temParameter.getParentParameter().getLevel() + 1);
            }
            for (Parameter parameter : this.complexInputParameterList) {
                if (parameter.getParentParameter().equals(temParameter)) {
                    temParameter.addChild(parameter);
                }
            }
        }
        for (Parameter parameter : this.simpleInputParameterList) {
            //parameter.setLevel();
            Parameter parent = parameter.getParentParameter();
            for (Parameter temParameter : this.complexInputParameterList) {
                if (temParameter.equals(parent)) {
                    temParameter.addChild(parameter);
                }
            }
        }
    }

    public ValidCombination getAccecptedParameterSets() {
        return accecptedParameterSets;
    }

    public ArrayList<Parameter> getKnownPath(String fileName) {
        //HashMap<int, String> result = new HashMap<int, String>();
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();

        //ArrayList<Parameter> result = new ArrayList<Parameter>();
        try {
            //File file = new File("ValidCombinations/track_knownPath.xml"); 
            File file = null;
            if (fileName == null) {
                file = new File("ValidCombinations/" + this.name + "_knownPath.xml");
            } else {
                file = new File("ValidCombinations/" + this.name + "/" + fileName);
            }

            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            //System.out.println("LEVEL 0: Root element :" + doc.getDocumentElement().getNodeName());
            if (doc.hasChildNodes()) {
                Parameter rootParameter = new Parameter();
                rootParameter.setName(doc.getChildNodes().item(0).getNodeName());
                //parameters.add(rootParameter);
                parameters = getAcceptedParameters(doc.getChildNodes().item(0).getChildNodes(), rootParameter, parameters);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        /*
         if (parameters != null) {
         for (Parameter parameter : parameters) {
         if (parameter.getParentParameter() != null) {
         System.out.println("Parameter: " + parameter.getName() + " its Parent: " + parameter.getParentParameter().getName());
         } else {
         System.out.println("Parameter: " + parameter.getName() + " its Parent: root");
         }
         }
         }
         */
        return parameters;
        /*
         if (parameters != null) {
         for (Parameter knownParameter : parameters) {
         for (Parameter parameter : this.simpleInputParameterList) {
         if ((parameter.getName().equals(knownParameter.getName()))
         && ((parameter.getParentParameter() == null && knownParameter.getParentParameter() == null)
         || (parameter.getParentParameter() != null && knownParameter.getParentParameter() != null
         //&& parameter.getParentParameter().getName().equals(knownParameter.getParentParameter().getName())))) {
         && parameter.getParentParameter().getParameterUniqueIDinTree()== knownParameter.getParentParameter().getParameterUniqueIDinTree()))) {
         result.add(parameter);
         break;
         }
         }
         }
         }
         */
        //for(Parameter parameter: parameters)
        //   if (parameter.getParentParameter()!=null)
        //       System.out.println("Parameter: "+ parameter.getName()+ " its Parent: "+ parameter.getParentParameter().getName());
        //   else System.out.println("Parameter: "+ parameter.getName()+ " its Parent: root");        

        //return result;
        //HashMap result = new HashMap();
    }

    private ArrayList<Parameter> getAcceptedParameters(NodeList nodeList, Parameter parentParameter, ArrayList<Parameter> parameters) {
        //System.out.println("the length is: "+ nodeList.getLength());
        //parentParameter = parameter;
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            Parameter parameter = null;
            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                // get node name and value
                //System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
                //Element eElement = (Element) tempNode;
                //System.out.println("\nNode value =" + eElement.getElementsByTagName(tempNode.getNodeName()).item(0).getTextContent() + " [OPEN]");                
                //create a parameter for it

                parameter = new Parameter();
                parameter.setName(tempNode.getNodeName());
                if (parentParameter != null) {
                    parameter.setParentParameter(parentParameter); //parentParameter should have unique id in tree
                    //do from here tomorrow
                }

                //if (tempNode.getNodeName().equals("AccountNumber"))
                //    System.out.println("dddd");
                if (!tempNode.hasChildNodes() || (tempNode.hasChildNodes() && tempNode.getChildNodes().getLength() == 1
                        && tempNode.getChildNodes().item(0).getNodeType() != Node.ELEMENT_NODE)) {
                    for (Parameter tempParameter : this.getSimpleInputParameterList()) {
                        if (parameter.getName().equals(tempParameter.getName())) {
                            if (parameter.getParentParameter() == null && tempParameter.getParentParameter() == null) {
                                //tempParameter.setSimpleIndex(tempParameter.getSimpleIndex()+1);
                                if (parameters.isEmpty() || 
                                        (!parameters.isEmpty() && parameters.get(parameters.size()-1).getSimpleIndex() != tempParameter.getSimpleIndex())) {
                                    //avoid the repeted parameter for now.
                                    parameters.add(tempParameter);
                                    break;
                                }
                            } else {
                                Parameter parent = parameter.getParentParameter();
                                Parameter parent1 = tempParameter.getParentParameter();
                                String same = null;
                                while (parent != null && parent1 != null) {
                                    if (!parent1.getName().equals(parent.getName())) {
                                        same = "NO";
                                        break;
                                    } else {
                                        same = "YES";
                                    }
                                    parent1 = parent1.getParentParameter();
                                    parent = parent.getParentParameter();
                                }
                                if (same.equals("YES")) {
                                    //tempParameter.setSimpleIndex(tempParameter.getSimpleIndex()+1);
                                        if (parameters.isEmpty() || 
                                            (!parameters.isEmpty() && parameters.get(parameters.size()-1).getSimpleIndex() != tempParameter.getSimpleIndex())) {
                                        parameters.add(tempParameter);
                                        break;
                                    }
                                }
                            }

                            /*
                             else if (parameter.getParentParameter().getName().equals(tempParameter.getParentParameter().getName())) {
                             if (parameter.getParentParameter().getParentParameter()==null && tempParameter.getParentParameter().getParentParameter()==null) {
                             parameters.add(tempParameter);
                             break;
                             } else if (parameter.getParentParameter().getParentParameter().getName().
                             equals(tempParameter.getParentParameter().getParentParameter().getName())) {
                             parameters.add(tempParameter);
                             break;
                             }
                             }
                             */
                        }
                    }
                }
                /*
                 if (tempNode.hasAttributes()) {
                 // get attributes names and values
                 NamedNodeMap nodeMap = tempNode.getAttributes();
 
                 for (int i = 0; i < nodeMap.getLength(); i++) { 
                 Node node = nodeMap.item(i);
                 System.out.println("attr name : " + node.getNodeName());
                 System.out.println("attr value : " + node.getNodeValue());
                 }
 
                 }
                 */
                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes
                    parameters = getAcceptedParameters(tempNode.getChildNodes(), parameter, parameters);
                }
                //System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
            }
        }

        return parameters;
    }

    public void setAccecptedParameterSets(Combination combination) {
        ValidCombination validCombination = this.getAccecptedParameterSets();
        if (validCombination != null) {
            boolean contained = false;
            for (Combination eachCombination : validCombination.getCombinations()) {
                if (eachCombination.getParameterSet().containsAll(combination.getParameterSet())) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                validCombination.getCombinations().add(combination);
                this.accecptedParameterSets = validCombination;
            }
        } else {
            validCombination = new ValidCombination();
            validCombination.getCombinations().add(combination);
            this.accecptedParameterSets = validCombination;
        }
    }

    public void setSimpleInputParameterList(ArrayList<Parameter> simpleInputParameterList) {
        this.simpleInputParameterList = simpleInputParameterList;
    }

    public void setComplexInputParameterList(ArrayList<Parameter> complexInputParameterList) {
        this.complexInputParameterList = complexInputParameterList;
    }
    private ArrayList<Parameter> complexInputParameterList;

    public ArrayList<Parameter> getSimpleInputParameterList() {
        return simpleInputParameterList;
    }

    public ArrayList<Parameter> getComplexInputParameterList() {
        return complexInputParameterList;
    }

    public void setCompulsoryInputParameterList(ArrayList<Parameter> compulsoryInputParameterList) {
        this.compulsoryInputParameterList = compulsoryInputParameterList;
    }

    /*
     public ArrayList<Parameter> getCompulsoryInputParameterList() {
     for (Parameter parameter : this.getInputParameters()) {
     DefaultMutableTreeNode node = parameter.getRoot();  //this is the root
     int childCount = node.getChildCount();
     for (int i = 0; i < childCount; i++) {
     DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);

     }
     return compulsoryInputParameterList;
     }
     }
     */
    /*
     public void saveTheValidCombination(ArrayList<Parameter> combination) {
     XStream xstream =  new XStream(new DomDriver());
     if (this.accecptedParameterSets == null) {
     ValidCombination validCombination = new ValidCombination();
     validCombination.getCombinations().add(combination);
     this.accecptedParameterSets = validCombination;

     String xmlString = xstream.toXML(validCombination);
     writeFile("Serialisation/" + this.getName() + ".xml", xmlString, false);
     } else {
     String xmlContent = null;
     try {
     xmlContent = readFile("Serialisation/" + this.getName() + ".xml");
     } catch (IOException ex) {
     Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
     }
     if (xmlContent != null) {         
     ValidCombination validCombination = null;
     if (xmlContent != null) {
     validCombination = (ValidCombination) xstream.fromXML(xmlContent);
     }
     boolean contained =false;
     if (validCombination !=null)
     for (ArrayList<Parameter> parameterList: validCombination.getCombinations()) {
     if (parameterList.containsAll(combination)) {
     contained = true;
     break;
     }
     }
     if (!contained) {
     validCombination.getCombinations().add(combination);
     String xmlString = xstream.toXML(validCombination);
     writeFile("Serialisation/" + this.getName() + ".xml", xmlString, true);
     }
     }
     }
     }
     */
    public void saveTheValidCombination() {
        if (this.accecptedParameterSets != null) {
            XStream xstream = new XStream(new DomDriver());
            ValidCombination validCombination = this.accecptedParameterSets;
            String xmlString = xstream.toXML(validCombination);
            writeFile("ValidCombinations/" + this.getName() + "_validCombination.xml", xmlString, false);
        }
    }

    /*
    public ValidCombination retrieveValidCombination() {
        if (this.accecptedParameterSets != null) {
            return this.accecptedParameterSets;
        }
        ValidCombination validCombination = null;
        String xmlContent = null;
        try {
            xmlContent = readFile("ValidCombinations/" + this.getName() + "_validCombination.xml");
        } catch (IOException ex) {
            Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (xmlContent != null) {
            XStream xstream = new XStream(new DomDriver());
            validCombination = (ValidCombination) xstream.fromXML(xmlContent);
        }
        return validCombination;
    }
    */

    public ArrayList<Parameter> getCompulsoryInputParameterList() {
        return compulsoryInputParameterList;
    }

    public ArrayList<Parameter> getOptionalInputParameterList() {
        return optionalInputParameterList;
    }

    public void setOptionalInputParameterList(ArrayList<Parameter> optionalInputParameterList) {
        this.optionalInputParameterList = optionalInputParameterList;
    }
    private ArrayList<Parameter> optionalInputParameterList;

    public ArrayList<String> getOutputParameterString() {
        return outputParameterString;
    }

    public ArrayList<String> getInputParameterString() {
        return inputParameterString;
    }

    public String getInputParametersWithMockValues() {
        return inputParametersWithMockValues;
    }

    public void setInputParameterString(ArrayList<String> inputParameterString) {
        this.inputParameterString = inputParameterString;
    }

    public void setOutputParameterString(ArrayList<String> outputParameterString) {
        this.outputParameterString = outputParameterString;
    }

    public int getNumberOfInputParameters() {
        return numberOfInputParameters;
    }

    public int getNumberOfOutputParameters() {
        return numberOfOutputParameters;
    }
    private int numberOfOutputParameters;

    public void setNumberOfInputParameters(int numberOfInputParameters) {
        this.numberOfInputParameters = numberOfInputParameters;
    }

    public void setNumberOfOutputParameters(int numberOfOutputParameters) {
        this.numberOfOutputParameters = numberOfOutputParameters;
    }

    public ServiceBEDataModel getOutputServiceBEDataModel() {
        return outputServiceBEDataModel;
    }

    public ServiceBEDataModel getServiceBEDataModel() {
        return serviceBEDataModel;
    }
    private String name;

    public String getName() {
        return name;
    }
    private String inputParametersWithMockValues;
    private String outPutParametersWithMockValues;

    public void setOutPutParametersWithMockValues(String outPutParametersWithMockValues) {
        this.outPutParametersWithMockValues = outPutParametersWithMockValues;
    }
    private ArrayList<Parameter> outputParameters;
    private ArrayList<Parameter> inputParameters;
    //private ArrayList<BusinessEntity> entities; //entities that are manipulated by the operation in inputs
    //private ArrayList<BusinessEntity> outputEntities; //entities that are manipulated by the operation in outputs

    public ArrayList<BusinessEntity> getNestedEntities(BusinessEntity entity) {
        ArrayList<BusinessEntity> entities = new ArrayList<BusinessEntity>();
        for (EntityPair entityPair : this.getServiceBEDataModel().getNestingPair()) {
            if (entityPair.getMainEntity().equals(entity)) {
                entities.add(entityPair.getSlaveEntity());
            }
        }
        return entities;
    }

    /*    
     public int getNumberofInputParameters() {
     for (Parameter parameter : this.getInputParameters()) {
     //thingsToPrint = thingsToPrint+ operation.getName()+"\n";
     DefaultMutableTreeNode root = parameter.getRoot();

     int childCount = node.getChildCount();
     if (node.getChildCount() > 0) {
     for (int i = 0; i < childCount; i++) {
     DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
     String parameterName = childNode.toString();
     Parameter tempParameter = new Parameter();
     tempParameter.setRoot(childNode);
     String[] parts = parameterName.split(":");
     if (parameterName.contains("COMPLEX")) {
     tempParameter.setComplex(true);
     tempParameter.setRoot(childNode);
     }
     tempParameter.setName(parts[0]); //must be the name                
     if (parts.length == 3) {
     tempParameter.setType(parts[1]);
     }
     //BusinessEntity newEntity =null;
     if (tempParameter.isComplex()) {
     identifyBEandRelation(service, operation, newEntity, tempParameter, intputOrOutput);
     }
     // add to set and etc.
     //allParameters.add(childNode.toString());
     //allParameters = result + childNode.toString()+",";
     }
     }

     }
     }
    
     */
    public ArrayList<Parameter> getInputParameters() {
        return inputParameters;
    }

    public ArrayList<Parameter> getOutputParameters() {
        return outputParameters;
    }

    public void setInputParametersWithMockValues(String inputParametersWithMockValues) {
        this.inputParametersWithMockValues = inputParametersWithMockValues;
    }

    public void setInputParameters(ArrayList<Parameter> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public void setOutputParameters(ArrayList<Parameter> outputParameters) {
        this.outputParameters = outputParameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof Operation) {
            sameSame = this.name.equals(((Operation) object).getName());
        }
        return sameSame;
    }

}
