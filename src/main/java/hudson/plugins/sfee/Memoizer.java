package hudson.plugins.sfee;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Memoizer<A, V> implements Computable<A, V> {
	private final ConcurrentMap<A, ExpiringFutureTask<V>> cache = new ConcurrentHashMap<A, ExpiringFutureTask<V>>();
	private final Computable<A, V> c;

	public Memoizer(Computable<A, V> c) {
		this.c = c;
	}

	public V compute(final A arg) throws InterruptedException {
		while (true) {
			ExpiringFutureTask<V> f = cache.get(arg);
			if (f != null && f.hasExpired()) {
				cache.remove(arg);
				f = null;
			}
			if (f == null) {
				Callable<V> eval = new Callable<V>() {
					public V call() throws InterruptedException {
						return c.compute(arg);
					}
				};
				ExpiringFutureTask<V> ft = new ExpiringFutureTask<V>(eval);
				f = cache.putIfAbsent(arg, ft);
				if (f == null) {
					f = ft;
					ft.run();
				}
			}
			try {
				return f.get();
			} catch (CancellationException e) {
				cache.remove(arg, f);
			} catch (ExecutionException e) {
				// Kabutz: this is my addition to the code...
				try {
					cache.remove(arg, f);
					throw e.getCause();
				} catch (RuntimeException ex) {
					throw ex;
				} catch (Error ex) {
					throw ex;
				} catch (Throwable t) {
					throw new IllegalStateException("Not unchecked", t);
				}
			}
		}
	}

	private static class ExpiringFutureTask<V>  extends FutureTask<V> {
		private long creationTime = System.currentTimeMillis();
		public ExpiringFutureTask(Callable<V> callable) {
			super(callable);
		}
		public boolean hasExpired() {
			return System.currentTimeMillis() - creationTime > 60000;
		}
	}
	
}
