package com.msp.messenger.bean.settging;

/**
 * Created by Y.B.H(mium2) on 17. 1. 18..
 */
public class OrganizationBean {
    private int ORGAN_IDX = 0;
    private int ORGAN_P_IDX = 0;
    private String ORGAN_NAME = "";
    private String ORGAN_NAME_EN = "";
    private int DEPTH = 0;
    private int ORDERBY = 0;
    private String DESCRIPTION = "";

    public int getORGAN_IDX() {
        return ORGAN_IDX;
    }

    public void setORGAN_IDX(int ORGAN_IDX) {
        this.ORGAN_IDX = ORGAN_IDX;
    }

    public int getORGAN_P_IDX() {
        return ORGAN_P_IDX;
    }

    public void setORGAN_P_IDX(int ORGAN_P_IDX) {
        this.ORGAN_P_IDX = ORGAN_P_IDX;
    }

    public String getORGAN_NAME() {
        return ORGAN_NAME;
    }

    public void setORGAN_NAME(String ORGAN_NAME) {
        this.ORGAN_NAME = ORGAN_NAME;
    }

    public String getORGAN_NAME_EN() {
        return ORGAN_NAME_EN;
    }

    public void setORGAN_NAME_EN(String ORGAN_NAME_EN) {
        this.ORGAN_NAME_EN = ORGAN_NAME_EN;
    }

    public int getDEPTH() {
        return DEPTH;
    }

    public void setDEPTH(int DEPTH) {
        this.DEPTH = DEPTH;
    }

    public int getORDERBY() {
        return ORDERBY;
    }

    public void setORDERBY(int ORDERBY) {
        this.ORDERBY = ORDERBY;
    }

    public String getDESCRIPTION() {
        return DESCRIPTION;
    }

    public void setDESCRIPTION(String DESCRIPTION) {
        this.DESCRIPTION = DESCRIPTION;
    }
}
