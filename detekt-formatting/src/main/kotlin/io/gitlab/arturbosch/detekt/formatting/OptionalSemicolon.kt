package io.gitlab.arturbosch.detekt.formatting

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.TokenRule
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtStringTemplateEntry
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType

/**
 * @author Artur Bosch
 */
class OptionalSemicolon(config: Config = Config.empty) : TokenRule("OptionalSemicolon", Severity.Style, config) {

	override fun procedure(node: ASTNode) {
		val psi = node.psi
		if (psi.isNoErrorElement() && psi.isNotPartOfEnum() && psi.isNotPartOfString()) {
			if (psi.isDoubleSemicolon()) {
				addFindings(CodeSmell(id, Entity.from(psi)))
				withAutoCorrect {
					deleteOneOrTwoSemicolons(node, psi)
				}
			} else if (psi.isSemicolon()) {
				val nextLeaf = PsiTreeUtil.nextLeaf(psi)
				if (isSemicolonOrEOF(nextLeaf) || nextTokenHasSpaces(nextLeaf)) {
					addFindings(CodeSmell(id, Entity.from(psi)))
					withAutoCorrect { psi.delete() }
				}
			}
		}
	}

	private fun deleteOneOrTwoSemicolons(node: ASTNode, psi: PsiElement) {
		val nextLeaf = PsiTreeUtil.nextLeaf(psi)
		if (isSemicolonOrEOF(nextLeaf) || nextTokenHasSpaces(nextLeaf)) {
			psi.delete()
		} else {
			(node as LeafPsiElement).replaceWithText(";")
		}
	}

	private fun PsiElement.isNotPartOfString() = this.getNonStrictParentOfType(KtStringTemplateEntry::class.java) == null
	private fun PsiElement.isNotPartOfEnum() = this.getNonStrictParentOfType(KtEnumEntry::class.java) == null
	private fun PsiElement.isNoErrorElement() = this is LeafPsiElement && this !is PsiErrorElement
	private fun PsiElement.isSemicolon() = this.textMatches(";")
	private fun PsiElement.isDoubleSemicolon() = this.textMatches(";;")

	private fun nextTokenHasSpaces(nextLeaf: PsiElement?) = nextLeaf is PsiWhiteSpace &&
			(nextLeaf.text.contains("\n") || isSemicolonOrEOF(PsiTreeUtil.nextLeaf(nextLeaf)))

	private fun isSemicolonOrEOF(nextLeaf: PsiElement?) = nextLeaf == null || nextLeaf.isSemicolon() || nextLeaf.isDoubleSemicolon()

}

