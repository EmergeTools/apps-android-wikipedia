package org.wikipedia.analytics

object MockSDK {

    /**
     * Intentional regression initializing a mock SDK that would take a non-trivial amount of time.
     */
    fun initMockSDK() {
        Thread.sleep(500)
    }
}
