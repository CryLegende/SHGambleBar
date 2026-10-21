package org.Cry_Legende.sHGambleBar.game;

public enum GambleResult {

    LOSE,
    WIN,
    SUPER_WIN;

    public boolean isWin() {
        return this != LOSE;
    }
}
