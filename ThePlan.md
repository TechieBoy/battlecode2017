# 1.Code The Framework First.

"Oh, hey! This particular weapon is really strong if used in this particular way! I'll just write some code to outfit some units with this weapon and then write some code for them to use the weapon properly. Now I have the best units and I'm kicking everyone's ass on the scrimmage server! I'm the best!"

Now the devs nerf that weapon. Oops. Or maybe they make it so you can't move robots backwards anymore, which was critical for your maneuvering. Or they buff another weapon so that it becomes the hot new thing. There are a ton of ways to screw yourself over if you program specific behavior instead of programming a framework to define behavior. Especially later on, when you need to be making changes and optimizing your strategy. If you can't easily try out multiple strategies, you're locked in. And that doesn't feel good.

Game plan should be roughly this:
#### 1.Low level infrastructure (pathfinding, messaging, attacking, base building, robot building)
#### 2.Mid-level infrastructure (getting units to be able to move and coordinate in groups)
#### 3.High-level strategy (build orders, high-level decisions)

The idea is that you can't do anything without pathfinding and attacking, and that code is not likely to change if the devs change the spec. Getting it out of the way early in a robust and unhurried manner just makes sense. It's okay if your player isn't the first one to charge the enemy base and wreck everything in sight. If you have the first player with a solid pathfinder, you're in the lead. The other teams will have to spend valuable days later in the competition fixing all the edge cases in their hastily-coded pathfinder while you're working on other stuff. Either that or they won't update it at all and their pathing will suck, giving you a key advantage.

The goal is that by the final week you have all of your API and all of your tools in place. You want to be able to have your robots do complicated actions without writing that much code, because the final week is when the final strategies start emerging. During the seeding tournament everyone sees what is and what isn't effective, and there won't be many more spec changes in the final week. The final week is the time when you want to be optimizing, and maybe completely changing, your strategy. If you have a flexible enough framework, you can adapt it to do whatever the emerging dominant strategies are.

# 2.Copy Everyone Else.

If you lose a scimmage match, don't be proud. Copy the technique that beat you, whether it's a new strategy or a combat micro refinement, and start using it. This will also let you start developing counters to that technique, using your implementation as a test opponent.

You should frequently scrim against the top teams and copy their strategies. To do well in Battlecode you don't need to invent clever strategies yourself. It's enough to copy the strategies that are doing well on the scrimmage server.

# 3.Test and Repeat.

Scrimmage everyone you can, and keep watching your games to improve.

Make it easy to write lots of code fast. You are going to have to write (and rewrite, and rewrite) a few thousand lines of code in just a few weeks. This is not a lot of time, so you need to make it as easy as possible to write code efficiently. Use a nice IDE. Wrap the API in easy-to-use interfaces. I found it particularly useful to wrap the communications functions and debugging functions in nicer interfaces.

Don't do premature bytecode optimization. Try to have a sense of which parts of your code cost a lot of bytecodes, but don't work too hard on optimizing them early on when they are still likely to change.

Test a lot. Keep a lot of versions of your bot around and play your current version against others to test your improvements. Implement other strategies and test your strategy against those.

Fix all the bugs and problems you find in your testing, unless you make a careful and considered decision that the bug is too rare or minor to matter. Unfixed bugs will come back to bite you in tournaments.

# 4.Reliable execution.

Top teams' bots have a quality I would call "reliable execution." Whatever their strategy was, they executed it reliably. This meant a bunch of things. It meant that at any point in time, every robot was doing something useful. It meant that their units didn't get stuck while pathfinding. It meant that they didn't sit around waiting forever for some attack signal that never came. It meant that if they lost some units, they rebuilt them as quickly as they could and kept trying.So the top teams were the ones who had spent enough effort on each of these sub-goals that they could execute each one, and thus their overall strategy, reliably, no matter what the map was or what the enemy did.

One lesson to draw from this is that simple strategies are better than complicated ones, because simpler strategies are easier to execute reliably.


