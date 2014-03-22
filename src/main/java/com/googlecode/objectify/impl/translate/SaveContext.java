package com.googlecode.objectify.impl.translate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.Property;
import com.googlecode.objectify.impl.Reference;
import com.googlecode.objectify.util.IdentityMultimapList;

/**
 * The context of a save operation; might involve multiple entities (eg, batch save).
 *
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class SaveContext
{
	/** The objectify instance */
	Objectify ofy;

	/** The current root entity; will change as multiple entities are loaded */
	Object currentRoot;

	/**
	 * Potential upgrades found during the save process; key is the pojo entity itself (its identity).
	 * The problem is that when saving, the id may not be set yet, so we can't track keys.
	 */
	IdentityMultimapList<Object, Reference> references = new IdentityMultimapList<Object, Reference>();
	
	/**
	 * Track all indexed values here. We may need to use some of this data to create synthetic
	 * indexes at the top level (ie, dot-separated indexes for v2 embedded saves).
	 */
	SetMultimap<Path, Object> indexes = HashMultimap.create();

	/** */
	public SaveContext(Objectify ofy) {
		this.ofy = ofy;
	}

	/** */
	public Objectify getObjectify() { return this.ofy; }

	/** Sets the current root entity, not its key! */
	public void setCurrentRoot(Object rootEntity) {
		this.currentRoot = rootEntity;
	}

	/** */
	public void registerReference(Property prop, Ref<?> ref) {
		references.add(currentRoot, new Reference(prop, ref.key()));
	}

	/** @return an empty list if no upgrades found */
	public List<Reference> getReferences(Object pojo) {
		List<Reference> list = references.get(pojo);
		if (list == null)
			return Collections.emptyList();
		else
			return list;
	}
	
	/** */
	public void addIndex(Path path, Object object) {
		indexes.put(path, object);
	}
	
	/** */
	public Map<Path, Collection<Object>> getIndexes() {
		return indexes.asMap();
	}
}