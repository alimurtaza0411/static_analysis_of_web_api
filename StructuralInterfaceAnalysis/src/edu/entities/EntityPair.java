package edu.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import entities.BusinessEntity;
public class EntityPair {
    @JsonIgnore private BusinessEntity mainEntity;
    @JsonIgnore private BusinessEntity slaveEntity;
    private String strMainEntity;
    private String strSlaveEntity;

    public void setStrMainEntity(String strMainEntity) {
        this.strMainEntity = strMainEntity;
    }

    public void setStrSlaveEntity(String strSlaveEntity) {
        this.strSlaveEntity = strSlaveEntity;
    }

    public String getStrMainEntity() {
        return strMainEntity;
    }

    public String getStrSlaveEntity() {
        return strSlaveEntity;
    }

    
    public EntityPair(BusinessEntity mainEntity, BusinessEntity slaveEntity) {
        this.mainEntity = mainEntity;
        this.strMainEntity = mainEntity.getName();
        this.slaveEntity = slaveEntity;
        this.strSlaveEntity = slaveEntity.getName();        
    }

    public void setMainEntity(BusinessEntity mainEntity) {
        this.mainEntity = mainEntity;
        this.strMainEntity = mainEntity.getName();
    }

    public void setSlaveEntity(BusinessEntity slaveEntity) {
        this.slaveEntity = slaveEntity;
        this.strSlaveEntity = slaveEntity.getName();        
    }

    public BusinessEntity getMainEntity() {
        return mainEntity;
    }

    public BusinessEntity getSlaveEntity() {
        return slaveEntity;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof EntityPair)
        {
            sameSame = this.mainEntity.getName().equals(((EntityPair) object).mainEntity.getName()) 
                    && this.slaveEntity.getName().equals(((EntityPair) object).slaveEntity.getName());
        }
        return sameSame;
    }    
}
