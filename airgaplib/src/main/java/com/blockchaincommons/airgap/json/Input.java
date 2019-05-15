package com.blockchaincommons.airgap.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "uid",
        "txHash",
        "inputIndex",
        "sender",
        "derivation",
        "amount"
})
public class Input {

    @JsonProperty("uid")
    private String uid;
    @JsonProperty("txHash")
    private String txHash;
    @JsonProperty("inputIndex")
    private Long inputIndex;
    @JsonProperty("sender")
    private String sender;
    @JsonProperty("derivation")
    private Derivation derivation;
    @JsonProperty("amount")
    private Long amount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Input() {
    }

    /**
     *
     * @param amount
     * @param sender
     * @param uid
     * @param derivation
     * @param inputIndex
     * @param txHash
     */
    public Input(String uid, String txHash, Long inputIndex, String sender, Derivation derivation, Long amount) {
        super();
        this.uid = uid;
        this.txHash = txHash;
        this.inputIndex = inputIndex;
        this.sender = sender;
        this.derivation = derivation;
        this.amount = amount;
    }

    @JsonProperty("uid")
    public String getUid() {
        return uid;
    }

    @JsonProperty("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @JsonProperty("txHash")
    public String getTxHash() {
        return txHash;
    }

    @JsonProperty("txHash")
    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    @JsonProperty("inputIndex")
    public Long getInputIndex() {
        return inputIndex;
    }

    @JsonProperty("inputIndex")
    public void setInputIndex(Long inputIndex) {
        this.inputIndex = inputIndex;
    }

    @JsonProperty("sender")
    public String getSender() {
        return sender;
    }

    @JsonProperty("sender")
    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty("derivation")
    public Derivation getDerivation() {
        return derivation;
    }

    @JsonProperty("derivation")
    public void setDerivation(Derivation derivation) {
        this.derivation = derivation;
    }

    @JsonProperty("amount")
    public Long getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Long amount) {
        this.amount = amount;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
