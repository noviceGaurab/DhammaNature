package io.virinchi.dhammanature.model.enums;

/** Product types sold on the FR-07 marketplace. */
public enum ProductCategory {
    PRAYER_BEADS("Prayer Beads"),
    INCENSE("Incense"),
    BOOKS("Books"),
    STATUES("Buddha Statues"),
    ACCESSORIES("Meditation Accessories"),
    HANDICRAFTS("Handicrafts");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
