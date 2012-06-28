package xuml.tools.jaxb.compiler.actor;

import xuml.tools.jaxb.compiler.message.EntityCommit;
import xuml.tools.jaxb.compiler.message.Signal;
import akka.actor.ActorSystem;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedPriorityMailbox;

import com.typesafe.config.Config;

public class EntityMailbox extends UnboundedPriorityMailbox {
	public EntityMailbox(ActorSystem.Settings settings, Config config) {
		// needed for reflective instantiation
		super(new PriorityGenerator() {
			@Override
			public int gen(Object message) {
				if (message instanceof Signal) {
					if (((Signal<?>) message).toSelf())
						return 0; // high priority
					else
						return 20; // low priority
				} else if (message instanceof EntityCommit)
					return 10;// medium priority
				else
					return 30;// lowest priority
			}
		});
	}
}