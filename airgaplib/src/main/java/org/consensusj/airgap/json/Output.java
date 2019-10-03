package org.consensusj.airgap.json;

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
        "receiver",
        "amount",
        "derivation"
})
public class Output {

    @JsonProperty("uid")
    private String uid;
    @JsonProperty("receiver")
    private String receiver;
    @JsonProperty("amount")
    private Long amount;
    @JsonProperty("derivation")
    private Derivation derivation;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Output() {
    }

    /**
     *
     * @param amount
     * @param uid
     * @param derivation
     * @param receiver
     */
    public Output(String uid, String receiver, Long amount, Derivation derivation) {
        super();
        this.uid = uid;
        this.receiver = receiver;
        this.amount = amount;
        this.derivation = derivation;
    }

    @JsonProperty("uid")
    public String getUid() {
        return uid;
    }

    @JsonProperty("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @JsonProperty("receiver")
    public String getReceiver() {
        return receiver;
    }

    @JsonProperty("receiver")
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @JsonProperty("amount")
    public Long getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Long amount) {
        this.amount = amount;
    }

    @JsonProperty("derivation")
    public Derivation getDerivation() {
        return derivation;
    }

    @JsonProperty("derivation")
    public void setDerivation(Derivation derivation) {
        this.derivation = derivation;
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
