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
import org.yakindu.scr.statechartkeywords.StatechartKeywordsStatemachine;
import org.yakindu.scr.statechartkeywords.StatechartKeywordsStatemachine.State;	
import org.yakindu.scr.VirtualTimer;

/**
 * Unit TestCase for StatechartKeywords
 */
@SuppressWarnings("all")
public class StatemachineKeywords {
	
	private StatechartKeywordsStatemachine statemachine;	
	private VirtualTimer timer;
	
	@Before
	public void setUp() {
		statemachine = new StatechartKeywordsStatemachine();
		timer = new VirtualTimer();
		statemachine.setTimer(timer);
		statemachine.init();
	}

	@After
	public void tearDown() {
		statemachine = null;
	}
	
	@Test
	public void statemachineKeywords() {
		statemachine.enter();
		assertTrue(statemachine.isStateActive(State.main_region_Timer));
	}
}
