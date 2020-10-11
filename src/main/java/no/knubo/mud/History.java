package no.knubo.mud;

import java.util.*;

class History {
	private static final int MAX_HISTORY = 100;

	private LinkedList<String> history = new LinkedList<>();
	private ListIterator<String> iterator;

	void addHistroy(String command) {
		
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

	public String search(StringBuilder criteria) {
		if(criteria.length() == 0) {
			return null;
		}
		Optional<String> match = history.stream().filter(s -> s.contains(criteria)).findFirst();

		return match.orElse(null);
	}

	public String allHistory() {
		ArrayList res = new ArrayList(history);
		Collections.reverse(res);
		return "Recorded history:\n" + res.toString();
	}
	
}
