package io.gitlab.arturbosch.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * @author Artur Bosch
 */
class NoDocOverPublicMethod(config: Config = Config.empty) : Rule("NoDocOverPublicMethod", Severity.Maintainability, config) {

	override fun visitNamedFunction(function: KtNamedFunction) {
		if (function.funKeyword == null && function.isLocal) return

		val modifierList = function.modifierList
		if (function.docComment == null) {
			if (modifierList == null) {
				addFindings(CodeSmell(id, methodHeaderLocation(function)))
			}
			if (modifierList != null) {
				if (function.isPublicNotOverriden()) {
					addFindings(CodeSmell(id, methodHeaderLocation(function)))
				}
			}
		}
	}

	private fun methodHeaderLocation(function: KtNamedFunction) = Entity.from(function)

}