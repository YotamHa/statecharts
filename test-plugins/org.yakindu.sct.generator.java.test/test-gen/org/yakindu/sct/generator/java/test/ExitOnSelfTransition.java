/**
* Copyright (c) 2017 committers of YAKINDU and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     committers of YAKINDU - initial API and implementation
*/

package org.yakindu.sct.generator.java.test;

import org.junit.*;
import static org.junit.Assert.*;
import org.yakindu.scr.exitonselftransition.ExitOnSelfTransitionStatemachine;
import org.yakindu.scr.exitonselftransition.ExitOnSelfTransitionStatemachine.State;	

/**
 * Unit TestCase for ExitOnSelfTransition
 */
@SuppressWarnings("all")
public class ExitOnSelfTransition {
	
	private ExitOnSelfTransitionStatemachine statemachine;	
	
	@Before
	public void setUp() {
		statemachine = new ExitOnSelfTransitionStatemachine();
		statemachine.init();
	}

	@After
	public void tearDown() {
		statemachine = null;
	}
	
	@Test
	public void exitOnSelfTransitionTest() {
		statemachine.enter();
		assertTrue(statemachine.isStateActive(State.main_region_A));
		assertTrue(statemachine.getEntryCount() == 1l);
		assertTrue(statemachine.getExitCount() == 0l);
		statemachine.raiseE();
		statemachine.runCycle();
		assertTrue(statemachine.getEntryCount() == 2l);
		assertTrue(statemachine.getExitCount() == 1l);
		statemachine.raiseF();
		statemachine.runCycle();
		assertTrue(statemachine.getEntryCount() == 2l);
		assertTrue(statemachine.getExitCount() == 2l);
	}
}
