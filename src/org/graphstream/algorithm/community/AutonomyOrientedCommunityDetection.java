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
package org.graphstream.algorithm.community;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math.stat.descriptive.moment.*;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * This class implements an autonomy oriented computing approach to community
 * mining in distributed and dynamic networks.
 * 
 * @reference B.Yang,J.Liu,andD.Liu, "An autonomy-oriented computing approach to
 *            community mining in distributed and dynamic networks," Autonomous
 *            Agents and Multi-Agent Systems, vol. 20, no. 2, pp. 123–157, 2010.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class AutonomyOrientedCommunityDetection extends
		DecentralizedCommunityAlgorithm {

	/**
	 * Views of the different nodes
	 */
	protected TreeMap<Node, TreeMap<Node, Double>> views;

	/**
	 * Assignments corresponding to the views
	 */
	protected TreeMap<Community, Set<Node>> communities;

	/**
	 * Store if the neighborhood of the given node has changed
	 */
	protected HashMap<Node, Boolean> neighborhoodChanged;

	/**
	 * Constants used by the algorithm
	 */
	protected double omega1 = 0.2;
	protected double omega2 = 0.2;
	protected int T = 10;

	/**
	 * 
	 */
	public AutonomyOrientedCommunityDetection() {
		super();
	}

	/**
	 * @param graph
	 */
	public AutonomyOrientedCommunityDetection(Graph graph) {
		super(graph);
	}

	/**
	 * @param graph
	 * @param marker
	 */
	public AutonomyOrientedCommunityDetection(Graph graph, String marker) {
		super(graph, marker);
	}

	/**
	 * 
	 */
	public void init(Graph graph) {
		super.init(graph);
		views = new TreeMap<Node, TreeMap<Node, Double>>(new NodeComparator());
		communities = new TreeMap<Community, Set<Node>>();
		neighborhoodChanged = new HashMap<Node, Boolean>();
	}

	public void compute() {
		super.compute();
		viewsToCommunities();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.community.DecentralizedCommunityAlgorithm#
	 * computeNode(org.graphstream.graph.Node)
	 */
	@Override
	public void computeNode(Node node) {

		// Initialization case
		initView(node);

		int maxS = 0;
		for (int s = 0; s < T; s++) {
			TreeMap<Node, Double> oldView = views.get(node);

			evaluateView(node);
			shrinkView(node);
			enlargeView(node);
			balanceView(node);

			if (oldView.equals(views.get(node))) {
				s = T;
			}
			maxS++;
		}
		System.out.println(viewToString(node) + " (" + maxS + ")");

		// for (Node n : views.get(node).keySet()) {
		// if (!n.hasAttribute(marker)
		// || n.getArray(marker) != node.getAttribute(marker)) {
		// n.setAttribute(marker, node.getAttribute(marker));
		// }
		// }
	}

	protected void initView(Node u) {
		// Real initialization
		if (!views.containsKey(u)) {
			views.put(u, new TreeMap<Node, Double>(new NodeComparator()));
			originateCommunity(u);
		}

		// Set view to initial condition, at first step or if neighborhood
		// changed
		if (views.get(u).isEmpty() || !neighborhoodChanged.containsKey(u)
				|| neighborhoodChanged.get(u)) {

			if (!neighborhoodChanged.containsKey(u)) {
				System.out.println(u.getId() + ": EMPTY!");
			} else if (neighborhoodChanged.get(u)) {
				System.out.println(u.getId() + ": CHANGED!");
			}

			for (Edge e : u.getEnteringEdgeSet()) {
				Node v = e.getOpposite(u);
				views.get(u).put(v, 1.0);

				if (!views.containsKey(v) || views.get(v).isEmpty()) {
					initView(v);
				}
			}
		}
		neighborhoodChanged.put(u, false);
	}

	protected void evaluateView(Node u) {
		// New view for this agent
		TreeMap<Node, Double> newView = new TreeMap<Node, Double>(
				new NodeComparator());

		// Iterate over the node in the current view of this agent
		// and add the similarity value to the new view
		for (Node v : views.get(u).keySet()) {
			newView.put(v, similarity(u, v));
		}

		// Replace the old view by the new view
		views.put(u, newView);

	}

	protected void shrinkView(Node u) {
		// New view for this agent
		TreeMap<Node, Double> newView = new TreeMap<Node, Double>(
				new NodeComparator());

		// Compute mean and stdev of the view weights
		Mean mean = new Mean();
		StandardDeviation stdev = new StandardDeviation();
		for (Double w : views.get(u).values()) {
			mean.increment(w.doubleValue());
			stdev.increment(w.doubleValue());
		}

		// Only put in the new view nodes above the threshold
		for (Node v : views.get(u).keySet()) {
			if (views.get(u).get(v) >= omega1
					* (mean.getResult() + omega2 * stdev.getResult())) {
				newView.put(v, views.get(u).get(v));
			}
		}

		// Replace the old view by the new view
		views.put(u, newView);
	}

	protected void enlargeView(Node u) {
		// New view for this agent
		TreeMap<Node, Double> newView = views.get(u);

		// Pick a random node from u's view
		Iterator<Node> it = views.get(u).keySet().iterator();
		for (int i = 0; i < rng.nextInt(views.get(u).size()); i++) {
			it.next();
		}
		Node r = it.next();

		// Iterate over this random node view
		for (Node v : views.get(r).keySet()) {
			double w = 0.0;
			if (views.get(u).containsKey(v)) {
				w = views.get(u).get(v);
			}
			newView.put(v,
					Math.max(w, views.get(u).get(r) * views.get(r).get(v)));
		}

		// Compute mean and stdev of the view weights of the original node
		Mean mean = new Mean();
		StandardDeviation stdev = new StandardDeviation();
		for (Double w : newView.values()) {
			mean.increment(w.doubleValue());
			stdev.increment(w.doubleValue());
		}

		// Only put in the final view nodes above the threshold
		TreeMap<Node, Double> finalView = new TreeMap<Node, Double>(
				new NodeComparator());
		for (Node v : newView.keySet()) {
			if (newView.get(v) >= omega1
					* (mean.getResult() + omega2 * stdev.getResult())) {
				finalView.put(v, newView.get(v));
			}
		}

		// Replace the old view by the final view
		views.put(u, finalView);
	}

	protected void balanceView(Node u) {
		// New view for this agent
		TreeMap<Node, Double> newView = new TreeMap<Node, Double>(
				new NodeComparator());

		// Iterate over the node in the current view of this agent
		// and add the similarity value to the new view
		for (Node v : views.get(u).keySet()) {
			if (views.get(v).containsKey(u)) {
				newView.put(v,
						(views.get(u).get(v) + views.get(v).get(u)) / 2.0);
			} else {
				newView.put(v, views.get(u).get(v) / 2.0);
			}
		}

		// Replace the old view by the new view
		views.put(u, newView);
	}

	protected String viewToString(Node u) {
		String s = u.getId() + ": [ ";

		for (Node v : views.get(u).keySet()) {
			// s += "(" + v.getId() + ", " + views.get(u).get(v) + ") ";
			s += v.getId() + " ";
		}
		s += "]";
		return s;
	}

	protected void viewsToCommunities() {
		// Store unique assignments
		HashSet<Set<Node>> assignments = new HashSet<Set<Node>>();

		// Fill assignments with different views
		for (TreeMap<Node, Double> view : views.values()) {
			assignments.add(view.keySet());
		}

		// Turn assignments to different communities
		System.out.println("Assignments:");
		for (Set<Node> assignment : assignments) {

			Object community = null;
			for (Node n : assignment) {
				System.out.print(n.getId() + " ");

				if (community == null) {
					if (!n.hasAttribute(marker)
							|| !n.hasAttribute(marker + ".first")) {
						originateCommunity(n);
					}
					community = n.getAttribute(marker);
					n.setAttribute(marker + ".first", 1);
				}
				else {
					n.setAttribute(marker, community);
					n.removeAttribute(marker + ".first");
				}
			}
			System.out.println();
		}
	}

	protected double similarity(Node n1, Node n2) {
		TreeMap<Node, Double> v1 = views.get(n1);
		TreeMap<Node, Double> v2 = views.get(n2);

		// First part of the similarity, based on view of first agent
		double num1 = 0;
		double denom1 = 0;
		for (Node u : v1.keySet()) {
			denom1 += v1.get(u);
			if (v2.containsKey(u)) {
				num1 += v1.get(u);
			}
		}

		// Second part of the similarity, based on view of second agent
		double num2 = 0;
		double denom2 = 0;
		for (Node u : v2.keySet()) {
			denom2 += v2.get(u);
			if (v1.containsKey(u)) {
				num2 += v2.get(u);
			}
		}

		// Return value based on presence/absence of nodes in both views
		if (denom1 != 0 && denom2 != 0) {
			return (num1 / denom1) * (num2 / denom2);
		} else {
			return 0;
		}
	}

	public void nodeAdded(String graphId, long timeId, String nodeId) {
		super.nodeAdded(graphId, timeId, nodeId);
		views.put(graph.getNode(nodeId), new TreeMap<Node, Double>(
				new NodeComparator()));
	}

	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		super.nodeAdded(graphId, timeId, nodeId);
		views.remove(graph.getNode(nodeId));
	}

	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		super.edgeAdded(graphId, timeId, edgeId, fromNodeId, toNodeId, directed);

		Edge e = graph.getEdge(edgeId);
		Node u = e.getNode0();
		Node v = e.getNode1();

		System.out.println("EDGE " + u.getId() + "=" + v.getId() + " ADDED");

		neighborhoodChanged.put(u, true);
		neighborhoodChanged.put(v, true);

	}

	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		super.edgeRemoved(graphId, timeId, edgeId);

		Edge e = graph.getEdge(edgeId);
		Node u = e.getNode0();
		Node v = e.getNode1();

		System.out.println("EDGE " + u.getId() + "=" + v.getId() + " REMOVED");

		neighborhoodChanged.put(u, true);
		neighborhoodChanged.put(v, true);
	}

	class NodeComparator implements Comparator<Node> {
		public int compare(Node u, Node v) {
			return u.getId().compareTo(v.getId());
		}

	}
}
