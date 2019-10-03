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
        "ecPublicKey",
        "ecSignature"
})
public class InputSignature {

    @JsonProperty("uid")
    private String uid;
    @JsonProperty("ecPublicKey")
    private String ecPublicKey;
    @JsonProperty("ecSignature")
    private String ecSignature;
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

    @JsonProperty("ecPublicKey")
    public String getEcPublicKey() {
        return ecPublicKey;
    }

    @JsonProperty("ecPublicKey")
    public void setEcPublicKey(String ecPublicKey) {
        this.ecPublicKey = ecPublicKey;
    }

    @JsonProperty("ecSignature")
    public String getEcSignature() {
        return ecSignature;
    }

    @JsonProperty("ecSignature")
    public void setEcSignature(String ecSignature) {
        this.ecSignature = ecSignature;
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
