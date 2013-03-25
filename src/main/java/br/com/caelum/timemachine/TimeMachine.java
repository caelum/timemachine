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

import java.lang.reflect.Field;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeUtils.MillisProvider;

/**
 * <p>
 * TimeMachine provides a simple interface for running a piece of code in a
 * different period of time, be it in the past or in the future.
 * </p>
 *
 * <p>
 * Usage:
 *
 * <pre>
 * TimeMachine.goTo(someDate).andExecute(aPieceOfCode);
 * </pre>
 *
 * <code>aPieceOfCode</code> may be a {@link Runnable} or a {@link Block}
 * </p>
 *
 * </p> During the execution of the code, a new {@link DateTime} will point to
 * the same date and time as <code>someDate</code>. After the execution, the
 * current date and time are restored to system's values. </p>
 *
 * @author Luiz Real
 */
public class TimeMachine {

	private final DateTime somewhereInTime;

	/**
	 * Private constructor. An instance of this class should be created by
	 * calling {@link #goTo(DateTime)}, as it makes code more readable.
	 *
	 * @param somewhereInTime
	 *            Date and time that should be used as current time during the
	 *            execution of the code
	 */
	private TimeMachine(DateTime somewhereInTime) {
		this.somewhereInTime = somewhereInTime;
	}

	// -----------------------------------------------------------------------
	/**
	 * Creates a TimeMachine configured to go to the given date and time when
	 * executing a piece of code.
	 *
	 * @param somewhereInTime
	 *            Date and time that should be used as current time during the
	 *            execution of the code
	 * @return A TimeMachine ready to travel in time and execute your code
	 * @see TimeMachine#andExecute(Block)
	 * @see TimeMachine#andExecute(Runnable)
	 */
	public static TimeMachine goTo(DateTime somewhereInTime) {
		return new TimeMachine(somewhereInTime);
	}

	// -----------------------------------------------------------------------
	/**
	 * Runs the given {@link Block} in the date and time given when constructing
	 * the TimeMachine. A {@link DateTime} created inside the given code will
	 * have always the same value, equal to the DateTime given when constructing
	 * the TimeMachine.
	 *
	 * @param code
	 *            The code to be run in a different date and time
	 * @return The result of running the given code
	 */
	public <T> T andExecute(Block<T> code) {
		MillisProvider provider = getCurrentTimeMillisProvider();
		DateTimeUtils.setCurrentMillisFixed(somewhereInTime.getMillis());
		try {
			return code.run();
		} finally {
			DateTimeUtils.setCurrentMillisProvider(provider);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Runs the given Runnable in the date and time given when constructing the
	 * TimeMachine. A {@link DateTime} created inside the given code will have
	 * always the same value, equal to the DateTime given when constructing the
	 * TimeMachine.
	 *
	 * @param code
	 *            The code to be run in a different date and time
	 */
	public void andExecute(Runnable code) {
		andExecute(toBlock(code));
	}

	/**
	 * Transforms a Runnable into a Block, so that we need only one
	 * implementation of the time-traveling code
	 *
	 * @param code
	 *            The Runnable to be converted
	 * @return A Block that, when called, executes the given Runnable
	 */
	private Block<Void> toBlock(final Runnable code) {
		return new Block<Void>() {
			public Void run() {
				code.run();
				return null;
			}
		};
	}

	/**
	 * Retrieves the {@link MillisProvider} in use for {@link DateTimeUtils}
	 */
	private MillisProvider getCurrentTimeMillisProvider() {
		try {
			Field providerField = DateTimeUtils.class.getDeclaredField("cMillisProvider");
			providerField.setAccessible(true);
			return (MillisProvider) providerField.get(null);
		} catch (NoSuchFieldException e) {
			throw new UnsupportedClassVersionError("The JodaTime version in use is not supported by TimeMachine. Try to use version 2.2");
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("This should not happen", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
}
