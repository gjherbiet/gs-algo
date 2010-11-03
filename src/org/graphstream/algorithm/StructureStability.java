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
 *  Guillaume-Jean Herbiet
 */
package org.graphstream.algorithm;

import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.moment.*;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

/**
 * @author Guillaume-Jean Herbiet
 * 
 */
public class StructureStability extends SinkAdapter implements Algorithm {

	/**
	 * Graph being used on computation.
	 */
	protected Graph graph;

	protected String structureMarker = "value";

	protected String stabilityMarker = "weight";

	/**
	 * Set to false after {@link #compute()}.
	 */
	protected boolean graphChanged = true;

	/**
	 * All communities stabilities indexed by their marker value.
	 */
	protected HashMap<Object, Mean> stability;

	/**
	 * New structure stability algorithm using the default markers for
	 * structures and stability of edges.
	 */
	public StructureStability() {
	}

	/**
	 * New structure stability algorithm using the given marker for structures
	 * and the default marker for stability of edges.
	 * 
	 * @param marker
	 *            name of the attribute marking the structures.
	 */
	public StructureStability(String marker) {
		this.structureMarker = marker;
	}

	/**
	 * New structure stability algorithm using the given markers for structures
	 * and the stability of edges.
	 * 
	 * @param marker
	 *            name of the attribute marking the structures.
	 * @param stabilityMarker
	 *            name of the attribute marking the edge stability.
	 */
	public StructureStability(String marker, String stabilityMarker) {
		this.structureMarker = marker;
		this.stabilityMarker = stabilityMarker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
		stability = new HashMap<Object, Mean>();
	}

	/**
	 * Sets the structure marker to a non-default value
	 * 
	 * @param marker
	 *            name of the attribute marking the structures.
	 */
	public void setStructureMarker(String marker) {
		this.structureMarker = marker;
	}

	/**
	 * Sets the stability marker to a non-default value
	 * 
	 * @param marker
	 *            name of the attribute marking the edge stability.
	 */
	public void setStabilityMarker(String marker) {
		this.stabilityMarker = marker;
	}

	/**
	 * @comlexity
	 */
	public void compute() {

		if (graphChanged) {
			
			stability.clear();

			/*
			 * Iterate over the edges of the graph
			 */
			for (Edge e : graph.getEdgeSet()) {
				Node u = e.getNode0();
				Node v = e.getNode1();

				/*
				 * Stability value is stored on the edge
				 */
				double value = 0;
				if (e.hasAttribute(stabilityMarker)) {
					value = e.getNumber(stabilityMarker);
				}

				/*
				 * Both ends are in the same structure, add the value of the
				 * link to the results
				 */
				if (u.hasAttribute(structureMarker)
						&& v.hasAttribute(structureMarker)
						&& u.<Object>getAttribute(structureMarker) == v
								.<Object>getAttribute(structureMarker)) {

					// Common structure
					Object structure = u.getAttribute(structureMarker);

					// Put value in the corresponding entry
					if (stability.containsKey(structure)) {
						stability.get(structure).increment(value);
					}
					// Create a new entry for the current structure if required
					else {
						Mean distribution = new Mean();
						distribution.increment(value);
						stability.put(structure, distribution);
					}
				}
			}
			graphChanged = false;
		}
	}

	/**
	 * Computes the stability value of the designed structure
	 * 
	 * @param structure
	 * @return stability
	 */
	public double getStability(Object structure) {
		compute();

		return stability.get(structure).getResult();
	}

	/**
	 * Computes the average structure stability over the network
	 * 
	 * @return stability average stability of all the structures of the network
	 */
	public double average() {
		compute();

		Mean avg = new Mean();
		for (Mean s : stability.values()) {
			avg.increment(s.getResult());
		}
		return avg.getResult();
	}

	/**
	 * Computes the standard deviation of the structure stability over the
	 * network
	 * 
	 * @return stability standard deviation of all the structures of the network
	 */
	public double stddev() {
		compute();

		StandardDeviation stdev = new StandardDeviation();
		for (Mean s : stability.values()) {
			stdev.increment(s.getResult());
		}
		return stdev.getResult();
	}

	/**
	 * Computes the minimum structure stability over the network
	 * 
	 * @return stability minimum stability of all the structures of the network
	 */
	public double min() {
		compute();

		double min = Double.POSITIVE_INFINITY;
		for (Mean s : stability.values()) {
			if (s.getResult() < min)
				min = s.getResult();
		}
		return min;
	}

	/**
	 * Computes the maximum structure stability over the network
	 * 
	 * @return stability maximum stability of all the structures of the network
	 */
	public double max() {
		compute();

		double max = Double.NEGATIVE_INFINITY;
		for (Mean s : stability.values()) {
			if (s.getResult() > max)
				max = s.getResult();
		}
		return max;
	}

	/**
	 * Returns the least stable structure of the network
	 * 
	 * @return structure least stable structure of the network
	 */
	public Object leastStableStructure() {
		compute();

		double min = Double.POSITIVE_INFINITY;
		Object minStructure = null;
		for (Object s : stability.keySet()) {
			if (stability.get(s).getResult() < min) {
				min = stability.get(s).getResult();
				minStructure = s;
			}
		}
		return minStructure;
	}

	/**
	 * Returns the most stable structure of the network
	 * 
	 * @return structure most stable structure of the network
	 */
	public Object mostStableStructure() {
		compute();

		double max = Double.NEGATIVE_INFINITY;
		Object maxStructure = null;
		for (Object s : stability.keySet()) {
			if (stability.get(s).getResult() > max) {
				max = stability.get(s).getResult();
				maxStructure = s;
			}
		}
		return maxStructure;
	}

	/**
	 * Updates the distribution information and returns a string for an easy
	 * display of the results.
	 * 
	 * The string has the following format: [number of structures] [average
	 * stability] [stdev stability] [min stability] ([least stable structure])
	 * [max stability] ([most stable structure])
	 * 
	 * @return a String containing all computed distribution information.
	 */
	public String toString() {
		compute();
		return stability.size() + " " + average() + " " + stddev() + " "
				+ min() + " (" + leastStableStructure() + ") " + max() + " ("
				+ mostStableStructure() + ")";
	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		if (attribute == stabilityMarker) {
			graphChanged = true;
		}
	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		if (attribute == stabilityMarker) {
			graphChanged = true;
		}
	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		if (attribute == stabilityMarker) {
			graphChanged = true;
		}
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		if (attribute == structureMarker) {
			graphChanged = true;
		}
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		if (attribute == structureMarker) {
			graphChanged = true;
		}
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		if (attribute == structureMarker) {
			graphChanged = true;
		}
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		graphChanged = true;
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		graphChanged = true;
	}

	public void graphCleared(String sourceId, long timeId) {
		graphChanged = true;
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		graphChanged = true;
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		graphChanged = true;
	}

	public void stepBegins(String sourceId, long timeId, double step) {
	}

}
