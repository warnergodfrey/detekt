package io.gitlab.arturbosch.detekt.formatting

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.TokenRule

/**
 * Based on KtLint.
 *
 * @author Shyiko
 */
class ConsecutiveBlankLines(config: Config) : TokenRule("ConsecutiveBlankLines", Severity.Style, config) {

	override fun procedure(node: ASTNode) {
		if (node is PsiWhiteSpace) {
			val split = node.getText().split("\n")
			if (split.size > 3) {
				addFindings(CodeSmell(id, Entity.from(node, offset = 2), "Needless blank line(s)"))
				withAutoCorrect {
					(node as LeafPsiElement).replaceWithText("${split.first()}\n\n${split.last()}")
				}
			}
		}
	}

}