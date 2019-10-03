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
        "header",
        "transaction"
})
public class TransactionSigningRequest {

    @JsonProperty("header")
    private Header header;
    @JsonProperty("transaction")
    private UnsignedTransaction transaction;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public TransactionSigningRequest() {
    }

    /**
     *
     * @param transaction
     * @param header
     */
    public TransactionSigningRequest(Header header, UnsignedTransaction transaction) {
        super();
        this.header = header;
        this.transaction = transaction;
    }

    @JsonProperty("header")
    public Header getHeader() {
        return header;
    }

    @JsonProperty("header")
    public void setHeader(Header header) {
        this.header = header;
    }

    @JsonProperty("transaction")
    public UnsignedTransaction getTransaction() {
        return transaction;
    }

    @JsonProperty("transaction")
    public void setTransaction(UnsignedTransaction transaction) {
        this.transaction = transaction;
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