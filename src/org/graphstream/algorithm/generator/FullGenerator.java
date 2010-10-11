/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm.generator;

/**
 * Full graph generator.
 * 
 * <p>
 * Probably not very useful, still sometimes needed. This generator creates
 * fully connected graphs of any size. Calling {@link #begin()} put one unique
 * node in the graph, then {@link #nextEvents()} will add a new node each time
 * it is called.
 * </p>
 * 
 * <p>
 * This generator has the ability to add randomly chosen numerical values on
 * arbitrary attributes on edges or nodes of the graph, and to randomly choose a
 * direction for edges.
 * </p>
 * 
 * <p>
 * A list of attributes can be given for nodes and edges. In this case each new
 * node or edge added will have this attribute and the value will be a randomly
 * chosen number. The range in which these numbers are chosen can be specified.
 * </p>
 * 
 * <p>
 * By default, edges are not oriented. It is possible to ask orientation, in
 * which case the direction is chosen randomly.
 * </p>
 * 
 * @since 2007
 */
public class FullGenerator extends BaseGenerator {
	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;

	/**
	 * New full graph generator. By default no attributes are added to nodes and
	 * edges, and edges are not directed.
	 */
	public FullGenerator() {
		super();
		keepNodesId = true;
	}

	/**
	 * New full graph generator.
	 * 
	 * @param directed
	 *            Are edge directed?
	 * @param randomlyDirectedEdges
	 *            randomly direct generated edges.
	 */
	public FullGenerator(boolean directed, boolean randomlyDirectedEdges) {
		super(directed, randomlyDirectedEdges);
		keepNodesId = true;
	}

	/**
	 * New full graph generator.
	 * 
	 * @param directed
	 *            Are edge directed?.
	 * @param randomlyDirectedEdges
	 *            randomly direct generated edges.
	 * @param nodeAttribute
	 *            put an attribute by that name on each node with a random
	 *            numeric value.
	 * @param edgeAttribute
	 *            put an attribute by that name on each edge with a random
	 *            numeric value.
	 */
	public FullGenerator(boolean directed, boolean randomlyDirectedEdges,
			String nodeAttribute, String edgeAttribute) {
		super(directed, randomlyDirectedEdges, nodeAttribute, edgeAttribute);
		keepNodesId = true;
	}

	/**
	 * Begin the generator by adding a node.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		String id = Integer.toString(nodeNames++);

		addNode(id);
	}

	/**
	 * Add a new node and connect it with all others.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		String id = Integer.toString(nodeNames++);

		addNode(id);

		for (String otherId : nodes) {
			if (otherId != id) // We can compare refs safely here.
				addEdge(null, id, otherId);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	public void end() {

	}
}