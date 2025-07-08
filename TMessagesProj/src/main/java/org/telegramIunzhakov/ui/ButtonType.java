package org.telegramIunzhakov.ui;

public enum ButtonType {
    JOIN(1),
    MESSAGE(2),
    DISCUSS(3),
    VOICE_CHAT(4),
    LIVE_STREAM(5),
    MUTE(6),
    CALL(7),
    VIDEO(8),
    SHARE(9),
    LEAVE(10),
    STOP(11),
    REPORT(12),
    STORY(13),
    GIFT(14);

    private final int priority;

    ButtonType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
