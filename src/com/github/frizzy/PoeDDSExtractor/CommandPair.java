package com.github.frizzy.PoeDDSExtractor;

import org.apache.commons.exec.DefaultExecuteResultHandler;

public class CommandPair < DefaultExecutor, ByteArrayOutputStream > {

    public DefaultExecuteResultHandler rh;

    public ByteArrayOutputStream bs;

    public CommandPair ( DefaultExecuteResultHandler de, ByteArrayOutputStream bs ) {
        this.rh = de;
        this.bs = bs;
    }
}
