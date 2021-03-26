package org.xrpl.xrpl4j.client.dex.model;

/**
 * Gets or Sets Side
 */
public enum Side {
    
    BUY, 
    SELL;

    @Override
    public String toString() {
        return name();
    }

    public static Side fromString(String text) {
        for (Side b : Side.values()) {
            if (b.name().toLowerCase().compareTo(text) == 0 || b.name().compareTo(text) == 0) {
                return b;
            }
        }
        return null;
    }
}
