package com.googlecode.objectify;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

/**
 * <p>This is the underlying implementation of the ObjectifyFactory static methods, available
 * as a singleton.  If you wish to override factory behavior, you can inject a subclass of
 * this object in your code.  You can also make calls to these methods by calling
 * {@code ObjectifyFact.instance().begin()} etc if you think static methods are fugly.</p>
 * 
 * <p>In general, however, you probably want to use the ObjectifyFactory methods.</p>
 * 
 * <p>Note that all method documentation is on ObjectifyFactory.</p>
 * 
 * @see ObjectifyFactory
 * 
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class Factory
{
	/** */
	protected static Factory instance = new Factory();
	
	/** Obtain the singleton as an alternative to using the static methods */
	public static Factory instance() { return instance; }
	
	/** */
	protected Map<String, EntityMetadata> types = new HashMap<String, EntityMetadata>();
	
	/** @see ObjectifyFactory#begin() */
	public Objectify begin()
	{
		return new ObjectifyImpl(DatastoreServiceFactory.getDatastoreService(), null);
	}
	
	/** @see ObjectifyFactory#beginTransaction() */
	public Objectify beginTransaction()
	{
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		return new ObjectifyImpl(ds, ds.beginTransaction());
	}
	
	/** @see ObjectifyFactory#withTransaction(Transaction) */
	public Objectify withTransaction(Transaction txn)
	{
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		return new ObjectifyImpl(ds, txn);
	}
	
	/** @see ObjectifyFactory#register(Class) */
	public void register(Class<?> clazz)
	{
		String kind = getKind(clazz);
		this.types.put(kind, new EntityMetadata(clazz));
	}
	
	//
	// Methods equivalent to those on KeyFactory, but which use typed Classes instead of kind.
	//
	
	/** @see ObjectifyFactory#createKey(Class, long) */
	public Key createKey(Class<?> kind, long id)
	{
		return KeyFactory.createKey(getKind(kind), id);
	}
	
	/** @see ObjectifyFactory#createKey(Class, String) */
	public Key createKey(Class<?> kind, String name)
	{
		return KeyFactory.createKey(getKind(kind), name);
	}
	
	/** @see ObjectifyFactory#createKey(Key, Class, long) */
	public Key createKey(Key parent, Class<?> kind, long id)
	{
		return KeyFactory.createKey(parent, getKind(kind), id);
	}
	
	/** @see ObjectifyFactory#createKey(Key, Class, String) */
	public Key createKey(Key parent, Class<?> kind, String name)
	{
		return KeyFactory.createKey(parent, getKind(kind), name);
	}
	
	/** @see ObjectifyFactory#createKey(Object) */
	public Key createKey(Object entity)
	{
		return getMetadata(entity).getKey(entity);
	}
	
	//
	// Friendly query creation methods
	//
	
	/** @see ObjectifyFactory#createQuery() */
	public Query createQuery()
	{
		return new Query();
	}
	
	/** @see ObjectifyFactory#createQuery(Class) */
	public Query createQuery(Class<?> entityClazz)
	{
		return new Query(getKind(entityClazz));
	}
	
	//
	// Stuff which should only be necessary internally.
	//
	
	/** @see ObjectifyFactory#getKind(Class) */
	public String getKind(Class<?> clazz)
	{
		javax.persistence.Entity entityAnn = clazz.getAnnotation(javax.persistence.Entity.class);
		if (entityAnn == null)
			return clazz.getSimpleName();
		else
			return entityAnn.name();
	}
	
	/** @see ObjectifyFactory#getMetadata(Key) */
	public EntityMetadata getMetadata(Key key)
	{
		EntityMetadata metadata = types.get(key.getKind());
		if (metadata == null)
			throw new IllegalArgumentException("No registered type for kind " + key.getKind());
		else
			return metadata;
	}
	
	/** @see ObjectifyFactory#getMetadata(Object) */
	public EntityMetadata getMetadata(Object obj)
	{
		EntityMetadata metadata = types.get(getKind(obj.getClass()));
		if (metadata == null)
			throw new IllegalArgumentException("No registered type for kind " + getKind(obj.getClass()));
		else
			return metadata;
	}
}