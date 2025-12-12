package org.example.model;

/**
 * Base interface for any issue we find. Could be salary, depth, whatever.
 */
public interface Issue {

    Type getType();
    String getDescription();

    /** The kinds of problems we look for. */
    enum Type {
        UNDERPAID_ISSUE("MANAGERS WHO EARN LESS THAN THEY SHOULD"),
        OVERPAID_ISSUE("MANAGERS WHO EARN MORE THAN THEY SHOULD"),
        HIERARCHY_DEPTH_ISSUE("EMPLOYEES WITH REPORTING LINE TOO LONG");

        private final String description;

        Type(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
