package mainpackage

import com.gamasoft.kakomu.agent.EvaluatorTest
import com.gamasoft.kakomu.model.BoardTest
import com.gamasoft.kakomu.model.GameStateTest
import com.gamasoft.kakomu.model.GoStringTest
import com.gamasoft.kakomu.model.ZobristTest
import org.junit.runner.JUnitCore
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import java.io.File

class TestListener : RunListener() {
    lateinit var result : Result

    override fun testRunFinished(result: Result){
        this.result = result
    }
}

data class HandlerOutput(val startTime: Long,
                         val runtime: Long,
                         val coldStart: Boolean,
                         val wasSuccess: Boolean,
                         val failures: MutableList<Failure>)

class RunTests {
    fun getStringResult(startTime: Long) : String {
        val listener = run()
        return createString(startTime, listener.result)
    }
    fun getJsonResult(startTime: Long) : HandlerOutput {
        val listener = run()
        return createJson(startTime, listener.result)
    }

    private fun run() : TestListener{
        val junit = JUnitCore()
        val listener = TestListener()
        junit.addListener(listener)
        junit.run(//EvaluatorTest::class.java,
            ZobristTest::class.java,
            BoardTest::class.java,
            GameStateTest::class.java,
            GoStringTest::class.java)
        return listener
    }

    private fun createString(startTime : Long, result: Result):String {
        return "{\"startTime\": ${startTime}, \"runtime\": ${result.runTime}, \"coldStart\": ${isColdStart()}, \"wasSuccess\": ${result.wasSuccessful()}," +
                " \"failures\": ${result.failures}}"
    }

    private fun createJson(startTime: Long, result: Result) : HandlerOutput{
        return HandlerOutput(startTime, result.runTime, isColdStart(), result.wasSuccessful(), result.failures)
    }

    private fun isColdStart(): Boolean{
        val fileName = "/tmp/out.txt"
        val file = File(fileName)

        if(file.exists()){
            return false;
        }
        File(fileName).createNewFile()
        return true;
    }
}