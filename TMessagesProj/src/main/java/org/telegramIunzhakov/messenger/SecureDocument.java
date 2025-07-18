package org.telegramIunzhakov.messenger;

import org.telegramIunzhakov.tgnet.TLObject;
import org.telegramIunzhakov.tgnet.TLRPC;

public class SecureDocument extends TLObject {

    public SecureDocumentKey secureDocumentKey;
    public TLRPC.TL_secureFile secureFile;
    public String path;
    public TLRPC.TL_inputFile inputFile;
    public byte[] fileSecret;
    public byte[] fileHash;
    public int type;

    public SecureDocument(SecureDocumentKey key, TLRPC.TL_secureFile file, String p, byte[] fh, byte[] secret) {
        secureDocumentKey = key;
        secureFile = file;
        path = p;
        fileHash = fh;
        fileSecret = secret;
    }
}
