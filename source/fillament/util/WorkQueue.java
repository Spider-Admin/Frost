
package fillament.util;

import java.util.*;

public class WorkQueue {

	List queue;

	public WorkQueue () {
		queue = Collections.synchronizedList(new LinkedList());
	}

	public void add(Object o) {
		queue.add(queue.size(),o);
	}

	public Object next() {
		return queue.remove(0);
	}

	public boolean hasMore() {
		return (queue.size() > 0);
	}
}
