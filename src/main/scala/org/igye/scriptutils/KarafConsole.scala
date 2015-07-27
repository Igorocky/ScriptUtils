package org.igye.scriptutils

class KarafConsole(host: String, port: Int, login: String, password: String)
    extends RemoteSshConsole(host, port, login, password, "karaf@root()>")
