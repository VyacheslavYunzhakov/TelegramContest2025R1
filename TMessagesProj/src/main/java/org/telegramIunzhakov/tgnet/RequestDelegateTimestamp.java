package org.telegramIunzhakov.tgnet;

public interface RequestDelegateTimestamp {
    void run(TLObject response, TLRPC.TL_error error, long responseTime);
}
