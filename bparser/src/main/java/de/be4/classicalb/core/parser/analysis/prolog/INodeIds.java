package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.node.Node;

/**
 * Interface for assigning file numbers to AST nodes and looking them up later.
 */
public interface INodeIds {
	/**
	 * Assign a file number to all elements of the syntax tree.
	 * 
	 * @param fileNumber the file number which will be assigned to {@code node} and its child nodes
	 * @param node the node from which to start assigning IDs
	 */
	void assignIdentifiers(int fileNumber, Node node);
	
	/**
	 * Looks up the ID of the given node.
	 * 
	 * @param node the node of which we want to have the ID
	 * @return the ID of the node, or {@code null} if no ID is available
	 * @deprecated The unique ID mechanism is deprecated and will be removed in the future.
	 *     There is no planned replacement.
	 *     Expect this method to always return {@code null} now.
	 */
	@Deprecated
	Integer lookup(Node node);
	
	/**
	 * Looks up the file number of the given node.
	 * 
	 * @param node the node of which we want to have the file number
	 * @return the file number of the node, or {@code -1} if no file number was assigned
	 */
	int lookupFileNumber(Node node);
}
