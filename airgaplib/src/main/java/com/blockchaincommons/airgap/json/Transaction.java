package com.blockchaincommons.airgap.json;

import java.util.HashMap;
import java.util.List;
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
        "asset",
        "inputSignatures"
})
public class Transaction {

    @JsonProperty("uid")
    private String uid;
    @JsonProperty("asset")
    private String asset;
    @JsonProperty("inputSignatures")
    private List<InputSignature> inputSignatures = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("uid")
    public String getUid() {
        return uid;
    }

    @JsonProperty("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @JsonProperty("asset")
    public String getAsset() {
        return asset;
    }

    @JsonProperty("asset")
    public void setAsset(String asset) {
        this.asset = asset;
    }

    @JsonProperty("inputSignatures")
    public List<InputSignature> getInputSignatures() {
        return inputSignatures;
    }

    @JsonProperty("inputSignatures")
    public void setInputSignatures(List<InputSignature> inputSignatures) {
        this.inputSignatures = inputSignatures;
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
