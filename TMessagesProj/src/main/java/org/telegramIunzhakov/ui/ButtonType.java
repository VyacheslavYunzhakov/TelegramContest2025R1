package org.telegramIunzhakov.ui;

public enum ButtonType {
    JOIN(1),
    MESSAGE(2),
    DISCUSS(3),
    MUTE(4),
    CALL(5),
    VIDEO(6),
    VOICE_CHAT(7),
    LIVE_STREAM(8),
    SHARE(9),
    LEAVE(10),
    GIFT(11),
    STOP(12),
    REPORT(13),
    STORY(14);

    private final int priority;

    ButtonType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
