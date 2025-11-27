package dev.jsinco.malts.enums;

public enum TriState {

    FALSE,
    ALTERNATIVE_STATE,
    TRUE;

    public boolean toBoolean() {
        return this == TRUE;
    }

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isAlternative() {
        return this == ALTERNATIVE_STATE;
    }

    public static TriState fromBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static TriState fromBoolean(Boolean b) {
        return b == null ? ALTERNATIVE_STATE : fromBoolean(b.booleanValue());
    }
}
