package com.campusbrain.dto;

public class DashboardStatsResponse {
    private long totalMaterials;
    private long totalNotes;
    private long totalLabManuals;
    private long totalAssignments;
    private long totalQuestionPapers;
    private long totalPublished;
    private long totalUnpublished;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(long totalMaterials, long totalNotes, long totalLabManuals,
                                  long totalAssignments, long totalQuestionPapers,
                                  long totalPublished, long totalUnpublished) {
        this.totalMaterials = totalMaterials;
        this.totalNotes = totalNotes;
        this.totalLabManuals = totalLabManuals;
        this.totalAssignments = totalAssignments;
        this.totalQuestionPapers = totalQuestionPapers;
        this.totalPublished = totalPublished;
        this.totalUnpublished = totalUnpublished;
    }

    public long getTotalMaterials() {
        return totalMaterials;
    }

    public void setTotalMaterials(long totalMaterials) {
        this.totalMaterials = totalMaterials;
    }

    public long getTotalNotes() {
        return totalNotes;
    }

    public void setTotalNotes(long totalNotes) {
        this.totalNotes = totalNotes;
    }

    public long getTotalLabManuals() {
        return totalLabManuals;
    }

    public void setTotalLabManuals(long totalLabManuals) {
        this.totalLabManuals = totalLabManuals;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public long getTotalQuestionPapers() {
        return totalQuestionPapers;
    }

    public void setTotalQuestionPapers(long totalQuestionPapers) {
        this.totalQuestionPapers = totalQuestionPapers;
    }

    public long getTotalPublished() {
        return totalPublished;
    }

    public void setTotalPublished(long totalPublished) {
        this.totalPublished = totalPublished;
    }

    public long getTotalUnpublished() {
        return totalUnpublished;
    }

    public void setTotalUnpublished(long totalUnpublished) {
        this.totalUnpublished = totalUnpublished;
    }
}
