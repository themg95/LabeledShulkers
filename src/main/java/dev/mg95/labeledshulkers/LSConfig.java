package dev.mg95.labeledshulkers;

import dev.mg95.colon3lib.config.Config;
import dev.mg95.colon3lib.config.Option;

public class LSConfig extends Config {
    public LSConfig() {
        super.init(this, "labeledshulkers");
    }

    @Option
    public boolean enableHolograms = true;

    @Option
    public boolean showNonCustomNames = false;

}
