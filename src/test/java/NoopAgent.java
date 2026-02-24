import java.lang.instrument.Instrumentation;

/** No-op Java agent that replaces the incompatible coroutines debug agent. */
public class NoopAgent {
    public static void premain(String args, Instrumentation inst) {
        // intentionally empty
    }
}
