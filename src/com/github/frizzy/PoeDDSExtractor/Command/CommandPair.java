package com.github.frizzy.PoeDDSExtractor.Command;

import org.apache.commons.exec.DefaultExecuteResultHandler;

/**
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 * @param <DefaultExecuteResultHandler > Handler assigned to a DefaultExecutor.
 * @param <ByteArrayOutputStream> Out stream assigned to a PumpStreamHandler for an executor.
 */
public class CommandPair < DefaultExecuteResultHandler , ByteArrayOutputStream > {

    /**
     * Result handler.
     */
    public DefaultExecuteResultHandler rh;

    /**
     * Out Stream.
     */
    public ByteArrayOutputStream bs;

    public CommandPair ( DefaultExecuteResultHandler rh, ByteArrayOutputStream bs ) {
        this.rh = rh;
        this.bs = bs;
    }
}
