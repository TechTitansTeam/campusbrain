package com.campusbrain.entity;

public enum MaterialCategory {
    NOTES("Notes", "Comprehensive lecture notes, conceptual summaries, and revision guides."),
    LAB_MANUAL("Lab Manuals", "Laboratory manuals, experiment instructions, coding guides, and viva references."),
    ASSIGNMENT("Assignments", "Coursework assignments, project briefs, problem sets, and submission tasks."),
    PREVIOUS_QUESTION_PAPER("Previous Question Papers", "Past semester university examination question papers and model test papers.");

    private final String displayName;
    private final String description;

    MaterialCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static MaterialCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().toUpperCase().replace(" ", "_");
        for (MaterialCategory category : MaterialCategory.values()) {
            if (category.name().equalsIgnoreCase(clean) || 
                category.displayName.equalsIgnoreCase(text.trim())) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + text);
    }
}
