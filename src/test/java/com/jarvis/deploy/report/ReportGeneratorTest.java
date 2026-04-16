package com.jarvis.deploy.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private ReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ReportGenerator();
    }

    private ReportEntry entry(String id, String version, String status, long duration) {
        return new ReportEntry(id, version, status, Instant.now(), "ci-bot", duration);
    }

    @Test
    void generateReportWithValidInputs() {
        List<ReportEntry> entries = List.of(
                entry("d1", "1.0.0", "SUCCESS", 30),
                entry("d2", "1.0.1", "FAILED", 15),
                entry("d3", "1.0.2", "ROLLED_BACK", 20)
        );
        DeploymentReport report = generator.generate("staging", entries);
        assertNotNull(report.getReportId());
        assertEquals("staging", report.getEnvironment());
        assertEquals(3, report.getTotalDeployments());
        assertEquals(1, report.countByStatus("SUCCESS"));
        assertEquals(1, report.countByStatus("FAILED"));
        assertEquals(1, report.countByStatus("ROLLED_BACK"));
    }

    @Test
    void summaryContainsExpectedKeys() {
        List<ReportEntry> entries = List.of(entry("d1", "2.0.0", "SUCCESS", 60));
        DeploymentReport report = generator.generate("prod", entries);
        assertTrue(report.getSummary().containsKey("total"));
        assertTrue(report.getSummary().containsKey("success"));
        assertTrue(report.getSummary().containsKey("failed"));
        assertTrue(report.getSummary().containsKey("avgDurationSeconds"));
        assertEquals("1", report.getSummary().get("total"));
        assertEquals("60.0", report.getSummary().get("avgDurationSeconds"));
    }

    @Test
    void generateThrowsOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("", List.of()));
    }

    @Test
    void generateThrowsOnNullEntries() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("dev", null));
    }

    @Test
    void exportAsCsvContainsHeader() {
        List<ReportEntry> entries = List.of(entry("d1", "1.0.0", "SUCCESS", 10));
        DeploymentReport report = generator.generate("dev", entries);
        ReportExporter exporter = new ReportExporter();
        String csv = exporter.exportAsCsv(report);
        assertTrue(csv.startsWith("deploymentId,artifactVersion,status"));
        assertTrue(csv.contains("d1"));
    }

    @Test
    void exportAsTextContainsSummarySection() {
        List<ReportEntry> entries = List.of(entry("d1", "1.0.0", "SUCCESS", 45));
        DeploymentReport report = generator.generate("qa", entries);
        ReportExporter exporter = new ReportExporter();
        String text = exporter.exportAsText(report);
        assertTrue(text.contains("Summary"));
        assertTrue(text.contains("qa"));
        assertTrue(text.contains("SUCCESS"));
    }
}
