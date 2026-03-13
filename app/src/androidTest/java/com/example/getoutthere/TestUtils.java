package com.example.getoutthere;

import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Utility class for common Espresso Matchers and test helpers.
 */
public class TestUtils {

    /**
     * Helper to select specific occurrence of a view when multiple views match the same matcher.
     * Use case: Selecting a 'DELETE' button in a dynamic list.
     *
     * @param matcher The matcher to find (e.g. withText("DELETE"))
     * @param index   The index of the occurrence (0 for first, 1 for second, etc.)
     * @return A Matcher that targets the specific occurrence.
     */
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: " + index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}