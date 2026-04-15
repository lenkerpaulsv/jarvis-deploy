package com.jarvis.deploy.audit;

import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private Path tempLogFile;
    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() throws IOException {
        tempLogFile = Files.createTempFile("audit-test", ".log");
        auditLogger = new AuditLogger(tempLogFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempLogFile);
    }

    @Test
    void testLogSingleEntry() throws IOException {
        AuditEntry entry = new AuditEntry("DEPLOY", "staging", "1.2.3", "alice", LocalDateTime.of(2024, 6, 1, 10, 0));
        auditLogger.log(entry);

        List<String> lines = auditLogger.readAll();
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ACTION=DEPLOY"));
        assertTrue(lines.get(0).contains("ENV=staging"));
        assertTrue(lines.get(0).contains("VERSION=1.2.3"));
        assertTrue(lines.get(0).contains("USER=alice"));
    }

    @Test
    void testLogMultipleEntries() throws IOException {
        auditLogger.log(new AuditEntry("DEPLOY", "prod", "2.0.0", "bob", LocalDateTime.now()));
        auditLogger.log(new AuditEntry("ROLLBACK", "prod", "1.9.0", "bob", LocalDateTime.now()));

        List<String> lines = auditLogger.readAll();
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("ACTION=ROLLBACK"));
    }

    @Test
    void testLogDeploymentRecord() throws IOException {
        DeploymentRecord record = new DeploymentRecord("dev", "3.1.0", LocalDateTime.of(2024, 7, 15, 9, 30));
        auditLogger.logDeployment(record, "DEPLOY", "carol");

        List<String> lines = auditLogger.readAll();
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ENV=dev"));
        assertTrue(lines.get(0).contains("VERSION=3.1.0"));
        assertTrue(lines.get(0).contains("USER=carol"));
    }

    @Test
    void testReadAllReturnsEmptyWhenFileAbsent() throws IOException {
        Files.deleteIfExists(tempLogFile);
        AuditLogger freshLogger = new AuditLogger(tempLogFile.toString());
        List<String> lines = freshLogger.readAll();
        assertTrue(lines.isEmpty());
    }

    @Test
    void testAuditEntryEquality() {
        LocalDateTime now = LocalDateTime.now();
        AuditEntry a = new AuditEntry("DEPLOY", "staging", "1.0", "user", now);
        AuditEntry b = new AuditEntry("DEPLOY", "staging", "1.0", "user", now);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
