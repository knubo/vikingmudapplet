package no.knubo.mud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

class History {
	private static final int MAX_HISTORY = 100;

	private LinkedList history = new LinkedList();
	private ListIterator iterator;

	private int discard_count;

	void addHistroy(String command) {
		if(discard_count-- > 0) {
			return;
		}
		
		/* Don't keep adding similar entries or blanks */
		if (command.trim().length() == 0
				|| history.size() > 0 && history.getLast().equals(command)) {
			iterator = history.listIterator();
			return;
		}

		history.addFirst(command);

		if (history.size() > MAX_HISTORY) {
			history.removeLast();
		}
		iterator = history.listIterator();
	}

	String next() {
		if (iterator.hasPrevious()) {
			return iterator.previous().toString();
		}
		return "";
	}

	String previous() {
		if (iterator.hasNext()) {
			return iterator.next().toString();
		}
		return "";
	}

	public String allHistory() {
		ArrayList res = new ArrayList(history);
		Collections.reverse(res);
		return "Recorded history:\n" + res.toString();
	}
	
	void init_discard_count() {
		discard_count = 2;
	}
}
