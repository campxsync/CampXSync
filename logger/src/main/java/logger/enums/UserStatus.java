package logger.enums;

/**
 * User account lifecycle states for college-scoped identities.
 *
 * State machine transition rules:
 *   ACTIVE      → SUSPENDED   (manual suspension by admin)
 *   ACTIVE      → DEACTIVATED (permanent closure)
 *   SUSPENDED   → ACTIVE      (reinstatement)
 *   SUSPENDED   → DEACTIVATED (permanent closure from suspended state)
 *   DEACTIVATED → (terminal — no transitions allowed)
 *
 * Using an enum instead of raw strings ensures transition rules are
 * compile-time verified and consistently applied across all services.
 */
public enum UserStatus {

    ACTIVE {
        @Override
        public boolean canTransitionTo(UserStatus target) {
            return target == SUSPENDED || target == DEACTIVATED;
        }
    },

    SUSPENDED {
        @Override
        public boolean canTransitionTo(UserStatus target) {
            return target == ACTIVE || target == DEACTIVATED;
        }
    },

    DEACTIVATED {
        @Override
        public boolean canTransitionTo(UserStatus target) {
            // Terminal state — no transitions allowed
            return false;
        }
    };

    /**
     * Returns true if a transition from this state to the target state is permitted.
     */
    public abstract boolean canTransitionTo(UserStatus target);

    /**
     * Returns the lowercase string representation for API responses.
     * Consistent with the existing API contract (e.g. "active", "suspended").
     */
    public String toApiValue() {
        return this.name().toLowerCase();
    }

    /**
     * Parses a status string (case-insensitive) to a UserStatus enum value.
     * Throws IllegalArgumentException with a clear message if the value is unrecognised.
     */
    public static UserStatus fromApiValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("User status must not be null or empty");
        }
        try {
            return UserStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid user status: '" + value + "'. " +
                "Allowed values: active, suspended, deactivated"
            );
        }
    }
}
