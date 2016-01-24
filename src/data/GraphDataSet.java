package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;

import algorithms.communityMining.data.Grouping;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;

public class GraphDataSet<V,E> {
		public String name;
		public Map<E, Double> weights;
		public Map<V, HashMap<Object, Vector<Object>>> attributes;
		public Graph<V, E> graph;
		public Map<String, V> labels_vertices;

		public GraphDataSet(String name) {
			this.name = name;
		}
		public Transformer<E, Double> getWeightsTransformer(){
			return weights==null?null: TransformerUtils.mapTransformer(weights);
		}
		
		
		public Double setWeight(E key, Double value) {
			if (weights ==null) weights = new HashMap<E, Double>();
 			return weights.put(key, value);
		}
		public HashMap<Object, Vector<Object>> getAttributesOfNode(V key) {
			return attributes.get(key);
		}
//		public HashMap<Object, Vector<Object>> setAttributesOfNode(V key, HashMap<Object, Vector<Object>> value) {
//			return attributes.put(key, value);
//		}
		public boolean addVertex(V vertex) {
			return graph.addVertex(vertex);
		}
		public boolean addEdge(E e, V v1, V v2) {
			return graph.addEdge(e, v1, v2);
		}
		public void addAttribute(V v, Object attributeKey, Object attValue){
			if (attributes==null) attributes = new HashMap<V, HashMap<Object,Vector<Object>>>();
			HashMap<Object, Vector<Object>> attributesMap = attributes.get(v);
			if(attributesMap==null){
				attributes.put(v,new HashMap<Object, Vector<Object>>());
				attributesMap = attributes.get(v);
			}
			Vector<Object> attributeValues = attributesMap.get(attributeKey);
			if(attributeValues ==null){
				attributesMap.put(attributeKey, new Vector<>());
				attributeValues = attributesMap.get(attributeKey);
			}
			attributeValues.add(attValue);
		}
		//Attribute values are the groupIds
		//Or that each unique value determines a new group
		public Grouping<V> getGrouping(Object attribute, final Object missingValue){
			 return getGrouping(attribute,new Transformer<Object, Integer>() {
					Vector<Object> clusters = new Vector<>();
					@Override
					public Integer transform(Object obj) {
						if (obj.equals(missingValue)||obj.toString().equals("null")) return null;
						if (!clusters.contains(obj)) clusters.add(obj);
						return clusters.indexOf(obj);
					}});
		}
		
		//Attribute values are the groupIds
		//Or that each unique value determines a new group
		public Grouping<V> getGrouping(Object attribute){
			 return getGrouping(attribute,new Transformer<Object, Integer>() {
					Vector<Object> clusters = new Vector<>();
					@Override
					public Integer transform(Object obj) {
						if (!clusters.contains(obj)) clusters.add(obj);
						return clusters.indexOf(obj);
					}});
		}
		// groupId transformers maps the attribute value to a groupId
		public Grouping<V> getGrouping(Object attribute, Transformer<Object, Integer> groupId){
			if (attribute instanceof String)
				attribute = ((String)attribute).toLowerCase();
			Grouping<V> res = new Grouping<>();
			for (V v : attributes.keySet()) {
				if(attributes.get(v).containsKey(attribute)){
					System.err.println(attributes.get(v));
					for (Object attValue: attributes.get(v).get(attribute)){
						Integer gId = groupId.transform(attValue);
						if (gId != null){ //missing
							while (gId>=res.getNumberOfGroups()) res.addGroup(new HashSet<V>());
							res.getGroup(gId).add(v);
						}
					}
				}else System.err.println("miss");
			}
//			res.removeGroupsSmallerThan(2);	//Not good idea since if two nodes are put into two different cluster, we will lose this info
			return res;
		}
		
		
		public Map<V,String> getLabels(){
			Map<V, String> res = new HashMap<>();
			if(labels_vertices!=null)
			for(Map.Entry<String,V> entry : labels_vertices.entrySet()){
				res.put(entry.getValue(), entry.getKey());
			}
			else for(V v :graph.getVertices()){
				res.put(v, v.toString());
			}
			return res;
		}
		
		Boolean isConnected = null;
		public boolean isConnected (Graph<V,E> graph){
			if (isConnected!=null) return isConnected;
			WeakComponentClusterer<V, E> clusterer= new WeakComponentClusterer<V, E>();
			if(clusterer.transform(graph).size()>1) {
				isConnected =false;
			}
			isConnected =true;

			return isConnected;
		}
		//Mind the lowercase
		public void addPartitioingAttribute (String key,Vector<Set<V>> grouping){
//			System.err.println("---------------adding:::  "+grouping.size());
			key  = key.toLowerCase();
			for (int groupid = 0 ; groupid< grouping.size(); groupid++) {
				for (V v : grouping.get(groupid)) {
//					System.err.println(v);
					addAttribute(v, key, groupid+1);
				}
			}
		}
		public Map<V, String> getAttMap(Object attribute){
			String attributeString ;
			Map<V, String> res = new HashMap<V, String> ();
			for (V v : attributes.keySet()) {
				if(attributes.get(v).containsKey(attribute)){
					attributeString = "";
					for (Object attValue: attributes.get(v).get(attribute)){
						if(attributeString.length()>0) attributeString+=" , ";
						attributeString+=attValue.toString();
					}
					res.put(v, attributeString);
				}
			}
			return res;
		}
		
		
		public synchronized void printStats(){
			System.out.print(">>>>>>>>>>>>>>>>>> Graph "+name+" : ");
			System.out.print(graph.getVertexCount()+" nodes and " + graph.getEdgeCount() + " Edges");
			if (weights!= null) System.out.print(" weighted ");
			if (attributes!= null) System.out.print(" attributed ");
			System.out.println();
		}
		public synchronized void print(){
			printStats();
			if(graph.getVertexCount()<500){
				System.out.println("Graph: " + graph);
				if (weights!= null) System.out.println("-- weights: " + weights);
				if (attributes!= null) System.out.println("-- attributes: " + attributes);
			}
		}
}
