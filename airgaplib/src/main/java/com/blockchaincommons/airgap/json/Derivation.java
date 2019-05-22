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
        "accountIndex",
        "addressIndex",
        "isChange"
})
public class Derivation {

    @JsonProperty("accountIndex")
    private Long accountIndex;
    @JsonProperty("addressIndex")
    private Long addressIndex;
    @JsonProperty("isChange")
    private Boolean isChange;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Derivation() {
    }

    /**
     *
     * @param accountIndex
     * @param addressIndex
     */
    public Derivation(Long accountIndex, Long addressIndex, Boolean isChange) {
        super();
        this.accountIndex = accountIndex;
        this.addressIndex = addressIndex;
        this.isChange = isChange;
    }

    @JsonProperty("accountIndex")
    public Long getAccountIndex() {
        return accountIndex;
    }

    @JsonProperty("accountIndex")
    public void setAccountIndex(Long accountIndex) {
        this.accountIndex = accountIndex;
    }

    @JsonProperty("addressIndex")
    public Long getAddressIndex() {
        return addressIndex;
    }

    @JsonProperty("addressIndex")
    public void setAddressIndex(Long addressIndex) {
        this.addressIndex = addressIndex;
    }

    @JsonProperty("isChange")
    public Boolean getIsChange() {
        return isChange;
    }

    @JsonProperty("isChange")
    public void setIsChange(Boolean isChange) {
        this.isChange = isChange;
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
