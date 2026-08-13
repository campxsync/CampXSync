package logger.enums;

/**
 * Institute lifecycle states for platform-level management.
 *
 * State machine transition rules:
 *   ONBOARDING  → ACTIVE      (onboarding complete)
 *   ACTIVE      → SUSPENDED   (manual suspension by platform admin)
 *   ACTIVE      → OFFBOARDED  (permanent decommission)
 *   SUSPENDED   → ACTIVE      (reinstatement)
 *   SUSPENDED   → OFFBOARDED  (decommission from suspended state)
 *   OFFBOARDED  → (terminal — no transitions allowed)
 *
 * Using an enum instead of raw strings ensures transition rules are
 * compile-time verified and consistently applied across all services.
 */
public enum InstituteStatus {

    ONBOARDING {
        @Override
        public boolean canTransitionTo(InstituteStatus target) {
            return target == ACTIVE;
        }
    },

    ACTIVE {
        @Override
        public boolean canTransitionTo(InstituteStatus target) {
            return target == SUSPENDED || target == OFFBOARDED;
        }
    },

    SUSPENDED {
        @Override
        public boolean canTransitionTo(InstituteStatus target) {
            return target == ACTIVE || target == OFFBOARDED;
        }
    },

    OFFBOARDED {
        @Override
        public boolean canTransitionTo(InstituteStatus target) {
            // Terminal state — no transitions allowed
            return false;
        }
    };

    /**
     * Returns true if a transition from this state to the target state is permitted.
     */
    public abstract boolean canTransitionTo(InstituteStatus target);

    /**
     * Returns the lowercase string representation for API responses.
     * Consistent with the existing API contract (e.g. "active", "suspended").
     */
    public String toApiValue() {
        return this.name().toLowerCase();
    }

    /**
     * Parses a status string (case-insensitive) to an InstituteStatus enum value.
     * Throws IllegalArgumentException with a clear message if the value is unrecognised.
     */
    public static InstituteStatus fromApiValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Institute status must not be null or empty");
        }
        try {
            return InstituteStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid institute status: '" + value + "'. " +
                "Allowed values: onboarding, active, suspended, offboarded"
            );
        }
    }
}
