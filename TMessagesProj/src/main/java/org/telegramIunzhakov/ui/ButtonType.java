package org.telegramIunzhakov.ui;

public enum ButtonType {
    JOIN(1),
    MESSAGE(2),
    MUTE(3),
    CALL(4),
    VIDEO(5),
    GIFT(6),
    SHARE(7),
    STOP(8),
    VOICE_CHAT(9),
    LIVE_STREAM(10),
    LEAVE(11),
    REPORT(12),
    DISCUSS(13),
    STORY(14); // Добавлен новый тип

    private final int priority;

    ButtonType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
