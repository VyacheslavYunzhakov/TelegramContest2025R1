package org.telegramIunzhakov.messenger;

public interface GenericProvider<F, T> {
    T provide(F obj);
}
