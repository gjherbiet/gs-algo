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
package org.graphstream.algorithm.measure;

import static org.graphstream.algorithm.Toolkit.communities;

import java.util.HashMap;
import java.util.HashSet;

import org.graphstream.graph.Node;

/**
 * Computes and updates a relative measure based on the comparison between the
 * current community assignment and a reference assignment on a given graph.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public abstract class CommunityRelativeMeasure extends CommunityMeasure {

	/**
	 * Name of the attribute marking the reference communities.
	 */
	protected String referenceMarker;

	/**
	 * All reference communities indexed by their marker value.
	 */
	protected HashMap<Object, HashSet<Node>> referenceCommunities;

	public CommunityRelativeMeasure(String marker) {
		super(marker);
		this.referenceMarker = "label";
	}

	/**
	 * New comparative measure using "marker" as attribute name for each node
	 * current community assignment and "referenceMarker" as attribute name for
	 * each node reference assignment.
	 * 
	 * @param marker
	 *            Current community assignment attribute name
	 * @param referenceMarker
	 *            Reference community assignment attribute name
	 */
	public CommunityRelativeMeasure(String marker, String referenceMarker) {
		super(marker);
		this.referenceMarker = referenceMarker;
	}

	// /**
	// * New comparative measure using the results of the specified algorithm as
	// * current community assignment and "referenceMarker" as attribute name
	// for
	// * each node reference assignment.
	// *
	// * @param algo
	// * Algorithm which results will be used for measurement.
	//  * @param referenceMarker
	//  *            Reference community assignment attribute name
	// */
	// public CommunityMeasure(CommunityAlgorithm algo) {
	// this.marker = algo.getMarker();
	// this.referenceMarker = referenceMarker;
	// }

	@Override
	public abstract void compute();

	@Override
	protected void initialize() {
		super.initialize();
		referenceCommunities = communities(graph, referenceMarker);
	}

	@Override
	public void nodeAdded(String graphId, long timeId, String nodeId) {
		super.nodeAdded(graphId, timeId, nodeId);

		Node n = graph.getNode(nodeId);
		assignNode(nodeId, n.getAttribute(referenceMarker),
				referenceCommunities);
	}

	@Override
	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		super.nodeRemoved(graphId, timeId, nodeId);

		Node n = graph.getNode(nodeId);
		unassignNode(nodeId, n.getAttribute(referenceMarker),
				referenceCommunities);
	}

	@Override
	public void nodeAttributeChanged(String graphId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		super.nodeAttributeChanged(graphId, timeId, nodeId, attribute,
				oldValue, newValue);

		if (attribute.equals(referenceMarker) && oldValue != newValue) {
			unassignNode(nodeId, oldValue, referenceCommunities);
			assignNode(nodeId, newValue, referenceCommunities);
		}
	}
}
