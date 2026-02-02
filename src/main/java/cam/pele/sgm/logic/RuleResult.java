package cam.pele.sgm.logic;

public record RuleResult(Type type, int timer) {

    public enum Type {
        ALLOWED,
        DENIED,
        ALLOW_RESPAWN,
        ALLOW_DECAY,
        ALLOW_NO_DROP
    }

    // Static constants for simple results
    public static final RuleResult ALLOWED = new RuleResult(Type.ALLOWED, 0);
    public static final RuleResult DENIED = new RuleResult(Type.DENIED, 0);
    public static final RuleResult ALLOW_NO_DROP = new RuleResult(Type.ALLOW_NO_DROP, 0);
    public static final RuleResult ALLOW_DECAY = new RuleResult(Type.ALLOW_DECAY, 0);
    public static final RuleResult ALLOW_RESPAWN = new RuleResult(Type.ALLOW_RESPAWN, 0);

    // Factory methods for results with timers
    public static RuleResult allowRespawn(int timer) {
        return new RuleResult(Type.ALLOW_RESPAWN, timer);
    }

    public static RuleResult allowDecay(int timer) {
        return new RuleResult(Type.ALLOW_DECAY, timer);
    }
}
