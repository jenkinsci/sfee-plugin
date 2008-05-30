package hudson.plugins.sfee;

public interface Computable<A, V> {
	V compute(A arg) throws InterruptedException;
}
