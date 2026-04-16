package tn.esprit.services;

import tn.esprit.entities.DailyTrackingEntry;
import tn.esprit.entities.DailyTrackingStats;
import tn.esprit.entities.DailyTrackingTrendPoint;
import tn.esprit.utils.MyDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyTrackingService {
    private final Connection connection;

    public DailyTrackingService() {
        connection = MyDB.getInstance().getConnection();
    }

    public DailyTrackingEntry getLatestEntryForUser(int userId) {
        String query = """
                SELECT dt.*, u.full_name
                FROM daily_tracking dt
                LEFT JOIN user u ON u.id = dt.user_id
                WHERE dt.user_id = ?
                ORDER BY dt.`date` DESC, dt.created_at DESC
                LIMIT 1
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapEntry(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest daily tracking entry for user: " + e.getMessage());
            return null;
        }
    }

    public DailyTrackingEntry getEntryForUserAndDate(int userId, LocalDate date) {
        String query = """
                SELECT dt.*, u.full_name
                FROM daily_tracking dt
                LEFT JOIN user u ON u.id = dt.user_id
                WHERE dt.user_id = ? AND dt.`date` = ?
                ORDER BY dt.created_at DESC
                LIMIT 1
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapEntry(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching daily tracking entry for date: " + e.getMessage());
            return null;
        }
    }

    public DailyTrackingEntry getLatestEntryForAllUsers() {
        String query = """
                SELECT dt.*, u.full_name
                FROM daily_tracking dt
                LEFT JOIN user u ON u.id = dt.user_id
                ORDER BY dt.`date` DESC, dt.created_at DESC
                LIMIT 1
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapEntry(rs) : null;
        } catch (SQLException e) {
            System.err.println("Error fetching latest daily tracking entry: " + e.getMessage());
            return null;
        }
    }

    public List<DailyTrackingEntry> getRecentEntriesForUser(int userId, int limit) {
        String query = """
                SELECT dt.*, u.full_name
                FROM daily_tracking dt
                LEFT JOIN user u ON u.id = dt.user_id
                WHERE dt.user_id = ?
                ORDER BY dt.`date` DESC, dt.created_at DESC
                LIMIT ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            return executeEntriesQuery(stmt);
        } catch (SQLException e) {
            System.err.println("Error fetching recent daily tracking entries for user: " + e.getMessage());
            return List.of();
        }
    }

    public List<DailyTrackingEntry> getRecentEntriesForAllUsers(int limit) {
        String query = """
                SELECT dt.*, u.full_name
                FROM daily_tracking dt
                LEFT JOIN user u ON u.id = dt.user_id
                ORDER BY dt.`date` DESC, dt.created_at DESC
                LIMIT ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            return executeEntriesQuery(stmt);
        } catch (SQLException e) {
            System.err.println("Error fetching recent daily tracking entries: " + e.getMessage());
            return List.of();
        }
    }

    public DailyTrackingStats getWeeklyStatsForUser(int userId) {
        String query = """
                SELECT COUNT(*) AS entry_count,
                       AVG(mood) AS avg_mood,
                       AVG(stress) AS avg_stress,
                       AVG(energy_level) AS avg_energy,
                       AVG(sleep_hours) AS avg_sleep,
                       AVG(water_intake) AS avg_water,
                       AVG(physical_activity_level) AS avg_activity
                FROM daily_tracking
                WHERE user_id = ? AND `date` >= ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(LocalDate.now().minusDays(6)));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapStats(rs) : new DailyTrackingStats();
            }
        } catch (SQLException e) {
            System.err.println("Error fetching weekly tracking stats for user: " + e.getMessage());
            return new DailyTrackingStats();
        }
    }

    public DailyTrackingStats getWeeklyStatsForAllUsers() {
        String query = """
                SELECT COUNT(*) AS entry_count,
                       AVG(mood) AS avg_mood,
                       AVG(stress) AS avg_stress,
                       AVG(energy_level) AS avg_energy,
                       AVG(sleep_hours) AS avg_sleep,
                       AVG(water_intake) AS avg_water,
                       AVG(physical_activity_level) AS avg_activity
                FROM daily_tracking
                WHERE `date` >= ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now().minusDays(6)));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapStats(rs) : new DailyTrackingStats();
            }
        } catch (SQLException e) {
            System.err.println("Error fetching weekly tracking stats: " + e.getMessage());
            return new DailyTrackingStats();
        }
    }

    public List<DailyTrackingTrendPoint> getTrendPointsForUser(int userId) {
        String query = """
                SELECT `date`,
                       AVG(mood) AS avg_mood,
                       AVG(stress) AS avg_stress
                FROM daily_tracking
                WHERE user_id = ? AND `date` >= ?
                GROUP BY `date`
                ORDER BY `date` ASC
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(LocalDate.now().minusDays(6)));
            return executeTrendQuery(stmt);
        } catch (SQLException e) {
            System.err.println("Error fetching daily tracking trend for user: " + e.getMessage());
            return List.of();
        }
    }

    public List<DailyTrackingTrendPoint> getTrendPointsForAllUsers() {
        String query = """
                SELECT `date`,
                       AVG(mood) AS avg_mood,
                       AVG(stress) AS avg_stress
                FROM daily_tracking
                WHERE `date` >= ?
                GROUP BY `date`
                ORDER BY `date` ASC
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now().minusDays(6)));
            return executeTrendQuery(stmt);
        } catch (SQLException e) {
            System.err.println("Error fetching daily tracking trend: " + e.getMessage());
            return List.of();
        }
    }

    public boolean saveEntry(DailyTrackingEntry entry) {
        if (entry == null || entry.getUserId() <= 0 || entry.getDate() == null) {
            return false;
        }

        Integer existingEntryId = findExistingEntryId(entry.getUserId(), entry.getDate());
        return existingEntryId == null ? insertEntry(entry) : updateEntry(existingEntryId, entry);
    }

    private List<DailyTrackingEntry> executeEntriesQuery(PreparedStatement stmt) throws SQLException {
        List<DailyTrackingEntry> entries = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                entries.add(mapEntry(rs));
            }
        }
        return entries;
    }

    private List<DailyTrackingTrendPoint> executeTrendQuery(PreparedStatement stmt) throws SQLException {
        List<DailyTrackingTrendPoint> points = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                DailyTrackingTrendPoint point = new DailyTrackingTrendPoint();
                Date entryDate = rs.getDate("date");
                if (entryDate != null) {
                    point.setDate(entryDate.toLocalDate());
                }
                point.setAverageMood(getNullableDouble(rs, "avg_mood"));
                point.setAverageStress(getNullableDouble(rs, "avg_stress"));
                points.add(point);
            }
        }
        return points;
    }

    private Integer findExistingEntryId(int userId, LocalDate date) {
        String query = """
                SELECT id
                FROM daily_tracking
                WHERE user_id = ? AND `date` = ?
                ORDER BY created_at DESC
                LIMIT 1
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        } catch (SQLException e) {
            System.err.println("Error looking up daily tracking entry: " + e.getMessage());
            return null;
        }
    }

    private boolean insertEntry(DailyTrackingEntry entry) {
        String query = """
                INSERT INTO daily_tracking (
                    user_id,
                    `date`,
                    mood,
                    stress,
                    activities,
                    anxiety_level,
                    focus_level,
                    motivation_level,
                    social_interaction_level,
                    sleep_hours,
                    energy_level,
                    symptoms,
                    medication_taken,
                    appetite_level,
                    water_intake,
                    physical_activity_level,
                    created_at,
                    updated_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            bindEntryParameters(stmt, entry, false);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting daily tracking entry: " + e.getMessage());
            return false;
        }
    }

    private boolean updateEntry(int entryId, DailyTrackingEntry entry) {
        String query = """
                UPDATE daily_tracking
                SET mood = ?,
                    stress = ?,
                    activities = ?,
                    anxiety_level = ?,
                    focus_level = ?,
                    motivation_level = ?,
                    social_interaction_level = ?,
                    sleep_hours = ?,
                    energy_level = ?,
                    symptoms = ?,
                    medication_taken = ?,
                    appetite_level = ?,
                    water_intake = ?,
                    physical_activity_level = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            bindEntryParameters(stmt, entry, true);
            stmt.setInt(15, entryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating daily tracking entry: " + e.getMessage());
            return false;
        }
    }

    private void bindEntryParameters(PreparedStatement stmt, DailyTrackingEntry entry, boolean updateOnly) throws SQLException {
        int index = 1;
        if (!updateOnly) {
            stmt.setInt(index++, entry.getUserId());
            stmt.setDate(index++, Date.valueOf(entry.getDate()));
        }

        setNullableInt(stmt, index++, entry.getMood());
        setNullableInt(stmt, index++, entry.getStress());
        setNullableString(stmt, index++, entry.getActivities());
        setNullableInt(stmt, index++, entry.getAnxietyLevel());
        setNullableInt(stmt, index++, entry.getFocusLevel());
        setNullableInt(stmt, index++, entry.getMotivationLevel());
        setNullableInt(stmt, index++, entry.getSocialInteractionLevel());
        setNullableInt(stmt, index++, entry.getSleepHours());
        setNullableInt(stmt, index++, entry.getEnergyLevel());
        setNullableString(stmt, index++, entry.getSymptoms());
        setNullableBoolean(stmt, index++, entry.getMedicationTaken());
        setNullableInt(stmt, index++, entry.getAppetiteLevel());
        setNullableInt(stmt, index++, entry.getWaterIntake());
        setNullableInt(stmt, index++, entry.getPhysicalActivityLevel());
    }

    private DailyTrackingEntry mapEntry(ResultSet rs) throws SQLException {
        DailyTrackingEntry entry = new DailyTrackingEntry();
        entry.setId(rs.getInt("id"));
        entry.setUserId(rs.getInt("user_id"));
        entry.setUserName(rs.getString("full_name"));

        Date entryDate = rs.getDate("date");
        if (entryDate != null) {
            entry.setDate(entryDate.toLocalDate());
        }

        entry.setMood(getNullableInt(rs, "mood"));
        entry.setStress(getNullableInt(rs, "stress"));
        entry.setActivities(rs.getString("activities"));
        entry.setAnxietyLevel(getNullableInt(rs, "anxiety_level"));
        entry.setFocusLevel(getNullableInt(rs, "focus_level"));
        entry.setMotivationLevel(getNullableInt(rs, "motivation_level"));
        entry.setSocialInteractionLevel(getNullableInt(rs, "social_interaction_level"));
        entry.setSleepHours(getNullableInt(rs, "sleep_hours"));
        entry.setEnergyLevel(getNullableInt(rs, "energy_level"));
        entry.setSymptoms(rs.getString("symptoms"));
        entry.setMedicationTaken(getNullableBoolean(rs, "medication_taken"));
        entry.setAppetiteLevel(getNullableInt(rs, "appetite_level"));
        entry.setWaterIntake(getNullableInt(rs, "water_intake"));
        entry.setPhysicalActivityLevel(getNullableInt(rs, "physical_activity_level"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            entry.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            entry.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return entry;
    }

    private DailyTrackingStats mapStats(ResultSet rs) throws SQLException {
        DailyTrackingStats stats = new DailyTrackingStats();
        stats.setEntryCount(rs.getInt("entry_count"));
        stats.setAverageMood(getNullableDouble(rs, "avg_mood"));
        stats.setAverageStress(getNullableDouble(rs, "avg_stress"));
        stats.setAverageEnergyLevel(getNullableDouble(rs, "avg_energy"));
        stats.setAverageSleepHours(getNullableDouble(rs, "avg_sleep"));
        stats.setAverageWaterIntake(getNullableDouble(rs, "avg_water"));
        stats.setAveragePhysicalActivityLevel(getNullableDouble(rs, "avg_activity"));
        return stats;
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private Boolean getNullableBoolean(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : rs.getBoolean(column);
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
            return;
        }
        stmt.setInt(index, value);
    }

    private void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            stmt.setNull(index, Types.VARCHAR);
            return;
        }
        stmt.setString(index, value.trim());
    }

    private void setNullableBoolean(PreparedStatement stmt, int index, Boolean value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.BOOLEAN);
            return;
        }
        stmt.setBoolean(index, value);
    }
}
