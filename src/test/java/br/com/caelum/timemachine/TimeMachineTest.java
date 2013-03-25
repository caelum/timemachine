/*
 *  Copyright 2013 Caelum Ensino e Inovacao
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package br.com.caelum.timemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Some tests to TimeMachine
 *
 * @author Luiz Real
 */
public class TimeMachineTest {

	long y2002days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365
			+ 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365
			+ 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365;

	// 2002-06-09
	private final long TEST_TIME_NOW =
			(y2002days + 31L + 28L + 31L + 30L + 31L + 9L - 1L) * DateTimeConstants.MILLIS_PER_DAY;

	private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");

	private static final Block<DateTime> RETURN_CURRENT_DATE = new Block<DateTime>() {
		public DateTime run() {
			return new DateTime();
		}
	};

	private static final Block<Void> THROW_EXCEPTION = new Block<Void>() {
		public Void run() {
			throw new MyException();
		}
	};

	private DateTimeZone zone = null;
	private Locale locale = null;

	@Before
	public void setUp() throws Exception {
		DateTimeUtils.setCurrentMillisFixed(TEST_TIME_NOW);
		zone = DateTimeZone.getDefault();
		locale = Locale.getDefault();
		DateTimeZone.setDefault(LONDON);
		java.util.TimeZone.setDefault(LONDON.toTimeZone());
		Locale.setDefault(Locale.UK);
	}

	@After
	public void tearDown() throws Exception {
		DateTimeUtils.setCurrentMillisSystem();
		DateTimeZone.setDefault(zone);
		java.util.TimeZone.setDefault(zone.toTimeZone());
		Locale.setDefault(locale);
		zone = null;
	}

	@Test
	public void changesDefaultDateTimeDuringExecutionOfGivenCode() throws Exception {
		DateTime destination = new DateTime().minusDays(1);
		DateTime beforeTravel = new DateTime();
		DateTime duringTravel = TimeMachine
									.goTo(destination)
									.andExecute(RETURN_CURRENT_DATE);
		DateTime afterTravel = new DateTime();

		assertEquals(destination, duringTravel);
		assertEquals(beforeTravel, afterTravel);
	}

	@Test
	public void restoresDefaultDateTimeAfterExecutionEvenWhenAnExceptionIsThrown() throws Exception {
		DateTime destination = new DateTime().minusDays(1);
		DateTime beforeTravel = new DateTime();
		try {
			TimeMachine.goTo(destination).andExecute(THROW_EXCEPTION);
			fail("Should have called code and thrown exception");
		} catch (MyException e) {
			// ok
			DateTime afterTravel = new DateTime();
			assertEquals("Should restore default DateTime", beforeTravel, afterTravel);
		}
	}

	@SuppressWarnings("serial")
	static class MyException extends RuntimeException {}
}
