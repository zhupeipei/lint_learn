package com.example.mylibrary

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.impl.source.tree.java.PsiCodeBlockImpl
import org.jetbrains.uast.*
import org.jetbrains.uast.java.*


class NullCheckDetector() : Detector(), Detector.UastScanner {
//    private val logger = LoggerFactory.getLogger(NullCheckDetector::class.java)

    companion object {
        val ISSUE = Issue.create(
            "NullCheckIssue",
            "Log Usage",
            "Please use the unified LogUtil class!",
            Category.CORRECTNESS,
            6,
            Severity.ERROR,
            Implementation(NullCheckDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
//        return listOf(UCallExpression::class.java, UCallableReferenceExpression::class.java, UExpression::class.java)
//        return listOf(UExpression::class.java, UExpressionList::class.java)
//        return listOf(UCallExpression::class.java, UMethod::class.java)
//        return listOf(UMethod::class.java)
//        return listOf(UClass::class.java)
//        return listOf(UCallExpression::class.java, UBinaryExpression::class.java)
        return listOf(UCallExpression::class.java)
//        return listOf(UCallExpression::class.java, UCallableReferenceExpression::class.java)
//        return listOf(UMethod::class.java, UClass::class.java)
//        return listOf(UExpression::class.java, UThisExpression::class.java, UMethod::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            override fun visitClass(node: UClass) {
                if ("LintLearn".equals(node.name)) {
                    if (node is JavaUClass) {
                        node.constructors.forEach { method ->
                            (method.body as PsiCodeBlockImpl).statements.forEach {
//                                (((method.body as PsiCodeBlockImpl).statements[1].node as PsiExpressionStatementImpl).children[0].node as PsiMethodCallExpressionImpl).node.psi.children.first().children[0]
                            }
                        }
                    }
                    println("visitClass: ${node.name} $context")
//                    (node.allMethods[1].body.firstBodyElement.nextSibling as PsiExpressionStatementImpl).nextSibling.nextSibling
//                    UastLintUtils.get
                    // (node.constructors[0].node as MethodElement)
                }
//                super.visitClass(node)
            }

            override fun visitBinaryExpression(node: UBinaryExpression) {
                println("visitBinaryExpression ${node.asSourceString()}")
                if (node is JavaUAssignmentExpression) {

                }
            }

            override fun visitCallExpression(node: UCallExpression) {
                val receiver = node.receiver
                if (receiver != null && receiver is JavaUSimpleNameReferenceExpression && node.receiverType != null) {
                    val method = getUCallMethod(node)
                    if (method != null && method.isConstructor) {
                        // 构造方法里调用了call方法，并且调用者是成员变量
                        // 调用的变量annotation是个nullable，则交给系统来处理
                        if (isFieldNullable(getUFieldAnnotation(receiver))) {
                            return
                        }
                        // 这里需要判断下field在当前call之前有没有初始化过
                        val body = method.uastBody as? JavaUCodeBlockExpression ?: return
                        val expressions = body.expressions ?: return
                        for (expression in expressions) {
                            if (expression is JavaUAssignmentExpression) {
                                val identifierName =
                                    (expression.leftOperand as? JavaUSimpleNameReferenceExpression)?.identifier
                                if (identifierName == receiver.identifier) {
                                    return
                                }
                            } else if (expression is JavaUIfExpression) {
                                val condition = expression.condition
                                if (condition is JavaUBinaryExpression && condition.operator is UastBinaryOperator.ComparisonOperator && condition.operator.text == "!==") {
                                    val identifierName =
                                        (condition.leftOperand as? JavaUSimpleNameReferenceExpression)?.identifier
                                    if (identifierName == receiver.identifier) {
                                        return
                                    }
                                }
                            }
                        }
                        // 这里报错

                        println("visitCallExpression: constructor: ${method?.isConstructor}, ${node.asSourceString()}, receiverType: ${node.receiverType}, receiver: ${node.receiver}, ${node.receiver?.javaClass}")
                    }
                }
//                println("asasasasasas ${node.receiverType} $receiver ${node.classReference}")
//                super.visitCallExpression(node)
            }

            override fun visitCallableReferenceExpression(node: UCallableReferenceExpression) {
                println("visitCallableReferenceExpression: ${node.asRenderString()} ${node.asSourceString()}")
//                super.visitCallableReferenceExpression(node)
            }

            override fun visitExpression(node: UExpression) {
                println("visitExpression ${node.asSourceString()}")
                super.visitExpression(node)
            }

            override fun visitExpressionList(node: UExpressionList) {
                println("visitExpressionList ${node.asSourceString()}")
                super.visitExpressionList(node)
            }

            override fun visitThisExpression(node: UThisExpression) {
                println("visitThisExpression ${node.asSourceString()}")
                super.visitThisExpression(node)
            }

            override fun visitMethod(node: UMethod) {
//                logger.error("visitMethod ${node.asSourceString()}")
                println("visitMethod ${node.asSourceString()}")
                super.visitMethod(node)
            }
        }
    }

    // for java
    private fun getUCallMethod(node: UCallExpression): JavaUMethod? {
        var element: UElement? = node
        while (element != null) {
            if (element is JavaUMethod) {
                return element
            }
            element = element.uastParent
        }
        return null
    }

    // for java
    private fun getUClass(node: JavaUSimpleNameReferenceExpression): JavaUClass? {
        var element: UElement? = node
        while (element != null) {
            if (element is JavaUClass) {
                return element
            }
            element = element.uastParent
        }
        return null
    }

    // for java
    private fun getUFieldAnnotation(field: JavaUSimpleNameReferenceExpression): List<UAnnotation>? {
        var uClass = getUClass(field) ?: return null
        val field = uClass.fields.find { it.name == field.resolvedName }
        return field?.uAnnotations
    }

    private fun isFieldNullable(annotations: List<UAnnotation>?): Boolean {
        annotations?.forEach {
            if (isAnnotationNullable(it) == true) {
                return true
            }
        }
        return false
    }

    private fun isAnnotationNullable(annotation: UAnnotation) =
        annotation.qualifiedName?.endsWith(".Nullable")

    private fun methodIsConstructor(node: JavaUMethod) = node.isConstructor
}