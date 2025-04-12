package com.example.cooksmart_n19.models;

import com.google.firebase.firestore.DocumentId;

public class CookingStep {
    @DocumentId
    private String stepId;
    private int stepNumber;
    private String instruction;
    private String images;

    public CookingStep() {}

    public CookingStep(String stepId, int stepNumber, String instruction, String images) {
        this.stepId = stepId;
        this.stepNumber = stepNumber;
        this.instruction = instruction;
        this.images = images;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "CookingStep{" +
                "stepId='" + stepId + '\'' +
                ", stepNumber=" + stepNumber +
                ", instruction='" + instruction + '\'' +
                ", images='" + images + '\'' +
                '}';
    }
}