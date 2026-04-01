package com.example.getoutthere.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for LotteryUtils.
 */
public class LotteryUtilsTest {

    /**
     * Helper method to create a dummy entrant.
     */
    private Map<String, String> createEntrant(String deviceId, String name) {
        Map<String, String> entrant = new HashMap<>();
        entrant.put("deviceId", deviceId);
        entrant.put("name", name);
        entrant.put("email", name.toLowerCase() + "@test.com");
        entrant.put("phone", "7801234567");
        return entrant;
    }

    /**
     * Helper method to create a list of dummy entrants.
     */
    private List<Map<String, String>> createEntrantList(int count) {
        List<Map<String, String>> entrants = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            entrants.add(createEntrant("device00" + i, "Entrant " + i));
        }
        return entrants;
    }

    @Test
    public void testDrawLottery_returnsCorrectSize() {
        // 5 entrants, capacity 3 → should return 3
        List<Map<String, String>> entrants = createEntrantList(5);
        List<Map<String, String>> result = LotteryUtils.drawLottery(entrants, 3);
        assertEquals(3, result.size());
    }

    @Test
    public void testDrawLottery_capacityLargerThanWaitlist() {
        // 3 entrants, capacity 10 → should return all 3
        List<Map<String, String>> entrants = createEntrantList(3);
        List<Map<String, String>> result = LotteryUtils.drawLottery(entrants, 10);
        assertEquals(3, result.size());
    }

    @Test
    public void testDrawLottery_emptyWaitlist() {
        // 0 entrants → should return empty list
        List<Map<String, String>> entrants = new ArrayList<>();
        List<Map<String, String>> result = LotteryUtils.drawLottery(entrants, 3);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDrawLottery_zeroCapacity() {
        // capacity 0 → should return empty list
        List<Map<String, String>> entrants = createEntrantList(5);
        List<Map<String, String>> result = LotteryUtils.drawLottery(entrants, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDrawLottery_nullWaitlist() {
        // null entrants → should return empty list
        List<Map<String, String>> result = LotteryUtils.drawLottery(null, 3);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDrawLottery_selectedEntrantsAreFromWaitlist() {
        // All selected entrants should be from the original waitlist
        List<Map<String, String>> entrants = createEntrantList(5);
        List<Map<String, String>> result = LotteryUtils.drawLottery(entrants, 3);
        assertTrue(entrants.containsAll(result));
    }

    @Test
    public void testDrawLottery_noDuplicates() {
        // Selected entrants should have no duplicates
        List<Map<String, String>> entrants = createEntrantList(5);
        List<Map<String, String>> result = LotteryUtils.drawLottery(entrants, 3);
        long uniqueCount = result.stream()
                .map(e -> e.get("deviceId"))
                .distinct()
                .count();
        assertEquals(result.size(), uniqueCount);
    }
}