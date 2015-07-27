package org.igye.scriptutils

import java.io._

class LocalWinConsole(initialPrompt: String = ">") extends ConsoleLike {
    private val proc: Process = Runtime.getRuntime.exec("cmd")
    override protected val input: BufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream))
    override protected val errorInput: BufferedReader = new BufferedReader(new InputStreamReader(proc.getErrorStream))
    override protected val output: BufferedWriter = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream))
    override protected val PROMPT = "#:#:#:+"

    readTillPromptPrivate(initialPrompt)
    writeCmd(s"set PROMPT=$PROMPT")
    readTillPrompt()

    /*def writePSCmd(cmd: String): Unit = {
        writeCmd("PowerShell -Command \"& {" + cmd + "}\"")
    }

    def writePSCmdAndReadTilPrompt(cmd: String, timeoutMillis: Long = 30000): Unit = {
        writePSCmd(cmd)
        readTillPrompt(timeoutMillis)
    }*/

    def close(): Unit = {
        input.close()
        errorInput.close()
        output.close()
        proc.destroy()
    }
}
