package edu.analysis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import edu.entities.Attribute;
import edu.entities.BusinessEntity;
import edu.entities.EntityPair;
import edu.services.Operation;
import edu.services.Parameter;
import edu.services.Service;
import edu.Utility;
import static edu.Utility.readXMLFile;

public class StructuralInterfaceAnalysis {

    public void identifyBEandRelationForAllOperations(Service service) {
        ArrayList<Operation> operations = service.getOperations();
        if (operations.size() > 0) {
            for (Operation operation : operations) {
                for (Parameter parameter : operation.getInputParameters()) {
                    identifyBEandRelation(service, operation, null, parameter, null);
                }
                for (Parameter parameter : operation.getOutputParameters()) {
                    identifyBEandRelation(service, operation, null, parameter, "OUTPUT");
                }
            }
        }
    }

    public void identifyBEandRelation(Service service, Operation operation, BusinessEntity entity, Parameter parameter, String intputOrOutput) {
        DefaultMutableTreeNode node = parameter.getRoot();
        BusinessEntity newEntity = null;
        if (parameter.isComplex()) { 

            newEntity = OntologyCheck(service.getServiceName(), parameter.getName(), parameter.getType());
            if (newEntity != null) {
                newEntity.setCompulsory(parameter.isCompulsory());
                ArrayList<Attribute> attributesList = addAndConvertAttributes(parameter.getRoot());
                if (!attributesList.isEmpty()) {
                    newEntity.addAttributes(attributesList);
                    //set the key
                    String key = Utility.readXMLFile(newEntity.getName(), "Key", "Configurations/BusinessEntityKeys.xml");
                    if (key != null) {
                        newEntity.setKey(key);
                    }
                }

                if (intputOrOutput != null && intputOrOutput.equals("OUTPUT")) {
                    if (!operation.getOutputServiceBEDataModel().getEntities().contains(newEntity)) {
                        operation.getOutputServiceBEDataModel().addEntity(newEntity);
                    }
                } else if (!operation.getServiceBEDataModel().getEntities().contains(newEntity)) {
                    operation.getServiceBEDataModel().addEntity(newEntity);
                }

                if (entity != null) {
                    EntityPair entityPair = new EntityPair(entity, newEntity);
                    if (intputOrOutput != null && intputOrOutput.equals("OUTPUT")) {
                        if (!operation.getOutputServiceBEDataModel().getNestingPair().contains(entityPair)) {
                            operation.getOutputServiceBEDataModel().addNestingPair(entityPair);
                        }
                    } else {
                        if (!operation.getServiceBEDataModel().getNestingPair().contains(entityPair)) {
                            operation.getServiceBEDataModel().addNestingPair(entityPair);
                        }
                    }
                }
                parameter.setMappedEntity(newEntity);
                node.setUserObject(parameter);
            }
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            Parameter tempParameter = (Parameter) childNode.getUserObject();

            // String parameterName = childNode.toString();
            tempParameter.setRoot(childNode);
            if (tempParameter.isComplex()) {
                identifyBEandRelation(service, operation, newEntity, tempParameter, intputOrOutput);
            }
        }      
    }

    private ArrayList<Attribute> addAndConvertAttributes(DefaultMutableTreeNode node) {
        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
        if (node != null) {
            int childCount = node.getChildCount();
            if (node.getChildCount() > 0) {
                for (int i = 0; i < childCount; i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);

                    Parameter parameter = (Parameter) childNode.getUserObject();
                    Attribute attribute = new Attribute(parameter.getName());
                    attribute.setType(parameter.getType());
                    attributesList.add(attribute);
                }
            }
        }
        return attributesList;
    }

    public void refineBERelation(Service service) {
        boolean mandatoryStrongDep = false;
        boolean optionalStrongDep = false;
        boolean weakDep = false;
        ArrayList<Operation> operations = service.getOperations();
        if (operations.size() > 0) {
            for (Operation operation : operations) {
                ArrayList<BusinessEntity> entities = operation.getServiceBEDataModel().getEntities();
                if (entities != null) {
                    for (BusinessEntity entity : entities) {                          
                        ArrayList<BusinessEntity> nestedEntities = operation.getNestedEntities(entity);
                        for (BusinessEntity nestedEntity : nestedEntities) {
                            if (service.ifMandatoryStrongDomination(entity, nestedEntity)) { 
                                mandatoryStrongDep = true;
                            } else {
                                boolean sameEntity = false;
                                ArrayList<BusinessEntity> dominaters = service.getDominaters(nestedEntity);
                                for (BusinessEntity tempEntity: dominaters) {
                                    if (tempEntity.equals(entity)) {
                                        sameEntity = true;
                                    } else {
                                        sameEntity = false;
                                        break;
                                    }
                                }
                                if (sameEntity) {
                                    optionalStrongDep = true;
                                }
                            }
                            if (!mandatoryStrongDep && !optionalStrongDep) {
                                if (service.ifDomination(nestedEntity, entity)) {
                                    weakDep = true;
                                }
                            }

                            EntityPair entityPair = new EntityPair(entity, nestedEntity);

                            if (mandatoryStrongDep || optionalStrongDep) {
                                if (nestedEntity.isCompulsory())
                                    operation.getServiceBEDataModel().addExclusiveContainmentPair(entityPair);
                                else
                                    operation.getServiceBEDataModel().addOptionalExclusiveContainmentPair(entityPair);
                                        
                            }
                            if (weakDep) {
                                if (service.ifAssociation(entity, nestedEntity)) {
                                    operation.getServiceBEDataModel().addAssociationPair(entityPair);
                                } else {
                                    if (nestedEntity.isCompulsory())
                                        //operation.getServiceBEDataModel().add
                                        operation.getServiceBEDataModel().addStrongInclusiveContainmentPair(entityPair);
                                    else
                                        operation.getServiceBEDataModel().addWeakInclusiveContainmentPair(entityPair);
                                }
                            }
                            mandatoryStrongDep = false;
                            optionalStrongDep = false;
                            weakDep = false;
                         
                        }
                    }
                }
            }
        }

    }

    public BusinessEntity OntologyCheck(String serviceName, String parameterName, String parameterType) {
        
        String entityName = null;
        if (parameterType != null) {
            entityName = readXMLFile(serviceName, parameterType, "Configurations/NonBOs.xml");
        } else {
            entityName = readXMLFile(serviceName, parameterName, "Configurations/NonBOs.xml");
        }

        BusinessEntity entity = null;
        if (entityName == null) {
            String meaningfulBEName = null;
            if (parameterType != null) {
                meaningfulBEName = readXMLFile(serviceName, parameterType, "Configurations/PredefinedBOs.xml");
            } else {
                meaningfulBEName = readXMLFile(serviceName, parameterName, "Configurations/PredefinedBOs.xml");
            }

            if (meaningfulBEName == null) {
                meaningfulBEName = parameterName;
                if (parameterType != null && !meaningfulBEName.equals(parameterType)) {
                    meaningfulBEName = parameterName + parameterType;  //For now
                }
            }
            entity = new BusinessEntity(meaningfulBEName);
            entity.setType(parameterType);
        }
        return entity;
    }
    
    public void printJsonfile() {
        
    }

    public static void main(String[] args) throws FileNotFoundException {

        long endTime, startTime, duration;
        StructuralInterfaceAnalysis structuralInterfaceAnalysis = new StructuralInterfaceAnalysis();
            
        Service service = new Service("TestData/ES/Fedex/OpenShipService_v9.wsdl");
        startTime = System.nanoTime();
        structuralInterfaceAnalysis.identifyBEandRelationForAllOperations(service);
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000000;  //divide by 1000000 to get milliseconds
        System.out.print("time consumed in executing identifyBEandRelationForAllOperations: " + duration);

        startTime = System.nanoTime();
        structuralInterfaceAnalysis.refineBERelation(service);
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000000;  //divide by 1000000 to get milliseconds
        System.out.print("time consumed in executing refineBERelation: " + duration);
        service.visualiseBEModel();
        service.outPutStatistics(duration, 0, false);
        service.visualiseAllOperationsBEModel();
        service.outputBEModelToXML();
        service.outputOperationsToXML();        
        
        
        System.exit(1);

    }

}
