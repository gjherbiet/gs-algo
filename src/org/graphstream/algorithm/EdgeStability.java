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
 * Project copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 * 
 * This file is copyright 2010
 * 	Guillaume-Jean Herbiet
 */
package org.graphstream.algorithm;

import java.util.HashMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.stream.SinkAdapter;

/**
 * This class provides a set of edge stability stability measures (age,
 * appearances, cumulated age, average age, volatility).
 * 
 * Each value is updated when required and stored to a dedicated edge attribute
 * field. All fields are prefixed by "stability." :
 * <ul>
 * <li><i>stability.age</i>: stores the current age of the link</li>
 * <li><i>stability.cumulated_age</i>: stores the cumulated age of the link</li>
 * <li><i>stability.average_age</i>: stores the average age of the link</li>
 * <li><i>stability.appearances</i>: stores the number of appearances of the
 * link</li>
 * <li><i>stability.volatility</i>: stores the volatility of the link</li>
 * </ul>
 * 
 * @reference Pigné, Y. <b>
 *            "Modélisation et traitement décentralisé des graphes dynamiques.
 *            Application aux réseaux mobiles ad hoc", 
 *            <i>PhD Thesis</i>, Université du Havre, 2008.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class EdgeStability extends SinkAdapter implements Algorithm {

	/**
	 * Graph being used on computation.
	 */
	protected Graph graph;

	/**
	 * Current age (i.e. number of consecutive iterations "on") of each edge
	 */
	protected HashMap<String, Double> age;

	/**
	 * Cumulated age (i.e. "on" time of each edge since the computation began.
	 */
	protected HashMap<String, Double> cumulatedAge;

	/**
	 * Number of appearances (i.e. "off-to-on" switches) of each edge.
	 */
	protected HashMap<String, Double> appearances;

	/**
	 * Construct a new EdgeStability measurement algorithm, without attaching it
	 * to a graph.
	 */
	public EdgeStability() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;

		age = new HashMap<String, Double>();
		cumulatedAge = new HashMap<String, Double>();
		appearances = new HashMap<String, Double>();
	}

	public void compute() {

	}

	public void stepBegins(String sourceId, long timeId, double step) {

		/*
		 * Increase the age and cumulated age of all "on" links
		 */
		for (String edgeId : age.keySet()) {
			age.put(edgeId, age.get(edgeId) + 1.0);
			cumulatedAge.put(edgeId, cumulatedAge.get(edgeId) + 1.0);
			updateEdgeStability(graph.getEdge(edgeId));
		}
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {

		/*
		 * Reset the age of this fresh age
		 */
		age.put(edgeId, 0.0);

		/*
		 * Initiate a cumulated age entry for this edge if first appearing
		 */
		if (!cumulatedAge.containsKey(edgeId)) {
			cumulatedAge.put(edgeId, 0.0);
		}

		/*
		 * Initiate or increment the number of appearances for this edge
		 */
		if (!appearances.containsKey(edgeId)) {
			appearances.put(edgeId, 1.0);
		} else {
			appearances.put(edgeId, appearances.get(edgeId) + 1.0);
		}
		updateEdgeStability(graph.getEdge(edgeId));
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {

		/*
		 * Delete the age entry for this edge
		 */
		age.remove(edgeId);

	}

	protected void updateEdgeStability(Edge e) {
		e.setAttribute("stability.age", age.get(e.getId()));
		e.setAttribute("stability.cumulated_age", cumulatedAge.get(e.getId()));
		e.setAttribute("stability.appearances", appearances.get(e.getId()));
		e.setAttribute("stability.average_age", cumulatedAge.get(e.getId())
				/ appearances.get(e.getId()));
		e.setAttribute("stability.volatility", appearances.get(e.getId())
				/ cumulatedAge.get(e.getId()));
	}
}
