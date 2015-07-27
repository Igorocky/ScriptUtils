package org.igye.scriptutils

import java.io._

import org.slf4j.LoggerFactory

import scala.collection.mutable.{ListBuffer, StringBuilder}

trait ConsoleLike {
    protected val log = LoggerFactory.getLogger(this.getClass)

    protected val input: BufferedReader
    protected val errorInput: BufferedReader
    protected val output: BufferedWriter
    protected val PROMPT: String

    private var buf: StringBuilder = new StringBuilder()
    private var bufIsEmpty: Boolean = true

    var printInputToConsole = true

    def writeCmd(cmd: String): Unit = {
        output.write(logOutcoming(cmd))
        output.newLine()
        output.flush()
    }

    def writeCmdAndReadTillPrompt(cmd: String, timeoutMillis: Long = 30000): List[String] = {
        writeCmd(cmd)
        readTillPrompt(timeoutMillis)
    }

    def writeCmdAndReadTillPromptDisablePrintln(cmd: String, timeoutMillis: Long = 30000): List[String] = {
        printInputToConsoleOff()
        writeCmd(cmd)
        val res = readTillPrompt(timeoutMillis)
        printInputToConsoleOn()
        res
    }

    private def logOutcoming(msg: String): String = {
        log(s"<-- $msg")
        msg
    }

    private def removeFormatting(str: String) = {
        str.replaceAll("\033[ -/]*\\[[;\\d]*[ -/]*[@-~]", "")
    }

    private def escape(raw: String): String = {
        import scala.reflect.runtime.universe._
        Literal(Constant(raw)).toString
    }

    private def logIncoming(msg: String): String = {
        if (printInputToConsole) {
            log(s"--> $msg")
        }
        msg
    }

    private def log(msg: String): Unit = {
        log.info(msg)
    }

    private def read(): Char = {
        if (errorInput.ready()) errorInput.read().toChar else input.read().toChar
    }

    def ready = errorInput.ready() || input.ready()

    private def tryReadTillEol(input: BufferedReader): Option[String] = {
        if (!ready) {
            None
        } else {
            appendToBuf("") //if EOL will be read when the buf is empty then we assume that an empty string was read
            val inputWasNotReady = 1
            val eolWasRead = 2
            var char = read()
            var causeOfStopReading = if (char == '\r' || char == '\n') eolWasRead else null
            while (causeOfStopReading == null) {
                appendToBuf(char)
                if (!ready) {
                    causeOfStopReading = inputWasNotReady
                } else {
                    char = read()
                    if (char == '\r' || char == '\n') {
                        causeOfStopReading = eolWasRead
                    }
                }
            }
            if (causeOfStopReading == inputWasNotReady) {
                None
            } else {
                val res = readBuffAndEmptyIt()
                if (char == '\r' && ready) {
                    char = read()
                    if (char != '\n') {
                        appendToBuf(char)
                    }
                }
                //res.get will never throw an exception here because the buf was appended by an empty string at the beginning
                Option(logIncoming(res.get))
            }
        }
    }

    private def emptyBuf(): Unit = {
        buf.clear()
        bufIsEmpty = true
    }

    private def appendToBuf(char: Char): Unit = {
        buf += char
        bufIsEmpty = false
    }

    def appendToBuf(str: String): Unit = {
        buf.append(str)
        bufIsEmpty = false
    }

    private def readBuffAndEmptyIt(): Option[String] = {
        val resStr = if (!bufIsEmpty) buf.mkString else null
        emptyBuf()
        if (resStr != null) Option(removeFormatting(resStr)) else None
    }

    private def readLine(ending: String): Option[String] = {
        val res = tryReadTillEol(input)
        if (res.isDefined) {
            res
        } else {
            val alreadyReadStr = readBuffAndEmptyIt()
            if (alreadyReadStr.isDefined) {
                if (alreadyReadStr.get.trim.endsWith(ending)) {
                    logIncoming(alreadyReadStr.get)
                    alreadyReadStr
                } else {
                    appendToBuf(alreadyReadStr.get)
                    None
                }
            } else {
                None
            }
        }
    }

    protected def readTillPromptPrivate(prompt: String, timeoutMillis: Long = 30000): List[String] = {
        val startMillis = System.currentTimeMillis()
        val res = ListBuffer[String]()
        var line = readLine(prompt)
        var stopReading = false
        while (!stopReading) {
            while (!line.isDefined) {
                if (System.currentTimeMillis() - startMillis > timeoutMillis) {
                    throw new TimeoutException("timeout in readLinesTillPrompt")
                }
                Thread.sleep(50)
                line = readLine(prompt)
            }
            res += line.get
            if (line.get.trim.endsWith(prompt)) {
                stopReading = true
            } else {
                line = readLine(prompt)
            }
        }
        res.toList
    }

    def readTillPrompt(timeoutMillis: Long = 30000): List[String] = {
        readTillPromptPrivate(PROMPT)
    }

    def printInputToConsoleOn(): Unit = {
        log("printInputToConsole - On")
        printInputToConsole = true
    }

    def printInputToConsoleOff(): Unit = {
        log("printInputToConsole - Off")
        printInputToConsole = false
    }
}
