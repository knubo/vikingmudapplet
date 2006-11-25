package no.knubo.mud;

interface Alias {

	/**
	 * Gets contents for alias.
	 * @param action The action that misses the alias.
	 * @return The expaned alias.
	 */
	public String getAlias(String action);
	/**
	 * Adds alias.
	 * @param action The command for the alias.
	 * @param value The value for the alias.
	 * @param checkForDuplicates TODO
	 * @return True if the alias was added, false if it already exists.
	 */
	public boolean addAlias(String action, String value, boolean checkForDuplicates);
	/**
	 * Adds alias, and replaces existing alias if there. The format is: #alias <action> <value>.
	 * @param raw The string containing the command.
	 * @return True if syntax was accepted and alias created.
	 */
	public boolean addAlias(String raw);
	
	/**
	 * Checks if the alias window is shown.
	 * @return True if visible, false is not.
	 */
	public boolean isVisible();
	
	/**
	 * Sets the alias window visibility.
	 * @param b true is visible, false is not.
	 */
	public void setVisible(boolean b);
}