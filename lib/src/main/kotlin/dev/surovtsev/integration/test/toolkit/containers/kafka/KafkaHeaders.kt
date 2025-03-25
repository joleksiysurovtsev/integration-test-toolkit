package dev.surovtsev.integration.test.toolkit.containers.kafka

import java.util.*
import kotlin.reflect.full.declaredMemberProperties

data class KafkaHeaders(
	val actionId: UUID? = null,
	val messageOriginator: String? = null,
	val parentActionId: String? = null
){
	fun toMap(): Map<String, String>{
		return this::class.declaredMemberProperties.mapNotNull { member ->
			if (member.getter.call(this) != null) {
				member.name to member.getter.call(this).toString()
			}
			else null
		}.toMap()
	}
}
