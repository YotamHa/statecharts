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
#include "gtest/gtest.h"
#include "EntryChoice.h"

static EntryChoice statechart;

class StatemachineTest : public ::testing::Test{
	protected:
	virtual void SetUp() {
		entryChoice_init(&statechart);
	}
};


TEST_F(StatemachineTest, EntryChoiceTest) {					
	entryChoice_enter(&statechart);
	entryChoice_runCycle(&statechart);
	entryChoice_runCycle(&statechart);
	EXPECT_TRUE(entryChoice_isStateActive(&statechart, EntryChoice_main_region_A));
}


