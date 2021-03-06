package com.domain;

public enum Privilege {
    ELDER("Elder"),
    MINISTERIAL_SERVANT("Ministerial Servant"),
    PUBLISHER("Publisher"),
    UNBAPTIZED_PUBLISHER("Unbaptized Publisher");

    private final String text;

    Privilege(String privilegeName) {
        text = privilegeName;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
