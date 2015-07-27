package org.igye.scriptutils

import scala.io.StdIn

object U {
    private val RESET = "\033[0m"
    private val RED = "\033[31m"
    private val BLUE = "\033[34m"
    private val GREEN = "\033[32m"
    private val BOLD = "\033[1m"

    def confirm(msg: String, formatting: String => String = s=>bold(blue(s)), defaultAnswer: String = "y"): Boolean = {
        print(formatting(s"$msg (y/n)[$defaultAnswer]"))
        val userAnswer = StdIn.readLine()
        "y" == (if (userAnswer != "") userAnswer.toLowerCase else defaultAnswer)
    }

    def mustConfirm(msg: String, defaultAnswer: String = "n") = {
        if (!confirm(msg, s => bold(red(s)), defaultAnswer)) {
            throw new NotConfirmedException(s"Not confirmed: $msg")
        }
    }

    def shouldConfirm(msg: String, defaultAnswer: String = "y") = {
        if (!confirm(msg, red, defaultAnswer)) {
            throw new NotConfirmedException(s"Not confirmed: $msg")
        }
    }

    def red(str: String): String = {
        s"$RED$str$RESET"
    }

    def blue(str: String): String = {
        s"$BLUE$str$RESET"
    }

    def green(str: String): String = {
        s"$GREEN$str$RESET"
    }

    def bold(str: String): String = {
        s"$BOLD$str$RESET"
    }
}
