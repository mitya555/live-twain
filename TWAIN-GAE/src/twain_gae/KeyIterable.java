package twain_gae;

import java.util.Iterator;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
	 
public class KeyIterable implements Iterable<Key> {
	Iterable<Entity> iterable;
	public KeyIterable(Iterable<Entity> iterable) {
		this.iterable = iterable;
	}
	@Override
	public Iterator<Key> iterator() {
		return new KeyIterator(iterable.iterator());
	}
	protected class KeyIterator implements Iterator<Key> {
		Iterator<Entity> it;
		public KeyIterator(Iterator<Entity> it) {
			this.it = it;
		}
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}
		@Override
		public Key next() {
			return it.next().getKey();
		}
		@Override
		public void remove() {
			it.remove();
		}
	}
}
