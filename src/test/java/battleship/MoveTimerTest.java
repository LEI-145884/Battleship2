package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MoveTimer}.
 * Author: 145884 - Lukasz Swiecicki
 * Cyclomatic complexity:
 * - startMove: 1
 * - stopMove: 2
 * - getTotalMillis: 1
 * - format: 1
 */
@DisplayName("MoveTimer")
class MoveTimerTest
{
	private MoveTimer timer;

	@BeforeEach
	void setUp()
	{
		timer = new MoveTimer();
	}

	/** Busy-waits (without sleeping) until at least the given number of milliseconds has passed. */
	private static void spendAtLeast(long millis)
	{
		long end = System.nanoTime() + millis * 1_000_000L;
		while (System.nanoTime() < end)
		{
			Thread.onSpinWait();
		}
	}

	@Test
	@DisplayName("A new timer has an accumulated total of 0 ms")
	void newTimerHasZeroTotal()
	{
		assertEquals(0, timer.getTotalMillis());
	}

	@Nested
	@DisplayName("stopMove()")
	class StopMove
	{
		@Test
		@DisplayName("returns 0 and does not change the total when no move was started")
		void stopWithoutStart()
		{
			assertEquals(0, timer.stopMove());
			assertEquals(0, timer.getTotalMillis());
		}

		@Test
		@DisplayName("returns the elapsed time of the move and adds it to the total")
		void stopAfterStart()
		{
			timer.startMove();
			spendAtLeast(20);
			long elapsed = timer.stopMove();

			assertTrue(elapsed >= 20, "elapsed should be at least 20 ms but was " + elapsed);
			assertEquals(elapsed, timer.getTotalMillis());
		}

		@Test
		@DisplayName("a second stop without a new start returns 0 and keeps the total")
		void stopTwice()
		{
			timer.startMove();
			spendAtLeast(5);
			long first = timer.stopMove();

			assertEquals(0, timer.stopMove());
			assertEquals(first, timer.getTotalMillis());
		}

		@Test
		@DisplayName("accumulates the durations of several moves in the total")
		void accumulatesSeveralMoves()
		{
			timer.startMove();
			spendAtLeast(10);
			long first = timer.stopMove();

			timer.startMove();
			spendAtLeast(15);
			long second = timer.stopMove();

			assertTrue(second >= 15);
			assertEquals(first + second, timer.getTotalMillis());
		}
	}

	@Nested
	@DisplayName("startMove()")
	class StartMove
	{
		@Test
		@DisplayName("restarting a running move discards the time already measured")
		void restartResetsCurrentMove()
		{
			timer.startMove();
			spendAtLeast(50);
			timer.startMove();
			long elapsed = timer.stopMove();

			assertTrue(elapsed < 50, "restart should reset the clock, but elapsed was " + elapsed);
			assertEquals(elapsed, timer.getTotalMillis());
		}
	}

	@Nested
	@DisplayName("format()")
	class Format
	{
		@Test
		@DisplayName("formats 0 ms as 00:00.000")
		void formatsZero()
		{
			assertEquals("00:00.000", MoveTimer.format(0));
		}

		@Test
		@DisplayName("formats seconds and milliseconds as mm:ss.S")
		void formatsSecondsAndMillis()
		{
			assertEquals("00:01.500", MoveTimer.format(1_500));
		}

		@Test
		@DisplayName("formats minutes, seconds and milliseconds (padded to 3 digits)")
		void formatsMinutes()
		{
			assertEquals("02:05.007", MoveTimer.format(125_007));
		}
	}
}
