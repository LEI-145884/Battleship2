package battleship;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;

/**
 * Clock that measures the time spent on each move and the total game time.
 * Built on top of Apache Commons Lang's {@link StopWatch}.
 */
public class MoveTimer
{
	private static final String TIME_FORMAT = "mm:ss.S";

	private final StopWatch stopWatch = new StopWatch();
	private long totalMillis = 0;

	/**
	 * Starts timing a new move.
	 */
	public void startMove()
	{
		stopWatch.reset();
		stopWatch.start();
	}

	/**
	 * Stops the clock for the current move and adds its duration to the game total.
	 *
	 * @return the duration of the move in milliseconds (0 if no move was being timed)
	 */
	public long stopMove()
	{
		if (!stopWatch.isStarted())
			return 0;
		stopWatch.stop();
		long elapsed = stopWatch.getTime(TimeUnit.MILLISECONDS);
		totalMillis += elapsed;
		return elapsed;
	}

	/**
	 * @return the accumulated time of all finished moves, in milliseconds
	 */
	public long getTotalMillis()
	{
		return totalMillis;
	}

	/**
	 * Formats a duration in milliseconds as mm:ss.S.
	 */
	public static String format(long millis)
	{
		return DurationFormatUtils.formatDuration(millis, TIME_FORMAT);
	}
}
