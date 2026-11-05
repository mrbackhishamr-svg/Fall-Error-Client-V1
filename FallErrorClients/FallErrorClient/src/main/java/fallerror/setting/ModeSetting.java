package fallerror.setting;

public class ModeSetting extends Setting<String> {
    public final String[] modes;
    public ModeSetting(String name, String value, String... modes) { super(name, value); this.modes = modes; }
}
