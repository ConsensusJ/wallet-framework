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
        "addressIndex"
})
public class Derivation {

    @JsonProperty("accountIndex")
    private Long accountIndex;
    @JsonProperty("addressIndex")
    private Long addressIndex;
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
    public Derivation(Long accountIndex, Long addressIndex) {
        super();
        this.accountIndex = accountIndex;
        this.addressIndex = addressIndex;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
