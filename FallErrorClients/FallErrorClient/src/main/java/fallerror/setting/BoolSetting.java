package fallerror.setting;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, boolean value) { super(name, value); }
    public void toggle() { value = !value; }
}
