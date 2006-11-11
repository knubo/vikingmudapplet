package no.knubo.mud;

class History {
	private static final int MAX_HISTORY = 100;
	String[] history = new String[MAX_HISTORY];
	int pos;
	int historypos;
	boolean wrapped = false;
	boolean first = false;

	void addHistroy(String command) {
		/* Do not recall similar commands repeated. */
		if (history[pos] != null && history[pos].equals(command)) {
			return;
		}

		history[++pos] = command;
		if (pos >= MAX_HISTORY) {
			pos = 0;
		}
		historypos = pos;
		first = false;
	}

	String previous() {

		if(!first) {
			first = true;
			return history[historypos];
		}
		
		final int maxpos = MAX_HISTORY - 1;

		/* Check if we can wrap */
		if ((historypos - 1) < 0 && history[maxpos] == null) {
			first = false;
			return "";
		}

		/* Okay we can wrap if we need to, so we go ahead */
		historypos--;

		if (wrapped && historypos < (pos + 1)) {
			return history[pos + 1];
		}

		if (historypos < 0) {
			historypos = maxpos;
			wrapped = true;
		}

		return history[historypos];
	}

	String next() {
		if(!first) {
			first = true;
			return history[historypos];
		}
		
		/* Check if There is still somethign to show. If not written yet we don't display anything. */
		if ((historypos + 1) < MAX_HISTORY && history[historypos+1] == null) {
			first = false;
			return "";
		}

		historypos++;
		
		if (historypos > pos && !wrapped) {
			historypos = pos;
		}
		if (historypos >= MAX_HISTORY || history[historypos] == null) {
			historypos = 0;
			wrapped = false;
		}

		return history[historypos];
	}
}
