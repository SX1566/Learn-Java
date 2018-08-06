package tech.simter.exception

import java.lang.RuntimeException

/**
 * @author RJ
 */
class PermissionDeniedException : RuntimeException {
  constructor(reason: String) : super(reason)
  constructor() : super()
}