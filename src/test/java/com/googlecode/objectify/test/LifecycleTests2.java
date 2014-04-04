package com.googlecode.objectify.test;

import org.testng.annotations.Test;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.test.util.TestBase;

import static com.googlecode.objectify.test.util.TestObjectifyService.fact;
import static com.googlecode.objectify.test.util.TestObjectifyService.ofy;

/**
 * This is just getting wacky, but there's a bug in there somewhere
 */
public class LifecycleTests2 extends TestBase
{
	/** */
	@Entity
	public static class Org {
		@Id Long id;
		String foo;
	}

	/** */
	@Entity
	public static class Event {
		@Load @Parent Ref<Org> org;
		@Id Long id;
	}

	/** */
	@Entity
	public static class Product {
		@Load @Parent Ref<Event> event;
		@Id Long id;

		@OnLoad void onLoad() {
			assert event.get().org.get().foo.equals("fooValue");
		}
	}

	/**
	 * More complicated test of a more complicated structure
	 */
	@Test
	public void testCrazyComplicated() throws Exception {
		fact().register(Org.class);
		fact().register(Event.class);
		fact().register(Product.class);

		Org org = new Org();
		org.foo = "fooValue";
		ofy().save().entity(org).now();

		Event event = new Event();
		event.org = Ref.create(org);
		ofy().save().entity(event).now();

		Product prod = new Product();
		prod.event = Ref.create(event);
		ofy().save().entity(prod).now();

		ofy().clear();
		Product fetched = ofy().load().entity(prod).now();
		assert fetched.event.get().org.get().foo.equals("fooValue");
	}
}
