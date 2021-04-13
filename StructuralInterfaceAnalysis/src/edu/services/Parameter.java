package edu.services;

import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import edu.entities.BusinessEntity;
import edu.Utility;

public class Parameter {

    private String name;
    private boolean complex;
    private Parameter parentParameter;
    private int parameterUniqueIDinTree;
    private boolean compulsory;
    private BusinessEntity mappedEntity;
    //this is used to store the markov blanket
    private HashMap<Integer, Integer> markovBlanket;
    private int level;    
    private int simpleIndex;
    private ArrayList<Parameter> children;    
    private Group groupBelongTo;

    public void setGroupBelongTo(Group groupBelongTo) {
        this.groupBelongTo = groupBelongTo;
    }

    public Group getGroupBelongTo() {
        return groupBelongTo;
    }

    
    /*
    public void addMarkovBlanket(String key, String value) {
        if (markovBlanket == null)
            markovBlanket = new HashMap();
        markovBlanket.put(key, value)
    }
    */
    public void addChild(Parameter parameter) {
        if (children == null)
            children = new ArrayList<Parameter>();
        children.add(parameter);
    }
            
    public ArrayList<Parameter> getChildren() {
        return children;
    }
            
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    
    public HashMap getMarkovBlanket() {
        if (markovBlanket == null)
            markovBlanket = new HashMap();
        return markovBlanket;
    }
    
   public void setSimpleIndex(int simpleIndex) {
        this.simpleIndex = simpleIndex;
    }

    public int getSimpleIndex() {
        return simpleIndex;
    }

    public BusinessEntity getMappedEntity() {
        return mappedEntity;
    }

    public void setMappedEntity(BusinessEntity mappedEntity) {
        this.mappedEntity = mappedEntity;
    }

    public boolean isCompulsory() {
        return compulsory;
    }

    public void setCompulsory(boolean compulsory) {
        this.compulsory = compulsory;
    }

    public int getParameterUniqueIDinTree() {
        return parameterUniqueIDinTree;
    }

    public void setParameterUniqueIDinTree(int parameterUniqueIDinTree) {
        this.parameterUniqueIDinTree = parameterUniqueIDinTree;
    }

    public Parameter getParentParameter() {
        return parentParameter;
    }

    public void setParentParameter(Parameter parentParameter) {
        this.parentParameter = parentParameter;
    }

    public boolean isComplex() {
        return complex;
    }

    public void setComplex(boolean complex) {
        this.complex = complex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }
    private String type;
    private DefaultMutableTreeNode root = null; // The tree structure of the parameter

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    public void setRoot(DefaultMutableTreeNode root) {
        this.root = root;
    }
    
    public String toString() {
        return this.simpleIndex+"-"+this.name;
    }
    
    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof Parameter)
        {            
            sameSame = this.name.equals(((Parameter) object).getName()) 
                    && this.getParameterUniqueIDinTree() == ((Parameter)object).getParameterUniqueIDinTree();
        }
        return sameSame;
    }        
    
}
