package com.github.frizzy.PoeDDSExtractor.Command;

public class CommandArg < String, Boolean > {

    public String arg;

    public Boolean quoting;

    public CommandArg ( String arg, Boolean quoting ) {
        this.arg = arg;
        this.quoting = quoting;
    }
}
