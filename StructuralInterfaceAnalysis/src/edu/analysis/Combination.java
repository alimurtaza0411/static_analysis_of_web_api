package edu.analysis;

import java.util.ArrayList;
import edu.services.Parameter;

public class Combination {
    private ArrayList<Parameter> parameterSet;
    private String requestContent;
    private String response;

    public ArrayList<Parameter> getParameterSet() {
        return parameterSet;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public String getResponse() {
        return response;
    }

    public Combination(ArrayList<Parameter> parameterSets, String requesContent, String response) {
        this.parameterSet = parameterSets;
        this.requestContent = requesContent;
        this.response = response;
    }

    public void setParameterSets(ArrayList<Parameter> parameterSets) {
        this.parameterSet = parameterSets;
    }

    public void setRequestContent(String requesContent) {
        this.requestContent = requesContent;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
