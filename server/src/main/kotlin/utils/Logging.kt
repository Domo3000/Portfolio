package utils

import io.ktor.server.application.ApplicationEnvironment

context(ApplicationEnvironment)
fun logInfo(s: String) = this@ApplicationEnvironment.log.info(s)

context(ApplicationEnvironment)
fun logDebug(s: String) = this@ApplicationEnvironment.log.debug(s)

context(ApplicationEnvironment)
fun logWarn(s: String) = this@ApplicationEnvironment.log.warn(s)

context(ApplicationEnvironment)
fun logError(t: Throwable) = this@ApplicationEnvironment.log.error("", t)
