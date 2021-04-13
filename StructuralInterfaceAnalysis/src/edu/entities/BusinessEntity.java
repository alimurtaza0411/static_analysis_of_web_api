package edu.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import edu.services.Operation;


public class BusinessEntity {
    private String name;
    private String type;
    private boolean compulsory;
    private Attribute key;

    

    
    public boolean isCompulsory() {
        return compulsory;
    }

    public void setCompulsory(boolean compulsory) {
        this.compulsory = compulsory;
    }
    

    public Attribute getKey() {
        return key;
    }

    public void setKey(String keyName) {
        for(Attribute attribute: this.getAttributes())
            if (attribute.getName().equals(keyName))
                    this.key = attribute;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    private ArrayList<Attribute> attributes;

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

    public BusinessEntity(String name) {
        this.name = name;
        this.attributes = new ArrayList<Attribute>();
    }
    
    public void addAttributes(ArrayList<Attribute> attributeList) {        
        this.attributes.addAll(attributeList);
    }
    
    public void setCreateOperation(Operation createOperation) {
        this.createOperation = createOperation;
    }

    public void setReadOperation(Operation readOperation) {
        this.readOperation = readOperation;
    }

    public void setUpdateOperation(Operation updateOperation) {
        this.updateOperation = updateOperation;
    }

    public void setDeleteOperation(Operation deleteOperation) {
        this.deleteOperation = deleteOperation;
    }

    public Operation getCreateOperation() {
        return createOperation;
    }

    public Operation getReadOperation() {
        return readOperation;
    }

    public Operation getUpdateOperation() {
        return updateOperation;
    }

    public Operation getDeleteOperation() {
        return deleteOperation;
    }
    
    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof BusinessEntity)
        {
            sameSame = this.name.equals(((BusinessEntity) object).getName());
        }
        return sameSame;
    }        
}
