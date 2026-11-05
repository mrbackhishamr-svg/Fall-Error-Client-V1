package fallerror.setting;

public class NumberSetting extends Setting<Double> {
    public final double min, max;
    public final String suffix;
    public NumberSetting(String name, double value, double min, double max, String suffix) {
        super(name, value); this.min = min; this.max = max; this.suffix = suffix;
    }
    public void setClamped(double v) { value = Math.max(min, Math.min(max, v)); }
    public double percent() { return (value - min) / (max - min); }
}
