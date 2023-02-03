package com.tcon;

public class TFResult implements Comparable<TFResult> {
    String rule_id;
    String long_id;
    String rule_description;
    String resolution;
    String severity;
    int status;
    String resource;
    TFFileLocation location;

    public String getSeverity() {
        return severity;
    }

    public int getStatus() {
        return status;
    }

    public String status() {
        return status == 1 ? "PASSED" : "FAILED";
    }

    @Override
    public int compareTo(TFResult other) {
        return severityInNumber().compareTo(other.severityInNumber());
    }

    private Integer severityInNumber() {
        if("CRITICAL".equals(severity)){
            return 4;
        }
        if("HIGH".equals(severity)){
            return 3;
        }
        if("MEDIUM".equals(severity)){
            return 2;
        }

        return 1;
    }
}
