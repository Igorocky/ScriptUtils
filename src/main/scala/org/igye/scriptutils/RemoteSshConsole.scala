package org.igye.scriptutils

import java.io._

import com.jcraft.jsch._

class RemoteSshConsole(host: String, port: Int, login: String, password: String, prompt: String = ">") extends ConsoleLike {
    private val (session, channel, inputStream, errorInputStream, outputStream) = init()
    override protected val input: BufferedReader = new BufferedReader(new InputStreamReader(inputStream))
    override protected val errorInput: BufferedReader = new BufferedReader(new InputStreamReader(errorInputStream))
    override protected val output: BufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))
    override protected val PROMPT = prompt

    private def init(): (Session, Channel, PipedInputStream, PipedInputStream, PipedOutputStream) = {
        val jsch = new JSch
        val session = jsch.getSession(login, host, port)
        session.setPassword(password)
        val ui = new MyUserInfo(){}
        session.setUserInfo(ui)
        session.connect(30000)
        val channel=session.openChannel("shell")

        val userInputStream = new PipedInputStream()
        val userErrorInputStream = new PipedInputStream()
        val userOutputStream = new PipedOutputStream()
        channel.setOutputStream(new PipedOutputStream(userInputStream))
        channel.setInputStream(new PipedInputStream(userOutputStream))

        channel.connect(3000)
        (session, channel, userInputStream, userErrorInputStream, userOutputStream)
    }

    def disconnect(): Unit = {
        input.close()
        errorInput.close()
        output.close()
        channel.disconnect()
        session.disconnect()
    }

    abstract class MyUserInfo extends UserInfo with UIKeyboardInteractive {
        override def getPassword() = null
        override def promptYesNo(str: String) = true
        override def getPassphrase() = null
        override def promptPassphrase(message: String) = false
        override def promptPassword(message: String) = false
        override def showMessage(message: String) {}
        override def promptKeyboardInteractive(destination: String,
                name: String,
            instruction: String,
            prompt: Array[String],
            echo: Array[Boolean]) = null
    }
}
