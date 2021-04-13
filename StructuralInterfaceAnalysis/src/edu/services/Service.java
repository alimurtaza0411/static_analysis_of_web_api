
package edu.services;

import com.eviware.soapui.SoapUI;
import qut.edu.au.entities.EntityPair;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.components.ResponseXmlDocument;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.editor.EditorDocument;
import com.eviware.soapui.support.xml.XmlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.wsdl.BindingOperation;
import org.apache.log4j.Priority;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import BusinessEntity;
import edu.Utility;
import static edu.Utility.writeFile;
import edu.analysis.AnalysisStats;
import edu.analysis.ParameterAnalysis;
import edu.entities.Attribute;


public class Service {

    private String serviceWSDLName;
    private ArrayList<Operation> operations;
    private String serviceName;
    private ServiceBEDataModel serviceBEDataModel; //the BEDataMOdel inherent in inputs

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Service.class);

    public void outPutOperations(String fileID) {
        String outfileName = "output/" + serviceName + "-" + fileID + ".operations";
        String thingsToPrint = "";
        for (Operation operation : this.getOperations()) {
            thingsToPrint = thingsToPrint + operation.getName() + "\n";
      }
        writeFile(outfileName, thingsToPrint, false);
    }

    public void outPutStatistics(long structuralTimeTaken, long behaviouralTimeTaken, boolean includeBehaviouir) {
        String outfileName = "output/" + serviceName + ".stats";
        String thingsToPrint = "The strucual statistics of " + this.serviceName + ":\n";
        thingsToPrint = thingsToPrint + "The overal time taken : " + structuralTimeTaken + "seconds and the average time is "
                + structuralTimeTaken / this.getOperations().size() + "\n";
        thingsToPrint = thingsToPrint + "The number of operations:  " + this.getOperations().size() + "\n";
        thingsToPrint = thingsToPrint + "The operations are :  \n";
        double averageNumberOfInputParameters = 0;
        double averageNumberOfOutputParameters = 0;
        int nubmerOfOperations = this.getOperations().size();
        for (Operation operation : this.getOperations()) {
            thingsToPrint = thingsToPrint + "The statistics of Operation " + operation.getName() + " are: \n";
            thingsToPrint = thingsToPrint + "Number of Input Parameters: " + operation.getNumberOfInputParameters() + "\n";
            thingsToPrint = thingsToPrint + "Number of Output Parameters: " + operation.getNumberOfOutputParameters() + "\n";
            thingsToPrint = thingsToPrint + "Input parameters are : ( " + operation.getInputParameterString() + "\n";
            thingsToPrint = thingsToPrint + "Output parameters are : ( " + operation.getOutputParameterString() + "\n";

            averageNumberOfInputParameters = averageNumberOfInputParameters + operation.getNumberOfInputParameters();

            averageNumberOfOutputParameters = averageNumberOfOutputParameters + operation.getNumberOfOutputParameters();
        }

        //System.out.println((int) Math.ceil(a / 100.0));        
        averageNumberOfInputParameters = averageNumberOfInputParameters / nubmerOfOperations;
        averageNumberOfOutputParameters = averageNumberOfOutputParameters / nubmerOfOperations;
        thingsToPrint = thingsToPrint + "The average number of input parameters is: " + (int) Math.ceil(averageNumberOfInputParameters) + "\n";
        thingsToPrint = thingsToPrint + "The average number of output parameters is: " + (int) Math.ceil(averageNumberOfOutputParameters) + "\n";

        ServiceBEDataModel be = this.getServiceBEDataModel();
        double size = be.getEntities().size();
        thingsToPrint = thingsToPrint + "The average number of entities :" + (int) Math.ceil(size / nubmerOfOperations) + "\n";
        size = be.getNestingPair().size();
        thingsToPrint = thingsToPrint + "The average number of nesting pairs :" + (int) Math.ceil(size / nubmerOfOperations) + "\n";
        size = be.getExclusiveContainmentPair().size();
        thingsToPrint = thingsToPrint + "The average number of mandatory strong containment pairs :" + (int) Math.ceil(size / nubmerOfOperations) + "\n";
        size = be.getWeakInclusiveContainmentPair().size();
        thingsToPrint = thingsToPrint + "The average number of optional strong containment pairs :" + (int) Math.ceil(size / nubmerOfOperations) + "\n";
        size = be.getStrongInclusiveContainmentPair().size();
        thingsToPrint = thingsToPrint + "The average number of weak containment pairs :" + (int) Math.ceil(size / nubmerOfOperations) + "\n";
        size = be.getAssociationPair().size();
        thingsToPrint = thingsToPrint + "The average number of association pairs :" + (int) Math.ceil(size / nubmerOfOperations) + "\n";

        writeFile(outfileName, thingsToPrint, false);
        //writeFile(outfileName, entityString, ifAppend);
        this.getServiceBEDataModel().outPutStatistics(outfileName, true);
    }

    private void generateServiceBEDataModel() {
        for (Operation operation : this.operations) {
            ServiceBEDataModel beDataModel = operation.getServiceBEDataModel();
            ArrayList<BusinessEntity> businessEntities = beDataModel.getEntities();
            ArrayList<String> demoBusinessEntities = new ArrayList<String>();
            demoBusinessEntities.add("OpenshipOrder");
            demoBusinessEntities.add("PackageLineItem");
            demoBusinessEntities.add("SpecialService");
            demoBusinessEntities.add("DangerousGood");
            demoBusinessEntities.add("PriorityAlert");
            demoBusinessEntities.add("Consolidation");
            demoBusinessEntities.add("ShipOrder");
            demoBusinessEntities.add("Shipper");
            demoBusinessEntities.add("Shipment");
            demoBusinessEntities.add("Shipment");
            demoBusinessEntities.add("Recipient");
            demoBusinessEntities.add("ShippingLabel");
            demoBusinessEntities.add("Pickup");
            demoBusinessEntities.add("CustomsClearance");
            demoBusinessEntities.add("Payment");
            demoBusinessEntities.add("PendingShipment");
            demoBusinessEntities.add("Payor");
            

            // if the service'BE model has the entity already, then skip it;
            for (BusinessEntity eachEntity : businessEntities) {
                if (!this.serviceBEDataModel.getEntities().contains(eachEntity)) {
                    //this if statement is temporary
                    //if (demoBusinessEntities.contains(eachEntity.getName())) 
                    {
                        //TBD there is a bug here, we should merge all entities that have the same name, rather than skip it
                        this.serviceBEDataModel.addEntity(eachEntity);
                    }
                }
            }

            for (EntityPair entityPair : beDataModel.getNestingPair()) {
                
                if (!this.serviceBEDataModel.getNestingPair().contains(entityPair)) {
                    //this if statement is temporary
                    BusinessEntity mainEntity =  entityPair.getMainEntity();
                    BusinessEntity slaveEntity =  entityPair.getSlaveEntity();
                    //if (demoBusinessEntities.contains(mainEntity.getName()) && demoBusinessEntities.contains(slaveEntity.getName()))
                    {
                        this.serviceBEDataModel.addNestingPair(entityPair);
                    }
                    
                }
            }
            for (EntityPair entityPair : beDataModel.getExclusiveContainmentPair()) {
                if (!this.serviceBEDataModel.getExclusiveContainmentPair().contains(entityPair)) {
                    //this if statement is temporary
                    BusinessEntity mainEntity =  entityPair.getMainEntity();
                    BusinessEntity slaveEntity =  entityPair.getSlaveEntity();                    
                    //if (demoBusinessEntities.contains(mainEntity.getName()) && demoBusinessEntities.contains(slaveEntity.getName()))
                    {                        
                        this.serviceBEDataModel.addExclusiveContainmentPair(entityPair);
                    }
                    
                }
            }
            
            for (EntityPair entityPair : beDataModel.getOptionalExclusiveContainmentPair()) {
                if (!this.serviceBEDataModel.getOptionalExclusiveContainmentPair().contains(entityPair)) {
                    //this if statement is temporary
                    BusinessEntity mainEntity =  entityPair.getMainEntity();
                    BusinessEntity slaveEntity =  entityPair.getSlaveEntity();                    
                    //if (demoBusinessEntities.contains(mainEntity.getName()) && demoBusinessEntities.contains(slaveEntity.getName()))
                    {                        
                        this.serviceBEDataModel.addOptionalExclusiveContainmentPair(entityPair);
                    }
                    
                }
            }
            
            for (EntityPair entityPair : beDataModel.getWeakInclusiveContainmentPair()) {
                if (!this.serviceBEDataModel.getWeakInclusiveContainmentPair().contains(entityPair)) {
                    //this if statement is temporary
                    BusinessEntity mainEntity =  entityPair.getMainEntity();
                    BusinessEntity slaveEntity =  entityPair.getSlaveEntity();                    
                    //if (demoBusinessEntities.contains(mainEntity.getName()) && demoBusinessEntities.contains(slaveEntity.getName()))
                    {                                            
                        this.serviceBEDataModel.addWeakInclusiveContainmentPair(entityPair);
                    }
                }
            }

            for (EntityPair entityPair : beDataModel.getStrongInclusiveContainmentPair()) {
                if (!this.serviceBEDataModel.getStrongInclusiveContainmentPair().contains(entityPair)) {
                    //this if statement is temporary
                    BusinessEntity mainEntity =  entityPair.getMainEntity();
                    BusinessEntity slaveEntity =  entityPair.getSlaveEntity();                    
                    //if (demoBusinessEntities.contains(mainEntity.getName()) && demoBusinessEntities.contains(slaveEntity.getName()))
                    {                                            
                        this.serviceBEDataModel.addStrongInclusiveContainmentPair(entityPair);
                    }
                }
            }
            for (EntityPair entityPair : beDataModel.getAssociationPair()) {
                if (!this.serviceBEDataModel.getAssociationPair().contains(entityPair)) {
                    //this if statement is temporary
                    BusinessEntity mainEntity =  entityPair.getMainEntity();
                    BusinessEntity slaveEntity =  entityPair.getSlaveEntity();                    
                    //if (demoBusinessEntities.contains(mainEntity.getName()) && demoBusinessEntities.contains(slaveEntity.getName()))
                    {                        
                        this.serviceBEDataModel.addAssociationPair(entityPair);                        
                    }
                }
            }
        }
        for (int i = 0; i < this.serviceBEDataModel.getStrongInclusiveContainmentPair().size(); i++) {
            if (this.serviceBEDataModel.getExclusiveContainmentPair().contains(this.serviceBEDataModel.getStrongInclusiveContainmentPair().get(i))
                    || this.serviceBEDataModel.getWeakInclusiveContainmentPair().contains(this.serviceBEDataModel.getStrongInclusiveContainmentPair().get(i))) {
                this.serviceBEDataModel.getStrongInclusiveContainmentPair().remove(i);
            }
        }
    }

    public void visualiseBEModel() {
        this.getServiceBEDataModel().visualise(this.serviceName);
    }

    public void outputOperationsToXML() {
        XStream xstream = new XStream();
        String fileName = "output/" + this.serviceName + "_Operations.xml";
        for (Operation operation : this.getOperations()) {
            if (operation.getName().equals("track")) {
                String serviceString = xstream.toXML(operation.getCompulsoryInputParameterList());
                Utility.writeFile(fileName, serviceString, true);
            }
        }
    }

    public void outBEtoJson() {
        //String fileName = "output/json/" + this.serviceName+"/";

        String fileName = "output/" + this.serviceName + ".json";

        ObjectMapper mapper = new ObjectMapper();
        try {
            //fileName = fileName +entity.getName()+".json";
            ServiceBEDataModel model = this.getServiceBEDataModel();
            model.sortBEDataModel();
            mapper.writeValue(new File(fileName), model);
        } catch (IOException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
        // }

    }

    public void outputBEModelToXML() throws FileNotFoundException {
        XStream xstream = new XStream();
        String fileName = "output/" + this.serviceName;

        String serviceString = xstream.toXML(this.getServiceBEDataModel());
        Utility.writeFile(fileName + "_BEModel.xml", serviceString, false);

        JsonArrayBuilder entitiesBuilder = Json.createArrayBuilder();
        JsonObjectBuilder finalBuilder = Json.createObjectBuilder();

        for (BusinessEntity entity : this.getServiceBEDataModel().getEntities()) {

            JsonObjectBuilder entityBuilder = Json.createObjectBuilder();
            entityBuilder = entityBuilder.add("name", entity.getName());

            JsonObjectBuilder attributeBuilder = Json.createObjectBuilder();
            int counter = 0;
            for (Attribute attribute : entity.getAttributes()) {
                JsonObjectBuilder eachAttributeBuilder = Json.createObjectBuilder();
                eachAttributeBuilder = eachAttributeBuilder.add("name", attribute.getName());
                eachAttributeBuilder = eachAttributeBuilder.add("type", attribute.getType());
                counter++;
                attributeBuilder.add("attriubte" + counter, eachAttributeBuilder);
            }
            entityBuilder = entityBuilder.add("attriubtes", attributeBuilder);

            JsonObjectBuilder childrenBuilder = Json.createObjectBuilder();

            for (EntityPair pair : this.getServiceBEDataModel().getNestingPair()) {
                counter = 0;
                if (pair.getMainEntity().equals(entity)) {
                    BusinessEntity child = pair.getSlaveEntity();
                    counter++;
                    JsonObjectBuilder childBuilder = Json.createObjectBuilder();
                    childBuilder = childBuilder.add("name", child.getName());
                    childBuilder = childBuilder.add("type", child.getType());
                    childrenBuilder.add("child" + counter, childBuilder);
                }
            }
            entityBuilder = entityBuilder.add("children", childrenBuilder);
            entitiesBuilder.add(entityBuilder);

        }
        finalBuilder.add("", entitiesBuilder);

        JsonObject empJsonObject = finalBuilder.build();
        OutputStream os = new FileOutputStream(fileName + ".json");
        JsonWriter jsonWriter = Json.createWriter(os);
        jsonWriter.writeObject(empJsonObject);
        jsonWriter.close();

        String serviceString2 = xstream.toXML(this.getServiceBEDataModel().getEntities());
        Utility.writeFile(fileName + "_Entities.xml", serviceString2, false);
        String serviceString3 = xstream.toXML(this.getServiceBEDataModel().getNestingPair());
        Utility.writeFile(fileName + "_NestingPairs.xml", serviceString3, false);
        String serviceString4 = xstream.toXML(this.getServiceBEDataModel().getExclusiveContainmentPair());
        Utility.writeFile(fileName + "_MandatoryStrongDependencePairs.xml", serviceString4, false);
        String serviceString5 = xstream.toXML(this.getServiceBEDataModel().getWeakInclusiveContainmentPair());
        Utility.writeFile(fileName + "_OptionalStrongDependencePairs.xml", serviceString5, false);
        String serviceString6 = xstream.toXML(this.getServiceBEDataModel().getStrongInclusiveContainmentPair());
        Utility.writeFile(fileName + "_WeakDependencePairs.xml", serviceString6, false);
        String serviceString7 = xstream.toXML(this.getServiceBEDataModel().getAssociationPair());
        Utility.writeFile(fileName + "_AssociationPairs.xml", serviceString7, false);
    }

    public void visualiseAllOperationsBEModel() {
        for (Operation operation : this.operations) {
            ServiceBEDataModel beDataModel = operation.getServiceBEDataModel();
            beDataModel.visualise(this.serviceName + "-" + operation.getName());
        }
    }

    public ArrayList<BusinessEntity> getDominaters(BusinessEntity entity) {

        ArrayList<Operation> operationsThatManipulateEntity = this.getOperationsThatManipulateEntity(entity);
        ArrayList<BusinessEntity> dominaters = new ArrayList<BusinessEntity>();

        for (Operation operationThatManipulatesEntity : operationsThatManipulateEntity) {
            ArrayList<BusinessEntity> entities2 = operationThatManipulatesEntity.getServiceBEDataModel().getEntities();; //TBD, we need to check output as well.
            if (entities2 != null) {
                for (BusinessEntity entity2 : entities2) {
                    if (this.ifDomination(entity2, entity)) {
                        dominaters.add(entity2);
                    }
                }
            }
        }
        return dominaters;
    }
    public ArrayList<Operation> getOperationsThatManipulateEntity(BusinessEntity entity) {
        ArrayList<Operation> operations = new ArrayList<Operation>();
        for (Operation operation : this.operations) {
            ArrayList<BusinessEntity> entities = operation.getServiceBEDataModel().getEntities();
            if (entities.contains(entity)) {
                operations.add(operation);
            }
        }
        return operations;
    }
     public boolean ifMandatoryStrongDomination(BusinessEntity entity1, BusinessEntity entity2) {
        boolean strongDependence = false;
        for (Operation operation : this.operations) {
            ArrayList<BusinessEntity> entities = operation.getServiceBEDataModel().getEntities();
            if (!entities.isEmpty()) {
                if (entities.contains(entity1)) {
                    if (!entities.contains(entity2)) {
                        //strongDependence = false;
                        return false;
                    } else {
                        strongDependence = true;
                    }
                }
                if (entities.contains(entity2)) {
                    if (!entities.contains(entity1)) {
                        //strongDependence = false;
                        return false;
                    } else {
                        strongDependence = true;
                    }
                }
            }
        }
        return strongDependence;
    }

    public boolean ifAssociation(BusinessEntity entity1, BusinessEntity entity2) {
        boolean association = false;
        for (Operation operation : this.operations) {
            ArrayList<BusinessEntity> inputEntities = operation.getServiceBEDataModel().getEntities();
            ArrayList<BusinessEntity> outPutEntities = operation.getOutputServiceBEDataModel().getEntities();
            if (inputEntities.contains(entity2) && outPutEntities.contains(entity1)) {
                association = true;
                break;
            }
        }
        return association;
    }

    public boolean ifDomination(BusinessEntity dominater, BusinessEntity dominatee) {
        boolean dominationCondition1 = false;
        boolean dominationCondition2 = true;
        boolean dominationCondition3 = false;

        //every operation uses dominatee as an input parameter, dominater should be used as an input as well
        for (Operation operation : this.operations) {
            ArrayList<BusinessEntity> entities = operation.getServiceBEDataModel().getEntities();
            if (!entities.isEmpty()) {
                if (entities.contains(dominatee)) {
                    if (entities.contains(dominater)) {
                        dominationCondition1 = true;
                    } else {
                        dominationCondition1 = false;
                        break;
                    }

                }
            }
        }

        if (dominationCondition1) {

           
            if (dominationCondition2) {
                for (Operation operation : this.operations) {
                    ArrayList<BusinessEntity> entities = operation.getServiceBEDataModel().getEntities();
                    if (!entities.isEmpty()) {
                        if (entities.contains(dominater) && !entities.contains(dominatee)) {
                            dominationCondition3 = true;
                            break;
                        }
                    }
                }

   
            }

        }

        if (dominationCondition1 && dominationCondition2 && dominationCondition3) {
            return true;
        } else {
            return false;
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Operation getOperation(String operationName) {
        if (this.operations.isEmpty()) {
            getOperations();
        }
        for (Operation operation : this.operations) {
            if (operation.getName().equals(operationName)) {
                return operation;
            }
        }
        return null;
    }

    public ArrayList<String> fakeInvokeServiceOperation(String operationName, ArrayList<Parameter> path) {
        ArrayList<String> result = new ArrayList<String>();
        String request = "request";
        String responseXML = "response successfully";
        result.add(request);
        result.add(responseXML);
        return result;

    }

    public ArrayList<String> invokeServiceOperation(String operationName, ArrayList<Parameter> path) {
        ArrayList<String> result = new ArrayList<String>();
        WsdlProject project = null;
        try {
            project = new WsdlProject();
        } catch (XmlException ex) {
            log.log(Priority.FATAL, this.serviceName, ex);
        } catch (IOException ex) {
            log.log(Priority.FATAL, this.serviceName, ex);
        } catch (SoapUIException ex) {
            log.log(Priority.FATAL, this.serviceName, ex);
        }
        SoapUI.getSettings().setString(WsdlSettings.XML_GENERATION_TYPE_EXAMPLE_VALUE, "true");
        SoapUI.getSettings().setString(WsdlSettings.XML_GENERATION_SKIP_COMMENTS, "true");

        WsdlInterface iface = null;
        try {
            iface = WsdlInterfaceFactory.importWsdl(project, this.serviceWSDLName, true)[0];
        } catch (SoapUIException ex) {
            log.log(Priority.FATAL, this.serviceName, ex);
        }

        WsdlOperation operation = (WsdlOperation) iface.getOperationByName(operationName);
        ParameterAnalysis param = new ParameterAnalysis();
        WsdlContext wsdlContext = iface.getWsdlContext();
        BindingOperation bindingOperation = null;
        try {
            bindingOperation = operation.findBindingOperation(wsdlContext.getDefinition());
        } catch (Exception ex) {
            log.log(Priority.FATAL, this.serviceName, ex);
        }

        if (bindingOperation == null) {
            log.error(this.serviceName + " no bindingOperation generated");
        }

        ArrayList<DefaultMutableTreeNode> root = null;
        HashMap hashMap = null;
        String xmlContent = null;
        param.setIface(iface);
        param.setWsdlContext(wsdlContext);
        try {
            AnalysisStats.resetStats();
            hashMap = param.buildSoapMessageFromInput(bindingOperation, true, false, null, path, serviceName);
        } catch (Exception ex) {
            //log.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            log.error(ex);
        }

        Iterator<ArrayList<DefaultMutableTreeNode>> keySetIterator = hashMap.keySet().iterator();

        while (keySetIterator.hasNext()) {
            root = keySetIterator.next();
            xmlContent = (String) hashMap.get(root);
        }

        WsdlRequest request = operation.addNewRequest("My request");
        request.setRequestContent(operation.createRequest(true));

        
        String title = "------------------------------------REQUEST CONTENT (NUMBER OF PARAMETER: " + path.size() + ")-------------------------------------\n";

        writeFile("InvocationLog/" + operationName + ".xml", title, true);
        writeFile("InvocationLog/" + operationName + ".xml", xmlContent, true);

        String responseXML = null;
        Response response = null;
        if (xmlContent != null) {
            request.setRequestContent(xmlContent);

            WsdlSubmit submit = null;
            try {
                submit = (WsdlSubmit) request.submit(new WsdlSubmitContext(request), false);
            } catch (Request.SubmitException ex) {
                log.error(ex);
            }
            response = submit.getResponse();
            responseXML = response.getContentAsXml();
            title = "------------------------------------RESPONSE CONTENT-------------------------------------\n";
            writeFile("InvocationLog/" + operationName + ".xml", title, true);
            writeFile("InvocationLog/" + operationName + ".xml", responseXML, true);
        }
        result.add(xmlContent);
        result.add(responseXML);
        return result;
    }

    public ArrayList<BusinessEntity> getDepedentEntities(BusinessEntity entity, String relationFlag, String compulsory) {
        ArrayList<BusinessEntity> entities = new ArrayList<BusinessEntity>();
        ArrayList<EntityPair> pairs = null;

        if (relationFlag.equals("STRONG")) {
            pairs = this.serviceBEDataModel.getExclusiveContainmentPair();
        } else if (relationFlag.equals("WEAK")) {
            pairs = this.serviceBEDataModel.getStrongInclusiveContainmentPair();
        } else if (relationFlag.equals("ASSOCIATION")) {
            pairs = this.serviceBEDataModel.getAssociationPair();
        } else {
            return null;
        }

        for (EntityPair pair : pairs) {
            if (pair.getMainEntity().equals(entity)) {
                if (compulsory != null && compulsory.equals("YES")) {
                    if (pair.getSlaveEntity().isCompulsory()) {
                        for (BusinessEntity tempEntity : this.getServiceBEDataModel().getEntities()) {
                            if (tempEntity.equals(pair.getSlaveEntity())) {
                                entities.add(tempEntity);
                                break;
                            }
                        }
                    }
                } else if (compulsory != null && compulsory.equals("NO")) {
                    if (!pair.getSlaveEntity().isCompulsory()) {
                        for (BusinessEntity tempEntity : this.getServiceBEDataModel().getEntities()) {
                            if (tempEntity.equals(pair.getSlaveEntity())) {
                                entities.add(tempEntity);
                                break;
                            }
                        }
                    }
                } else {
                    for (BusinessEntity tempEntity : this.getServiceBEDataModel().getEntities()) {
                        if (tempEntity.equals(pair.getSlaveEntity())) {
                            entities.add(tempEntity);
                            break;
                        }
                    }
                }
            }
        }
        return entities;
    }

    public boolean ifPossessDependency(BusinessEntity entity) {
        ArrayList<EntityPair> pairs = new ArrayList<EntityPair>(this.serviceBEDataModel.getExclusiveContainmentPair());
        pairs.addAll(this.serviceBEDataModel.getStrongInclusiveContainmentPair());
        pairs.addAll(this.serviceBEDataModel.getAssociationPair());
        for (EntityPair pair : pairs) {
            if (pair.getMainEntity().equals(entity)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Operation> getOperations() {
        if (this.operations.isEmpty()) {
            WsdlProject project = null;
            try {
                project = new WsdlProject();
            } catch (XmlException ex) {
            } catch (IOException ex) {
            } catch (SoapUIException ex) {
            }
            WsdlInterface iface = null;
            try {
                iface = WsdlInterfaceFactory.importWsdl(project, this.serviceWSDLName, true)[0];
                String serviceName = iface.getName().replace("ServiceSoapBinding", "");
                this.setServiceName(serviceName);
            } catch (SoapUIException ex) {
            }

            for (int i = 0; i < iface.getOperationCount(); i++) {
                WsdlOperation wsdlOperation = iface.getOperationAt(i);
                ParameterAnalysis parameterAnalysis = new ParameterAnalysis();

                WsdlContext wsdlContext = iface.getWsdlContext();

                BindingOperation bindingOperation = null;
                try {
                    bindingOperation = wsdlOperation.findBindingOperation(wsdlContext.getDefinition());
                } catch (Exception ex) {
                }

                if (bindingOperation == null) {
                }
                ArrayList<DefaultMutableTreeNode> nodesInput = null, nodesOutput = null;
                HashMap hashMapInput = null, hashMapOutput = null;
                String xmlContentInput = null, xmlContentOutput = null;
                parameterAnalysis.setIface(iface);
                parameterAnalysis.setWsdlContext(wsdlContext);

                try {
                    AnalysisStats.resetStats();
                    hashMapInput = parameterAnalysis.buildSoapMessageFromInput(bindingOperation, true, false, null, null, this.serviceName);
                } catch (Exception ex) {
                }

                if (hashMapInput != null) {
                    Iterator<ArrayList<DefaultMutableTreeNode>> keySetIteratorInput = hashMapInput.keySet().iterator();

                    while (keySetIteratorInput.hasNext()) {
                        nodesInput = keySetIteratorInput.next();
                        xmlContentInput = (String) hashMapInput.get(nodesInput);
                    }

                    Operation operation = new Operation(wsdlOperation.getName());
                    ArrayList<Parameter> parameters = new ArrayList<Parameter>();

                    for (DefaultMutableTreeNode root : nodesInput) {
                        Parameter parameter = (Parameter) root.getUserObject();
                        parameter.setRoot(root);
                       
                        parameters.add(parameter);
                    }
                    operation.setSimpleInputParameterList(AnalysisStats.simpleParameterList);
                    operation.setComplexInputParameterList(AnalysisStats.complexParameterList);
                    operation.setCompulsoryInputParameterList(AnalysisStats.compulsoryInputParameterList);
                    operation.setOptionalInputParameterList(AnalysisStats.optionalInputParameterList);
                    operation.setInputParameterString(AnalysisStats.listofPreliminary);
                    operation.setNumberOfInputParameters(AnalysisStats.simpleParameterList.size());
                
                    operation.setInputParameters(parameters);
                    operation.setInputParametersWithMockValues(xmlContentInput);

                    try {
                        AnalysisStats.resetStats();
                        hashMapOutput = parameterAnalysis.buildSoapMessageFromOutput(bindingOperation, true, false, null, this.serviceName);
                    } catch (Exception ex) {
                    }

                    if (hashMapOutput != null) {

                        Iterator<ArrayList<DefaultMutableTreeNode>> keySetIteratorOutput = hashMapOutput.keySet().iterator();

                        while (keySetIteratorOutput.hasNext()) {
                            nodesOutput = keySetIteratorOutput.next();
                            xmlContentOutput = (String) hashMapOutput.get(nodesOutput);
                        }

                        parameters = new ArrayList<Parameter>();

                        for (DefaultMutableTreeNode root : nodesOutput) {
                            Parameter parameter = (Parameter) root.getUserObject();
                            parameter.setRoot(root);
                           
                            parameters.add(parameter);
                        }
                        operation.setOutputParameters(parameters);
                        operation.setOutPutParametersWithMockValues(xmlContentOutput);
                        operation.setOutputParameterString(AnalysisStats.listofPreliminary);
                        operation.setNumberOfOutputParameters(AnalysisStats.listofPreliminary.size());

                    }
                    this.operations.add(operation);
                }
            }
        }

        return this.operations;

    }

    public String getServiceWSDLName() {
        return serviceWSDLName;
    }

    public Service(String serviceWSDLName) {
        this.serviceWSDLName = serviceWSDLName;
        this.operations = new ArrayList<Operation>();
        this.serviceBEDataModel = new ServiceBEDataModel();
    }

    public ServiceBEDataModel getServiceBEDataModel() {
        if (this.serviceBEDataModel.getEntities().isEmpty()) {
            generateServiceBEDataModel();
        }
        return serviceBEDataModel;

    }

}
