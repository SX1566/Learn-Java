package tech.simter.exception

import java.lang.RuntimeException

/**
 * @author RJ
 */
class ForbiddenException : RuntimeException {
  constructor(reason: String) : super(reason)
  constructor() : super()
}