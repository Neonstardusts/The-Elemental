package com.teamneon.theelemental.magic.base;

public class SpellCastResult {
    private final boolean success;
    private final String reason; // null if success or fail without reason

    private SpellCastResult(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    // Success
    public static SpellCastResult success() {
        return new SpellCastResult(true, null);
    }

    // Failure with reason
    public static SpellCastResult fail(String reason) {
        return new SpellCastResult(false, reason);
    }

    // Failure without reason
    public static SpellCastResult fail() {
        return new SpellCastResult(false, null);
    }

    public boolean isSuccess() { return success; }

    // Can be null if no reason
    public String getReason() { return reason; }
}
