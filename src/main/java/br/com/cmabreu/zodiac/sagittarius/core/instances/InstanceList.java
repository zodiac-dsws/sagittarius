package br.com.cmabreu.zodiac.sagittarius.core.instances;

import java.util.List;

import br.com.cmabreu.zodiac.sagittarius.entity.Instance;



public class InstanceList {
	private List<Instance> list;
	private String id;
	
	public Instance get( int index ) {
		return list.get(index);
	}
	
	public int size() {
		return list.size();
	}
	
	public String getId() {
		return id;
	}
	
	public InstanceList( List<Instance> list, String id ) {
		this.id = id;
		this.list = list;
	}
	
	public List<Instance> getList() {
		return list;
	}
	
}
