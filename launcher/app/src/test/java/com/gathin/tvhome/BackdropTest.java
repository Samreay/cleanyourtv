package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** The background_style setting: known names (any case, spaces trimmed) pass, anything else falls back to glow. */
public class BackdropTest {

    @Test
    public void knownStylesPassThrough() {
        assertEquals(Backdrop.GLOW, Backdrop.normalise("glow"));
        assertEquals(Backdrop.AURORA, Backdrop.normalise("aurora"));
        assertEquals(Backdrop.HORIZON, Backdrop.normalise(" Horizon "));
    }

    @Test
    public void unknownOrMissingFallsBackToGlow() {
        assertEquals(Backdrop.GLOW, Backdrop.normalise(null));
        assertEquals(Backdrop.GLOW, Backdrop.normalise(""));
        assertEquals(Backdrop.GLOW, Backdrop.normalise("dots"));
    }
}
