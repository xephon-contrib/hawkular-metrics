package org.rhq.metrics.restServlet;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;

/**
 * @author John Sanda
 */
public class AvailabilityDataPoint {

    private long timestamp;

    private String value;

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    private Set<String> tags;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AvailabilityDataPoint that = (AvailabilityDataPoint) o;

        if (timestamp != that.timestamp) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", timestamp)
            .add("value", value)
            .add("tags", tags)
            .toString();
    }
}