package de.mc.ladon.server.core.test

/**
 * Abstract Test class for file system tests
 * Created by Ralf Ulrich on 19.08.16.
 */
abstract class AbstractFSTestRunner {


    abstract val testCase: FSTestCase


    open fun runTest() {


        createStartState(testCase.startState)

        runServiceCalls(testCase.calls)

        validateResultState(testCase.resultState)

    }

    abstract fun createStartState(startState: FSSnapshot)

    abstract fun runServiceCalls(calls: List<ServiceCall>)


    private fun validateResultState(resultState: FSSnapshot) {
        val currentState = getFSSnapshot()
        currentState.equalsExpected(resultState)
    }

    abstract fun getFSSnapshot(): FSSnapshot


}
