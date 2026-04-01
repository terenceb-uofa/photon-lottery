package com.example.getoutthere.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for lottery sampling logic.
 */
public class LotteryUtils {

    /**
     * Randomly samples a specified number of entrants from the waitlist.
     *
     * @param entrants List of entrants to sample from.
     * @param capacity Maximum number of entrants to select.
     * @return List of randomly selected entrants.
     */
    public static List<Map<String, String>> drawLottery(List<Map<String, String>> entrants, int capacity) {
        if (entrants == null || entrants.isEmpty() || capacity <= 0) {
            return new ArrayList<>();
        }

        int sampleSize = Math.min(capacity, entrants.size());

        List<Map<String, String>> shuffled = new ArrayList<>(entrants);
        Collections.shuffle(shuffled);

        return new ArrayList<>(shuffled.subList(0, sampleSize));
    }
}