/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lunar.quickswapserver;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Administrator
 */
public class LipaNaMpesaRequest {

    @SerializedName("MerchantRequestID")
    private String MerchantRequestID;
    @SerializedName("CheckoutRequestID")
    private String CheckoutRequestID;
    @SerializedName("ResponseCode")
    private String ResponseCode;
    @SerializedName("ResponseDescription")
    private String ResponseDescription;
    @SerializedName("CustomerMessage")
    private String CustomerMessage;

    public String getMerchantRequestID() {
        return MerchantRequestID;
    }

    public void setMerchantRequestID(String MerchantRequestID) {
        this.MerchantRequestID = MerchantRequestID;
    }

    public String getCheckoutRequestID() {
        return CheckoutRequestID;
    }

    public void setCheckoutRequestID(String CheckoutRequestID) {
        this.CheckoutRequestID = CheckoutRequestID;
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String ResponseCode) {
        this.ResponseCode = ResponseCode;
    }

    public String getResponseDescription() {
        return ResponseDescription;
    }

    public void setResponseDescription(String ResponseDescription) {
        this.ResponseDescription = ResponseDescription;
    }

    public String getCustomerMessage() {
        return CustomerMessage;
    }

    public void setCustomerMessage(String CustomerMessage) {
        this.CustomerMessage = CustomerMessage;
    }

}
