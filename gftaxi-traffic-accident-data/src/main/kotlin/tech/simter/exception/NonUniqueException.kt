package tech.simter.exception

import java.lang.RuntimeException

/**
 * @author RJ
 */
class NonUniqueException : RuntimeException {
  constructor(reason: String) : super(reason)
  constructor() : super()
}