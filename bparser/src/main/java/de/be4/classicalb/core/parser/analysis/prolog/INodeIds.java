package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.node.Node;

/**
 * Interface for assigning file numbers and unique identifiers to AST nodes and looking them up later.
 */
public interface INodeIds {
	/**
	 * <p>Assign a file number and unique IDs to all elements of the syntax tree.</p>
	 * <p>We are planning to remove the unique ID mechanism. Some implementations of this method may only set the file number and not assign any unique IDs.</p>
	 * 
	 * @param fileNumber the file number which will be assigned to {@code node} and its child nodes
	 * @param node the node from which to start assigning IDs
	 */
    void assignIdentifiers(int fileNumber, Node node);
	
	/**
	 * <p>Looks up the ID of the given node.</p>
	 * <p>We are planning to remove the unique ID mechanism. Some implementations of this method may always return {@code null}.</p>
	 * 
	 * @param node the node of which we want to have the ID
	 * @return the ID of the node, or {@code null} if no ID is available
	 */
    Integer lookup(Node node);
	
	/**
	 * Looks up the file number of the given node.
	 * 
	 * @param node the node of which we want to have the file number
	 * @return the file number of the node, or {@code -1} if no file number was assigned
	 */
    int lookupFileNumber(Node node);
}
